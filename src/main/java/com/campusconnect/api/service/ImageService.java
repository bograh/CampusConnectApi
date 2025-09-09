package com.campusconnect.api.service;

import com.campusconnect.api.entity.embedded.ImageData;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    private String generateFileName(String folder) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return folder + "/" + uuid;
    }

    public ImageData uploadImage(MultipartFile file, String folder) throws IOException {
        String fileName = generateFileName(folder);
        
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().uploadLarge(file.getBytes(), ObjectUtils.asMap(
                    "public_id", fileName,
                    "unique_filename", true,
                    "overwrite", true,
                    "folder", folder
            ));

            return extractImageDataFromResult(uploadResult, fileName);
            
        } catch (Exception e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    public ImageData uploadImage(String base64Image, String folder) {
        try {
            if (base64Image == null || base64Image.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 image cannot be null or empty");
            }

            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String fileName = generateFileName(folder);
            
            Map<?, ?> uploadResult = cloudinary.uploader().uploadLarge(imageBytes, ObjectUtils.asMap(
                    "public_id", fileName,
                    "unique_filename", true,
                    "overwrite", true,
                    "folder", folder
            ));

            return extractImageDataFromResult(uploadResult, fileName);
            
        } catch (Exception e) {
            log.error("Error uploading base64 image to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) {
        try {
            if (publicId != null && !publicId.trim().isEmpty()) {
                Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted from Cloudinary: {}, result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage());
        }
    }

    private ImageData extractImageDataFromResult(Map<?, ?> uploadResult, String fileName) throws IOException {
        Object url = uploadResult.get("secure_url");
        Object width = uploadResult.get("width");
        Object height = uploadResult.get("height");

        if (url != null) {
            String transformedUrl = transformCloudinaryUrl(url.toString());
            
            return ImageData.builder()
                    .url(transformedUrl)
                    .publicId(fileName)
                    .width(width != null ? (Integer) width : null)
                    .height(height != null ? (Integer) height : null)
                    .build();
        } else {
            log.error("Cloudinary upload failed for {}, URL not found in response: {}", fileName, uploadResult);
            throw new IOException("Failed to upload image to Cloudinary, URL not found in response.");
        }
    }

    private static String transformCloudinaryUrl(String originalUrl) {
        int uploadIndex = originalUrl.indexOf("upload/");

        if (uploadIndex != -1) {
            String baseUrl = originalUrl.substring(0, uploadIndex + "upload/".length());
            String imagePath = originalUrl.substring(uploadIndex + "upload/".length());
            
            return baseUrl + "q_auto/f_avif/" + imagePath;
        } else {
            return originalUrl;
        }
    }
}
