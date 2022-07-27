package app.core.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.core.services.FileStorageService;

@RestController
@RequestMapping("/files")
@CrossOrigin
public class FileController {

	@Autowired
	private FileStorageService fileStorageService;

	private String generateErrorMsg(Exception e) {
		String msg = e.getMessage();
		Throwable t = e.getCause();
		while (t != null) {
			msg += " => " + t.getClass().getSimpleName();
			t = t.getCause();
		}
		return msg;
	}

	@PostMapping
	public String uploadFile(@RequestParam() MultipartFile file) {
		try {
			String fileName = this.fileStorageService.storeFile(file);
			return fileName;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, generateErrorMsg(e));
		}
	}

	@PostMapping("/multi")
	public List<String> uploadFiles(@RequestParam() MultipartFile[] files) {
		var names = new ArrayList<String>();
		try {
			for (MultipartFile multipartFile : files) {
				String fileName = this.fileStorageService.storeFile(multipartFile);
				names.add(fileName);
			}
			return names;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, generateErrorMsg(e));
		}
	}
	
	

	@GetMapping("/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		return downloadFileByName(fileName, request);
	}

	@GetMapping("/request-params")
	public ResponseEntity<Resource> downloadFileByName(@RequestParam String fileName, HttpServletRequest request) {
		try {
			// look for the file and get it as a Resource
			Resource resource = this.fileStorageService.loadFileAsResource(fileName);
			// figure out the mime type based on file name
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
			} catch (IOException e) {
				System.out.println("failed to determine mime type");
			}
			if (contentType == null) {
				// arbitrary binary data
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
			System.out.println("content type: " + contentType);
			return ResponseEntity

					.ok()

					.contentType(MediaType.parseMediaType(contentType))
					// inline - displayed inline in the Web page or as part of a Web page
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")

					// attachement - downloaded and saved locally
//					.header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + resource.getFilename() + "\"")

					.body(resource);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, generateErrorMsg(e));
		}
	}

	@PutMapping("/{fileName:.+}/{newFileName:.+}")
	public void renameFile(@PathVariable String fileName, @PathVariable String newFileName) {
		try {
			this.fileStorageService.renameFile(fileName, newFileName);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, generateErrorMsg(e));
		}
	}

	@DeleteMapping("/{fileName:.+}")
	public boolean deleteFile(@PathVariable String fileName) {
		try {
			return this.fileStorageService.deleteFile(fileName);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, generateErrorMsg(e));
		}
	}

	@DeleteMapping
	public void deleteAllFiles() {
		try {
			this.fileStorageService.cleanStorageDirectory();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, generateErrorMsg(e));
		}
	}

	@GetMapping
	public String[] listFiles() {
		return this.fileStorageService.listFiles();
	}

}
