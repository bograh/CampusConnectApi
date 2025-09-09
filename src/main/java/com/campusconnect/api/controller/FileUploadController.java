package com.campusconnect.api.controller;

import com.campusconnect.api.entity.embedded.ImageData;
import com.campusconnect.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final ImageService imageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageData> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "uploads") String folder) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Check if file is an image
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            ImageData imageData = imageService.uploadImage(file, folder);
            return ResponseEntity.ok(imageData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/image/base64")
    public ResponseEntity<ImageData> uploadBase64Image(
            @RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            String folder = request.getOrDefault("folder", "uploads");

            if (base64Image == null || base64Image.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ImageData imageData = imageService.uploadImage(base64Image, folder);
            return ResponseEntity.ok(imageData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/image/{publicId}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String publicId) {
        try {
            // Replace URL-encoded slashes with actual slashes for Cloudinary public_id
            String decodedPublicId = publicId.replace("%2F", "/");
            imageService.deleteImage(decodedPublicId);
            
            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete image"));
        }
    }
}
