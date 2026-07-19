package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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
public class ImageService {

    private static final long MAX_SIZE_BYTES = 500L * 1024L; // 500 KB

    private final String bucketName = "your-bucket-name"; // change this
    private final Region region = Region.EU_CENTRAL_1;     // change if needed

    private final S3Client s3 = S3Client.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    // ------------------------------------------------------------
    // VALIDATION
    // ------------------------------------------------------------

    public void validateImage(byte[] file) {

        if (file == null || file.length == 0) {
            throw new IllegalArgumentException("image.empty");
        }

        if (file.length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("image.toobig");
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

            throw new IllegalArgumentException("image.invalidtype");
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
                .contentType("image/jpeg") // safe default
                .acl("public-read")        // allow public access
                .build();

        s3.putObject(req, RequestBody.fromBytes(file));

        return "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;
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
