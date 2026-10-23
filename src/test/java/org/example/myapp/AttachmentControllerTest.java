package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.service.AttachmentPresignService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class AttachmentControllerTest {

    @InjectMock
    AttachmentPresignService presignService;

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresign_Success() {
        PresignResponseDTO mockResponse = new PresignResponseDTO();
        mockResponse.uploadUrl = "https://bucket.s3.eu-central-1.amazonaws.com/attachments/uuid?X-Amz-Signature=signature";
        mockResponse.fileUrl = "https://bucket.s3.eu-central-1.amazonaws.com/attachments/uuid";

        Mockito.when(presignService.presign(eq("image/png"), eq(1024000L), eq(100L)))
                .thenReturn(mockResponse);

        String requestBody = "{ \"contentType\": \"image/png\", \"fileSize\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/attachments/presign")
                .then()
                .statusCode(200)
                .body("uploadUrl", notNullValue())
                .body("fileUrl", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresign_InvalidContentType() {
        Mockito.when(presignService.presign(eq("application/exe"), eq(1024000L), eq(100L)))
                .thenThrow(new FieldValidationException("contentType", "contentType.invalid"));

        String requestBody = "{ \"contentType\": \"application/exe\", \"fileSize\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/attachments/presign")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("details.contentType", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresign_FileSizeTooBig() {
        Mockito.when(presignService.presign(eq("image/png"), eq(15L * 1024L * 1024L), eq(100L)))
                .thenThrow(new FieldValidationException("fileSize", "fileSize.toobig"));

        String requestBody = "{ \"contentType\": \"image/png\", \"fileSize\": 15728640 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/attachments/presign")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("details.fileSize", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"ADMIN"})
    public void testPresign_AdminCanPresign() {
        PresignResponseDTO mockResponse = new PresignResponseDTO();
        mockResponse.uploadUrl = "https://bucket.s3.eu-central-1.amazonaws.com/attachments/uuid?X-Amz-Signature=signature";
        mockResponse.fileUrl = "https://bucket.s3.eu-central-1.amazonaws.com/attachments/uuid";

        Mockito.when(presignService.presign(eq("application/pdf"), eq(2048000L), eq(100L)))
                .thenReturn(mockResponse);

        String requestBody = "{ \"contentType\": \"application/pdf\", \"fileSize\": 2048000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/attachments/presign")
                .then()
                .statusCode(200)
                .body("uploadUrl", notNullValue())
                .body("fileUrl", notNullValue());
    }

    @Test
    public void testPresign_Unauthorized() {
        String requestBody = "{ \"contentType\": \"image/png\", \"fileSize\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/attachments/presign")
                .then()
                .statusCode(401);
    }
}
