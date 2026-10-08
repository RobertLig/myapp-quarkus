package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.service.AnnouncementPhotoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class AnnouncementPhotoSortEndpointTest {

    @InjectMock
    AnnouncementPhotoService photoService;

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testSortPhotosSuccess() {
        PhotoDTO p1 = new PhotoDTO();
        p1.id = 30L;
        p1.position = 0;

        PhotoDTO p2 = new PhotoDTO();
        p2.id = 10L;
        p2.position = 1;

        Mockito.when(photoService.sortPhotos(eq(1L), eq(100L), anyList()))
                .thenReturn(List.of(p1, p2));

        String jsonBody = """
            [
                {"id": 30},
                {"id": 10}
            ]
            """;

        given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .put("/announcements/1/photos/sort")
                .then()
                .statusCode(200)
                .body("[0].id", equalTo(30))
                .body("[0].position", equalTo(0))
                .body("[1].id", equalTo(10))
                .body("[1].position", equalTo(1));
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testSortPhotosNotFound() {
        Mockito.when(photoService.sortPhotos(eq(999L), eq(100L), anyList()))
                .thenThrow(new EntityNotFoundException("error.announcement.notfound"));

        given()
                .contentType(ContentType.JSON)
                .body("[{\"id\": 10}]")
                .when()
                .put("/announcements/999/photos/sort")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testSortPhotosForbidden() {
        Mockito.when(photoService.sortPhotos(eq(1L), eq(100L), anyList()))
                .thenThrow(DomainException.forbidden("error.unauthorized.access"));

        given()
                .contentType(ContentType.JSON)
                .body("[{\"id\": 10}]")
                .when()
                .put("/announcements/1/photos/sort")
                .then()
                .statusCode(403)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testSortPhotosValidationMismatch() {
        Mockito.when(photoService.sortPhotos(eq(1L), eq(100L), anyList()))
                .thenThrow(new FieldValidationException("photos", "photo.mismatch"));

        given()
                .contentType(ContentType.JSON)
                .body("[{\"id\": 10}]")
                .when()
                .put("/announcements/1/photos/sort")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("details.photos", notNullValue());
    }

    @Test
    public void testSortPhotosUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("[{\"id\": 10}]")
                .when()
                .put("/announcements/1/photos/sort")
                .then()
                .statusCode(401);
    }
}