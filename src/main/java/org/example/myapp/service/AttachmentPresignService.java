package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.dto.PresignResponseDTO;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class AttachmentPresignService {

    private final String bucketName = "your-bucket-name"; // same bucket or a separate one
    private final Region region = Region.EU_CENTRAL_1;

    private final S3Presigner presigner = S3Presigner.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    public PresignResponseDTO presign(String contentType) {

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
        dto.fileUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + key;

        return dto;
    }
}
