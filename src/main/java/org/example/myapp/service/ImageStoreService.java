package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

@ApplicationScoped
public class ImageStoreService {

    private static final long MAX_SIZE_BYTES = 500L * 1024L; // 500 KB

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "s3.public-url")
    String s3BaseUrl;

    @Inject
    S3Client s3;

    // ------------------------------------------------------------
    // VALIDATION
    // ------------------------------------------------------------

    public void validateImage(byte[] file) {
        if (file == null || file.length == 0) {
            throw new IllegalArgumentException("photo.empty");
        }

        if (file.length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("photo.toobig");
        }

        String mime;
        try {
            mime = URLConnection.guessContentTypeFromStream(
                    new ByteArrayInputStream(file)
            );
        } catch (IOException e) {
            mime = null;
        }

        // Fallback magic-byte detection for PNG / JPEG if guessContentTypeFromStream returns null
        boolean isPng = file.length >= 8
                && (file[0] & 0xFF) == 0x89
                && (file[1] & 0xFF) == 0x50
                && (file[2] & 0xFF) == 0x4E
                && (file[3] & 0xFF) == 0x47;

        boolean isJpeg = file.length >= 3
                && (file[0] & 0xFF) == 0xFF
                && (file[1] & 0xFF) == 0xD8
                && (file[2] & 0xFF) == 0xFF;

        boolean isValidMime = mime != null && (
                mime.equals("image/jpeg") ||
                        mime.equals("image/jpg")  ||
                        mime.equals("image/png")
        );

        if (!isValidMime && !isPng && !isJpeg) {
            throw new IllegalArgumentException("photo.invalidtype");
        }
    }

    // ------------------------------------------------------------
    // UPLOAD
    // ------------------------------------------------------------

    public String upload(byte[] file) {
        validateImage(file);

        String key = "images/" + UUID.randomUUID();

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/jpeg")
                .build();

        s3.putObject(req, RequestBody.fromBytes(file));

        // Dynamically uses LocalStack in dev, AWS S3 URL in prod
        return s3BaseUrl + "/" + key;
    }

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------

    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        String key = extractKey(url);

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3.deleteObject(req);
    }

    private String extractKey(String url) {
        int idx = url.indexOf("/images/");
        if (idx != -1) {
            return url.substring(idx + 1); // Returns "images/<UUID>"
        }
        return url;
    }
}