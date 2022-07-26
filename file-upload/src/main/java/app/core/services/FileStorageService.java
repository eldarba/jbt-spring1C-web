package app.core.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import javax.annotation.PostConstruct;

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
	private Path fileStoragePath;

	@PostConstruct
	public void init() {
		this.fileStoragePath = Paths.get(this.uploadDir).toAbsolutePath().normalize();
		System.out.println("file storage path: " + this.fileStoragePath);
		try {
			Files.createDirectories(fileStoragePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String uploadFile(MultipartFile multipartFile) throws FileAlreadyExistsException {
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		System.out.println(">>> " + fileName);
		if (fileName.contains("..")) {
			throw new RuntimeException("file name invalid: " + fileName);
		}
		try {
			Path targetLocation = this.fileStoragePath.resolve(fileName);
			System.out.println(">>> " + targetLocation.toString());
//			Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(multipartFile.getInputStream(), targetLocation); // error if file exists
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("storing file failed: " + fileName, e);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			// get path to the resource (the file)
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

	public boolean deleteFile(String fileName) {
		Path filePath = this.fileStoragePath.resolve(fileName).normalize();
		File file = filePath.toFile();
		if (file.exists()) {
			return file.delete();
		} else {
			throw new RuntimeException("downloadFile failed. file not found: " + fileName);
		}
	}

	public void deleteAllFiles() {
		File rootDir = this.fileStoragePath.toFile();
		deleteDirectory(rootDir);
	}

	private void deleteDirectory(File dir) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		}
	}

	public boolean renameFile(String fileName, String newFileName) {
		Path filePath = this.fileStoragePath.resolve(fileName).normalize();
		File file = filePath.toFile();
		if (file.exists()) {
			Path newFilePath = this.fileStoragePath.resolve(newFileName).normalize();
			return file.renameTo(newFilePath.toFile());
		} else {
			throw new RuntimeException("renameFile failed. file not found: " + fileName);
		}
	}

	public String[] listFiles() {
		return this.fileStoragePath.toFile().list();
	}

}
