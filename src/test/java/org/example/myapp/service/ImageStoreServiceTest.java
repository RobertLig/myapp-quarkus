package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.exception.FieldValidationException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class ImageStoreServiceTest {

    @Inject
    ImageStoreService imageStoreService;

    @InjectMock
    S3Client s3Client; // Mocks out the S3 network call during validation tests

    @Test
    public void testValidateImageEmptyFile() {
        FieldValidationException ex = assertThrows(FieldValidationException.class, () ->
                imageStoreService.validateAndDetectMime(new byte[0])
        );
        assertEquals("photo.empty", ex.getMessageKey());
    }

    @Test
    public void testValidateImageExceedsMaxSize() {
        byte[] oversizedFile = new byte[(500 * 1024) + 1]; // 500KB + 1 byte
        FieldValidationException ex = assertThrows(FieldValidationException.class, () ->
                imageStoreService.validateAndDetectMime(oversizedFile)
        );
        assertEquals("photo.toobig", ex.getMessageKey());
    }

    @Test
    public void testValidateImageInvalidFileType() {
        byte[] plainTextBytes = "This is not an image".getBytes();
        FieldValidationException ex = assertThrows(FieldValidationException.class, () ->
                imageStoreService.validateAndDetectMime(plainTextBytes)
        );
        assertEquals("photo.invalidtype", ex.getMessageKey());
    }

    @Test
    public void testValidateValidJpegMagicBytes() {
        // Valid JPEG Magic Bytes: 0xFF 0xD8 0xFF
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        assertDoesNotThrow(() -> imageStoreService.validateAndDetectMime(jpegHeader));
    }

    @Test
    public void testValidateValidPngMagicBytes() {
        // Valid PNG Magic Bytes: 0x89 0x50 0x4E 0x47
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertDoesNotThrow(() -> imageStoreService.validateAndDetectMime(pngHeader));
    }
}