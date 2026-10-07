package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.myapp.exception.FieldValidationException;
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

    public String validateAndDetectMime(byte[] file) {
        if (file == null || file.length == 0) {
            throw new FieldValidationException("photo", "photo.empty");
        }

        if (file.length > MAX_SIZE_BYTES) {
            throw new FieldValidationException("photo", "photo.toobig");
        }

        String mime;
        try {
            mime = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(file));
        } catch (IOException e) {
            mime = null;
        }

        boolean isPng = file.length >= 8
                && (file[0] & 0xFF) == 0x89
                && (file[1] & 0xFF) == 0x50
                && (file[2] & 0xFF) == 0x4E
                && (file[3] & 0xFF) == 0x47;

        boolean isJpeg = file.length >= 3
                && (file[0] & 0xFF) == 0xFF
                && (file[1] & 0xFF) == 0xD8
                && (file[2] & 0xFF) == 0xFF;

        if ("image/png".equals(mime) || isPng) {
            return "image/png";
        } else if ("image/jpeg".equals(mime) || "image/jpg".equals(mime) || isJpeg) {
            return "image/jpeg";
        }

        throw new FieldValidationException("photo", "photo.invalidtype");
    }

    public String upload(byte[] file) {
        String mimeType = validateAndDetectMime(file);
        String key = "images/" + UUID.randomUUID();

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(mimeType)
                .build();

        s3.putObject(req, RequestBody.fromBytes(file));
        return s3BaseUrl + "/" + key;
    }

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
            return url.substring(idx + 1);
        }
        return url;
    }
}