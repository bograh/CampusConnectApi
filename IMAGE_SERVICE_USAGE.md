# CampusConnect Image Service - Cloudinary Integration

## Overview

The `ImageService` now includes full Cloudinary integration for uploading, transforming, and managing images in the CampusConnect application. It supports both MultipartFile uploads (for direct file uploads) and base64 string uploads (for existing functionality).

## Configuration

Add the following to your `application.properties`:

```properties
# Cloudinary Configuration
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME:your_cloud_name}
cloudinary.api-key=${CLOUDINARY_API_KEY:your_api_key}
cloudinary.api-secret=${CLOUDINARY_API_SECRET:your_api_secret}
```

## Features

### 1. MultipartFile Upload

For direct file uploads from forms:

```java
@Autowired
private ImageService imageService;

public ImageData uploadUserProfile(MultipartFile file) throws IOException {
    return imageService.uploadImage(file, "profile-images");
}
```

### 2. Base64 Upload

For existing functionality (student ID images, selfies):

```java
public ImageData uploadStudentId(String base64Image) {
    return imageService.uploadImage(base64Image, "student-ids");
}
```

### 3. Image Deletion

Remove images from Cloudinary:

```java
public void removeUserImage(String publicId) {
    imageService.deleteImage(publicId);
}
```

## API Endpoints

### Upload Image (MultipartFile)

```http
POST /api/upload/image
Content-Type: multipart/form-data

Form Data:
- file: [image file]
- folder: "uploads" (optional)
```

### Upload Image (Base64)

```http
POST /api/upload/image/base64
Content-Type: application/json

Body:
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEA...",
  "folder": "uploads"
}
```

### Delete Image

```http
DELETE /api/upload/image/{publicId}
```

## URL Optimization

Images are automatically optimized with:

- **Auto Quality**: Cloudinary automatically adjusts quality for best performance
- **Auto Format**: Serves AVIF, WebP, or JPEG based on browser support
- **Secure URLs**: All URLs use HTTPS

Example transformed URL:

```
Original: https://res.cloudinary.com/demo/image/upload/v1234567890/folder/filename.jpg
Optimized: https://res.cloudinary.com/demo/image/upload/q_auto/f_auto/v1234567890/folder/filename.jpg
```

## Error Handling

The service includes comprehensive error handling:

- Invalid file types are rejected
- Upload failures are logged and re-thrown as IOExceptions
- Base64 decoding errors are handled gracefully
- Deletion failures are logged but don't throw exceptions

## Security Notes

1. **File Validation**: Only image files are accepted
2. **Unique Filenames**: UUIDs prevent filename collisions
3. **Secure URLs**: All Cloudinary URLs use HTTPS
4. **Environment Variables**: Credentials are externalized

## Integration with Existing Code

The service maintains backward compatibility with the existing base64 upload method used in `UserService.java` for student ID and selfie uploads.

## Example Usage in Controllers

```java
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ImageService imageService;

    @PostMapping("/avatar")
    public ResponseEntity<ImageData> uploadAvatar(
            @RequestPart("file") MultipartFile file) throws IOException {

        ImageData imageData = imageService.uploadImage(file, "avatars");
        return ResponseEntity.ok(imageData);
    }
}
```
