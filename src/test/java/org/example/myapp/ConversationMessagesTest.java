package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ConversationMessagesTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository participantRepository;

    private Long userId;
    private Long participantConversationId;
    private Long otherConversationId;

    @BeforeEach
    @Transactional
    void setUpData() {
        User user = ensureUser(800_001L);
        User otherUser = ensureUser(800_002L);
        userId = user.getId();

        participantConversationId = createConversation(user, otherUser).getId();
        otherConversationId = createConversation(otherUser).getId();
    }

    private User ensureUser(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (?1, 'USER', false, false)")
                    .setParameter(1, id)
                    .executeUpdate();
            user = userRepository.findById(id);
        }
        return user;
    }

    private String validUserToken() {
        return "Bearer " + Jwt.issuer("your-app")
                .subject(String.valueOf(userId))
                .groups("USER")
                .expiresIn(900)
                .sign();
    }

    private Conversation createConversation(User... participants) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.USER_TO_USER);
        conversation.setCreatedAt(new Date());
        conversationRepository.persistAndFlush(conversation);

        for (User user : participants) {
            ConversationParticipant participant = new ConversationParticipant();
            participant.setUser(user);
            participant.setConversation(conversation);
            participant.setJoinedAt(new Date());
            participantRepository.persistAndFlush(participant);
        }
        return conversation;
    }

    @Test
    void testGetMessagesSuccess() {
        given()
                .header("Authorization", validUserToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", participantConversationId)
                .then()
                .statusCode(200)
                .body("messages", notNullValue())
                .body("hasMore", notNullValue());
    }

    @Test
    void testGetMessagesUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", participantConversationId)
                .then()
                .statusCode(401);
    }

    @Test
    void testGetMessagesConversationNotFound() {
        Long nonExistentConversationId = Long.MAX_VALUE;

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
        given()
                .header("Authorization", validUserToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", otherConversationId)
                .then()
                .statusCode(403);
    }

    @Test
    void testGetMessagesWithPaginationParams() {
        given()
                .header("Authorization", validUserToken())
                .queryParam("limit", 30)
                .queryParam("before", System.currentTimeMillis())
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/messages", participantConversationId)
                .then()
                .statusCode(200)
                .body("messages", notNullValue());
    }

    @Test
    void testSearchMessagesUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/conversations/{id}/search?query=test", participantConversationId)
                .then()
                .statusCode(401);
    }
}
