package org.example.myapp.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.exception.FieldValidationException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class ImageUploadPresignService {

    private static final long MAX_FILE_SIZE_BYTES = 500L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "s3.public-url")
    String s3BaseUrl;

    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

    @Inject
    S3Client s3;

    private S3Presigner presigner;

    @PostConstruct
    void init() {
        presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

    public PresignResponseDTO presign(String contentType, Long fileSize) {
        String validatedContentType = validateImage(contentType, fileSize);
        String key = "images/" + UUID.randomUUID();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(validatedContentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(request -> request
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putRequest));

        PresignResponseDTO response = new PresignResponseDTO();
        response.uploadUrl = presigned.url().toString();
        response.fileUrl = publicUrlFor(key);
        return response;
    }

    public void verifyUploadedImage(String fileUrl, String contentType, Long fileSize) {
        String validatedContentType = validateImage(contentType, fileSize);
        String normalizedContentType = normalizeContentType(validatedContentType);
        String key = extractIssuedKey(fileUrl);

        HeadObjectResponse metadata;
        try {
            metadata = s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new FieldValidationException("fileUrl", "photo.uploadmissing");
            }
            throw e;
        }

        if (metadata.contentLength() == null || !metadata.contentLength().equals(fileSize)) {
            throw new FieldValidationException("fileSize", "photo.invalid");
        }
        if (metadata.contentType() == null
                || !normalizedContentType.equals(normalizeContentType(metadata.contentType()))) {
            throw new FieldValidationException("contentType", "photo.invalidtype");
        }

        try (ResponseInputStream<GetObjectResponse> object = s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .range("bytes=0-7")
                .build())) {
            byte[] signature = object.readNBytes(8);
            if (!matchesImageSignature(normalizedContentType, signature)) {
                throw new FieldValidationException("fileUrl", "photo.invalidtype");
            }
        } catch (IOException e) {
            throw new RuntimeException("error.internal", e);
        }
    }

    private String validateImage(String contentType, Long fileSize) {
        if (contentType == null || contentType.isBlank()
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new FieldValidationException("contentType", "photo.invalidtype");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new FieldValidationException("fileSize", "fileSize.invalid");
        }
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new FieldValidationException("fileSize", "photo.toobig");
        }
        return contentType.toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return "image/jpg".equalsIgnoreCase(contentType) ? "image/jpeg" : contentType.toLowerCase(Locale.ROOT);
    }

    private String extractIssuedKey(String fileUrl) {
        String prefix = normalizedBaseUrl() + "/images/";
        if (fileUrl == null || !fileUrl.startsWith(prefix)) {
            throw new FieldValidationException("fileUrl", "url.invalid");
        }

        String keySuffix = fileUrl.substring(prefix.length());
        try {
            if (!UUID.fromString(keySuffix).toString().equals(keySuffix)) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new FieldValidationException("fileUrl", "url.invalid");
        }
        return "images/" + keySuffix;
    }

    private String publicUrlFor(String key) {
        return normalizedBaseUrl() + "/" + key;
    }

    private String normalizedBaseUrl() {
        int end = s3BaseUrl.length();
        while (end > 0 && s3BaseUrl.charAt(end - 1) == '/') {
            end--;
        }
        return s3BaseUrl.substring(0, end);
    }

    private boolean matchesImageSignature(String contentType, byte[] bytes) {
        if ("image/png".equals(contentType)) {
            return bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;
        }
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }
}
