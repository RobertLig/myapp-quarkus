package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.service.UserAvatarService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class UserAvatarUploadEndpointTest {

    @InjectMock
    UserAvatarService avatarService;

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresignAvatarSuccess() {
        PresignResponseDTO response = new PresignResponseDTO();
        response.uploadUrl = "https://bucket.s3.eu-central-1.amazonaws.com/images/uuid?signature";
        response.fileUrl = "https://bucket.s3.eu-central-1.amazonaws.com/images/uuid";
        Mockito.when(avatarService.presignAvatar(100L, "image/png", 100L)).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"contentType":"image/png","fileSize":100}
                        """)
                .when()
                .post("/users/me/avatar/presign")
                .then()
                .statusCode(200)
                .body("uploadUrl", notNullValue())
                .body("fileUrl", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testCompleteAvatarUploadSuccess() {
        Mockito.when(avatarService.completeAvatarUpload(eq(100L), any()))
                .thenReturn("https://bucket.s3.eu-central-1.amazonaws.com/images/uuid");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"fileUrl":"https://bucket.s3.eu-central-1.amazonaws.com/images/uuid","contentType":"image/png","fileSize":100}
                        """)
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(200)
                .body("avatarUrl", equalTo("https://bucket.s3.eu-central-1.amazonaws.com/images/uuid"));
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresignAvatarRejectsInvalidContentType() {
        Mockito.when(avatarService.presignAvatar(100L, "application/exe", 100L))
                .thenThrow(new FieldValidationException("contentType", "photo.invalidtype"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"contentType":"application/exe","fileSize":100}
                        """)
                .when()
                .post("/users/me/avatar/presign")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    public void testAvatarPresignUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"contentType":"image/png","fileSize":100}
                        """)
                .when()
                .post("/users/me/avatar/presign")
                .then()
                .statusCode(401);
    }
}
