package com.footballacademy.controllers_rest.file;

import com.footballacademy.security.UserPrincipal;
import com.footballacademy.services.file.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public
class FileUploadController {
    private final FileStorageService fileStorageService;
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
    @AuthenticationPrincipal UserPrincipal principal,
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "description", required = false) String description,
    @RequestParam(value = "category", required = false) String category) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Authentication required"));
            }
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "File cannot be empty"));
            } String storedName = fileStorageService.storeFile(file,(category == null || category.isBlank()) ? "misc" : category, description, principal.getUser() .getId());
            return ResponseEntity.ok(Map.of("message", "File uploaded successfully", "filePath", storedName, "originalName", file.getOriginalFilename(), "size", file.getSize(), "contentType", file.getContentType()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "File upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "File upload error: " + e.getMessage()));
        }
    }
    @GetMapping("/{fileName}")
    public ResponseEntity<?> downloadFile(
    @PathVariable String fileName) {
        try {
            if (fileName == null || fileName.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "File name is required"));
            } Resource resource = fileStorageService.loadFileAsResource(fileName);
            String contentType = "application/octet-stream";
            try {
                contentType = resource.getURL() != null ? resource.getURL() .openConnection() .getContentType() : contentType;
            } catch (Exception ignored) {
            } return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType)) .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"") .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "File not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to download file: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(
    @PathVariable String fileName,
    @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Authentication required"));
            }
            if (fileName == null || fileName.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "File name is required"));
            } fileStorageService.deleteFile(fileName);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "File not found for deletion: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete file: " + e.getMessage()));
        }
    }
}
