package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

@ApplicationScoped
public class ImageStoreService {

    private static final long MAX_SIZE_BYTES = 500L * 1024L; // 500 KB

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

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

        if (mime == null ||
                (!mime.equals("image/jpeg") &&
                        !mime.equals("image/jpg") &&
                        !mime.equals("image/png"))) {

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
                .acl("public-read")
                .build();

        s3.putObject(req, RequestBody.fromBytes(file));

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
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
        int idx = url.indexOf(".amazonaws.com/");
        return url.substring(idx + ".amazonaws.com/".length());
    }
}