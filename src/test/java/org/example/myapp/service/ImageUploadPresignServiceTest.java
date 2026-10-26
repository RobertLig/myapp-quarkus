package org.example.myapp.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.exception.FieldValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ImageUploadPresignServiceTest {

    @Inject
    ImageUploadPresignService service;

    @Test
    void presignReturnsImageKeyAndUploadUrl() {
        String oldAccessKey = System.getProperty("aws.accessKeyId");
        String oldSecretKey = System.getProperty("aws.secretAccessKey");
        System.setProperty("aws.accessKeyId", "test-access-key");
        System.setProperty("aws.secretAccessKey", "test-secret-key");

        try {
            PresignResponseDTO response = service.presign("image/png", 512000L);
            assertTrue(response.uploadUrl.startsWith("http"));
            assertTrue(response.fileUrl.matches(".*/images/[0-9a-f-]{36}"));
        } finally {
            restoreSystemProperty("aws.accessKeyId", oldAccessKey);
            restoreSystemProperty("aws.secretAccessKey", oldSecretKey);
        }
    }

    @Test
    void presignRejectsUnsupportedContentType() {
        assertThrows(FieldValidationException.class, () -> service.presign("application/pdf", 100L));
    }

    @Test
    void presignRejectsImageOverLimit() {
        assertThrows(FieldValidationException.class, () -> service.presign("image/jpeg", 512001L));
    }

    private void restoreSystemProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
