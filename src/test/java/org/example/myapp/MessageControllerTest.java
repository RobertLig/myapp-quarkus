package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class MessageControllerTest {

    @Test
    public void testReplyToSupport_Success() {
        // Assuming conversationId 1 exists and user 1 is an ADMIN
        String requestBody = "{ \"conversationId\": 1, \"content\": \"Hello from automated test!\" }";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer <insert-mock-or-test-admin-jwt-token>") // Or adjust auth based on your setup
                .body(requestBody)
                .when()
                .post("/support/reply")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("content", is("Hello from automated test!"));
    }

    @Test
    public void testReplyToSupport_ValidationFailure() {
        // Missing conversationId and blank content
        String requestBody = "{ \"content\": \"\" }";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer <insert-test-admin-token>")
                .body(requestBody)
                .when()
                .post("/support/reply")
                .then()
                .statusCode(400); // Bad Request from Bean Validation
    }

    @Test
    public void testReplyToSupport_ForbiddenForRegularUser() {
        String requestBody = "{ \"conversationId\": 1, \"content\": \"User trying to reply\" }";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer <insert-regular-user-token>")
                .body(requestBody)
                .when()
                .post("/support/reply")
                .then()
                .statusCode(403); // Forbidden because role is USER, not ADMIN
    }
}