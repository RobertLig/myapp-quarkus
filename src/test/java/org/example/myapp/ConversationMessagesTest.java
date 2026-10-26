package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ConversationMessagesTest {

    private String validUserToken() {
        return "Bearer " + Jwt.issuer("your-app")
                .subject("1")
                .groups("USER")
                .expiresIn(900)
                .sign();
    }

    @Test
    void testGetMessagesSuccess() {
        Long validConversationId = 1L; // Ensure this conversation exists and the token user is a participant

        given()
                .header("Authorization", validUserToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", validConversationId)
                .then()
                .statusCode(200)
                .body("messages", notNullValue())
                .body("hasMore", notNullValue());
    }

    @Test
    void testGetMessagesUnauthorized() {
        Long conversationId = 1L;

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", conversationId)
                .then()
                .statusCode(401);
    }

    @Test
    void testGetMessagesConversationNotFound() {
        Long nonExistentConversationId = 99999L;

        given()
                .header("Authorization", validUserToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", nonExistentConversationId)
                .then()
                .statusCode(404);
    }

    @Test
    void testGetMessagesForbiddenNotParticipant() {
        Long conversationIdWithoutUser = 2L;

        given()
                .header("Authorization", validUserToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", conversationIdWithoutUser)
                .then()
                .statusCode(403);
    }

    @Test
    void testGetMessagesWithPaginationParams() {
        Long validConversationId = 1L;

        given()
                .header("Authorization", validUserToken())
                .queryParam("limit", 30)
                .queryParam("before", System.currentTimeMillis())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", validConversationId)
                .then()
                .statusCode(200)
                .body("messages", notNullValue());
    }

    @Test
    void testSearchMessagesUnauthorized() {
        Long conversationId = 1L;

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/search?query=test", conversationId)
                .then()
                .statusCode(401);
    }
}
