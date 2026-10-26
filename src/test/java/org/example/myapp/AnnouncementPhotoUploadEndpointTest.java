package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.service.AnnouncementPhotoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class AnnouncementPhotoUploadEndpointTest {

    @InjectMock
    AnnouncementPhotoService photoService;

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testPresignPhotoSuccess() {
        PresignResponseDTO response = new PresignResponseDTO();
        response.uploadUrl = "https://bucket.s3.eu-central-1.amazonaws.com/images/uuid?signature";
        response.fileUrl = "https://bucket.s3.eu-central-1.amazonaws.com/images/uuid";
        Mockito.when(photoService.presignPhoto(1L, 100L, "image/jpeg", 100L)).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"contentType":"image/jpeg","fileSize":100}
                        """)
                .when()
                .post("/announcements/1/photos/presign")
                .then()
                .statusCode(200)
                .body("uploadUrl", notNullValue())
                .body("fileUrl", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testCompletePhotoUploadSuccess() {
        PhotoDTO photo = new PhotoDTO();
        photo.id = 5L;
        photo.url = "https://bucket.s3.eu-central-1.amazonaws.com/images/uuid";
        Mockito.when(photoService.completePhotoUpload(eq(1L), eq(100L), any())).thenReturn(photo);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"fileUrl":"https://bucket.s3.eu-central-1.amazonaws.com/images/uuid","contentType":"image/jpeg","fileSize":100}
                        """)
                .when()
                .post("/announcements/1/photos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("url", notNullValue());
    }

    @Test
    public void testPhotoPresignUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"contentType":"image/jpeg","fileSize":100}
                        """)
                .when()
                .post("/announcements/1/photos/presign")
                .then()
                .statusCode(401);
    }
}
