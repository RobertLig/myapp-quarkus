package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.model.Message;
import org.example.myapp.repository.MessageRepository;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AttachmentPresignService {

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "s3.public-url")
    String s3BaseUrl;

    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

    @Inject
    S3Client s3;

    @Inject
    MessageRepository messageRepository;

    @Inject
    ImageLimitService imageLimitService;

    private S3Presigner presigner;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

    // Allowed MIME types for message attachments
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            // Images
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Text
            "text/plain",
            "text/csv",
            // Archives
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed"
    );

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L; // 5MB

    public PresignResponseDTO presign(String contentType, Long fileSize, Long messageId, Long currentUserId) {
        // Validate content type
        if (contentType == null || contentType.isBlank()) {
            throw new FieldValidationException("contentType", "contentType.required");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FieldValidationException("contentType", "contentType.invalid");
        }

        // Validate file size
        if (fileSize == null || fileSize <= 0) {
            throw new FieldValidationException("fileSize", "fileSize.invalid");
        }

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new FieldValidationException("fileSize", "fileSize.toobig");
        }

        // Validate message exists
        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new EntityNotFoundException("error.message.notfound");
        }

        // Limit Check
        if (!imageLimitService.canAddMessageAttachment(message)) {
            throw DomainException.tooManyRequests("error.attachment.limit");
        }

        String key = "attachments/" + UUID.randomUUID();

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putReq)
        );

        PresignResponseDTO dto = new PresignResponseDTO();
        dto.uploadUrl = presigned.url().toString();
        dto.fileUrl = s3BaseUrl + "/" + key;

        return dto;
    }
}
