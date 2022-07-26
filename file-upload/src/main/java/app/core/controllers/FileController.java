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
			String fileName = this.fileStorageService.uploadFile(file);
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
				String fileName = this.fileStorageService.uploadFile(multipartFile);
				names.add(fileName);
			}
			return names;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, generateErrorMsg(e));
		}
	}

	@GetMapping("/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		try {
			Resource resource = this.fileStorageService.loadFileAsResource(fileName);
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
			System.out.println("content type = " + contentType);
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, generateErrorMsg(e));
		}
	}

	@GetMapping("/request-params")
	public ResponseEntity<Resource> downloadFile2(@RequestParam String fileName, HttpServletRequest request) {
		try {
			Resource resource = this.fileStorageService.loadFileAsResource(fileName);
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
			System.out.println("content type = " + contentType);
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@DeleteMapping("/{fileName:.+}")
	public boolean deleteFile(@PathVariable String fileName1) {
		try {
			return this.fileStorageService.deleteFile(fileName1);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@DeleteMapping
	public void deleteAllFiles() {
		try {
			this.fileStorageService.deleteAllFiles();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PutMapping("/{fileName:.+}/{newFileName:.+}")
	public boolean renameFile(@PathVariable String fileName, @PathVariable String newFileName) {
		try {
			return this.fileStorageService.renameFile(fileName, newFileName);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping
	public String[] listFiles() {
		return this.fileStorageService.listFiles();
	}

}
