package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.exception.EntityNotFoundException;
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

    // PNG Header (Magic Bytes: 0x89, 'P', 'N', 'G')
    private static final byte[] VALID_PNG_BYTES = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testUploadAvatarSuccess() {
        Mockito.when(avatarService.uploadAvatar(eq(100L), any()))
                .thenReturn("http://localhost:4566/bucket/images/avatar123.jpg");

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("file", "avatar.png", VALID_PNG_BYTES, "image/png")
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(200)
                .body("avatarUrl", equalTo("http://localhost:4566/bucket/images/avatar123.jpg"));
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testUploadAvatarUserNotFound() {
        Mockito.when(avatarService.uploadAvatar(eq(100L), any()))
                .thenThrow(new EntityNotFoundException("error.user.notfound"));

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("file", "avatar.png", VALID_PNG_BYTES, "image/png")
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(404)
                .body("error", notNullValue()); // Triggers EntityNotFoundExceptionMapper
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testUploadAvatarLimitExceeded() {
        Mockito.when(avatarService.uploadAvatar(eq(100L), any()))
                .thenThrow(new FieldValidationException("avatar", "avatar.limit"));

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("file", "avatar.png", VALID_PNG_BYTES, "image/png")
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("details.avatar", notNullValue()); // Triggers FieldValidationExceptionMapper
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testUploadAvatarInternalServerError() {
        Mockito.when(avatarService.uploadAvatar(eq(100L), any()))
                .thenThrow(new RuntimeException("error.internal"));

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("file", "avatar.png", VALID_PNG_BYTES, "image/png")
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(500)
                .body("error", notNullValue())
                .body("message", equalTo("error.internal")); // Triggers GlobalExceptionMapper
    }

    @Test
    public void testUploadAvatarUnauthorized() {
        // Unauthenticated request should yield 401 Unauthorized directly from Quarkus Security
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("file", "avatar.png", VALID_PNG_BYTES, "image/png")
                .when()
                .post("/users/me/avatar")
                .then()
                .statusCode(401);
    }
}