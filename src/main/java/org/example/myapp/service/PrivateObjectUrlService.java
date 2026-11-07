package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@ApplicationScoped
public class PrivateObjectUrlService {

    private static final Duration READ_URL_LIFETIME = Duration.ofMinutes(15);

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "s3.object-url-base")
    String objectUrlBase;

    @Inject
    S3Presigner presigner;

    public String readUrl(String objectUrl) {
        if (objectUrl == null || objectUrl.isBlank()) {
            return objectUrl;
        }

        String prefix = normalizedObjectUrlBase() + "/";
        if (!objectUrl.startsWith(prefix)) {
            return objectUrl;
        }

        String key = objectUrl.substring(prefix.length());
        if ((!key.startsWith("images/") && !key.startsWith("attachments/"))
                || key.indexOf('?') >= 0
                || key.indexOf('#') >= 0
                || key.contains("..")) {
            return objectUrl;
        }

        return presigner.presignGetObject(request -> request
                        .signatureDuration(READ_URL_LIFETIME)
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build()))
                .url()
                .toString();
    }

    private String normalizedObjectUrlBase() {
        int end = objectUrlBase.length();
        while (end > 0 && objectUrlBase.charAt(end - 1) == '/') {
            end--;
        }
        return objectUrlBase.substring(0, end);
    }
}
