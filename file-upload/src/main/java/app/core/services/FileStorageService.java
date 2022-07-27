package app.core.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	@Value("${file.upload-dir}")
	private String uploadDir;
	private Path fileStoragePath; // Path is like File

	@PostConstruct
	public void init() {
		// initialize the path to the store directory.
		// normalize removes redundant . or ..
		this.fileStoragePath = Paths.get(this.uploadDir).toAbsolutePath().normalize();
		System.out.println("file storage path: " + this.fileStoragePath);
		try {
			Files.createDirectories(fileStoragePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String storeFile(MultipartFile multipartFile) {
		// get the original filename in the client's filesystem
		// cleanPath removes redundant . or ..
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		System.out.println(">>> " + fileName);
		if (fileName.contains("..")) {
			throw new RuntimeException("file name invalid: " + fileName);
		}
		try {
			// create the path to the new file to save
			// resolve adds the fileName to the path
			Path fileTargetLocation = this.fileStoragePath.resolve(fileName);
			System.out.println(">>> " + fileTargetLocation.toString());
			// copy the file to the target location
			Files.copy(multipartFile.getInputStream(), fileTargetLocation, StandardCopyOption.REPLACE_EXISTING);
//			Files.copy(multipartFile.getInputStream(), targetLocation); // error if file exists
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("storing file failed: " + fileName, e);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			// get path to the file
			// resolve method - adds to the path
			Path filePath = this.fileStoragePath.resolve(fileName).normalize();
			// create the resource descriptor object
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new RuntimeException("file not found: " + fileName);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("file not found: " + fileName, e);
		}
	}

	// public void deleteAllFilesInStorageDirectory() {
	// File rootDir = this.fileStoragePath.toFile();
	// deleteDirectory(rootDir);
	// }
	//
	// private void deleteDirectory(File dir) {
	// for (File file : Objects.requireNonNull(dir.listFiles())) {
	// if (file.isDirectory()) {
	// deleteDirectory(file);
	// } else {
	// file.delete();
	// }
	// }
	// }

	public void renameFile(String oldFileName, String newFileName) {
		Path oldFilePath = this.fileStoragePath.resolve(oldFileName).normalize();
		Path newFilePath = this.fileStoragePath.resolve(newFileName).normalize();
		try {
			// rename from old name to new name
			Files.move(oldFilePath, newFilePath);
		} catch (IOException e) {
			throw new RuntimeException("renameFile failed", e);
		}
	}

	public boolean deleteFile(String fileName) {
		Path filePath = this.fileStoragePath.resolve(fileName).normalize();
		try {
			return Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new RuntimeException("downloadFile failed", e);
		}
	}

	public void cleanStorageDirectory() {
		File rootDir = this.fileStoragePath.toFile();
		try {
			// org.apache.tomcat.util.http.fileupload.FileUtils
			FileUtils.cleanDirectory(rootDir);
		} catch (IOException e) {
			throw new RuntimeException("deleteAllFilesInStorageDirectory failed", e);
		}
	}

//	public void deleteAllFilesInStorageDirectory() {
//		File rootDir = this.fileStoragePath.toFile();
//		deleteDirectory(rootDir);
//	}
//
//	private void deleteDirectory(File dir) {
//		for (File file : Objects.requireNonNull(dir.listFiles())) {
//			if (file.isDirectory()) {
//				deleteDirectory(file);
//			} else {
//				file.delete();
//			}
//		}
//	}

	public String[] listFiles() {
		return this.fileStoragePath.toFile().list();
	}

}
