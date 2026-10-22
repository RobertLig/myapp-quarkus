package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ConversationControllerTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    private Long testUserId;
    private Long testConversationId;

    @BeforeEach
    @Transactional
    void setUpData() {
        // Ensure test user with ID 1 exists and has correct role
        User user = userRepository.findById(1L);
        if (user == null) {
            // Use native SQL to insert with explicit ID and all required fields
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (1, 'USER', false, false)")
                    .executeUpdate();
            user = userRepository.findById(1L);
        } else {
            user.setRole(Role.USER);
        }
        testUserId = 1L;

        // Ensure test user with ID 999 exists for the "not participant" test
        User user999 = userRepository.findById(999L);
        if (user999 == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (999, 'USER', false, false)")
                    .executeUpdate();
        }

        // Create test conversation
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.USER_TO_USER);
        conversation.setCreatedAt(new Date());
        conversationRepository.persistAndFlush(conversation);
        testConversationId = conversation.getId();

        // Add user as participant
        ConversationParticipant participant = new ConversationParticipant();
        participant.setUser(user);
        participant.setConversation(conversation);
        participant.setJoinedAt(new Date());
        conversationParticipantRepository.persistAndFlush(participant);
    }

    @Test
    @TestSecurity(user = "1", roles = {"USER"})
    public void testDeleteConversation_Success() {
        given()
                .pathParam("conversationId", testConversationId)
                .when()
                .delete("/conversations/{conversationId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "1", roles = {"ADMIN"})
    public void testDeleteConversation_AdminCanDelete() {
        given()
                .pathParam("conversationId", testConversationId)
                .when()
                .delete("/conversations/{conversationId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "1", roles = {"USER"})
    public void testDeleteConversation_ConversationNotFound() {
        Long nonExistentConversationId = 99999L;

        given()
                .pathParam("conversationId", nonExistentConversationId)
                .when()
                .delete("/conversations/{conversationId}")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testDeleteConversation_UserNotParticipant() {
        given()
                .pathParam("conversationId", testConversationId)
                .when()
                .delete("/conversations/{conversationId}")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "1", roles = {"USER"})
    public void testRestoreConversation_Success() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("conversationId", testConversationId)
                .when()
                .post("/conversations/{conversationId}/restore")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "1", roles = {"ADMIN"})
    public void testRestoreConversation_AdminCanRestore() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("conversationId", testConversationId)
                .when()
                .post("/conversations/{conversationId}/restore")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "1", roles = {"USER"})
    public void testRestoreConversation_ConversationNotFound() {
        Long nonExistentConversationId = 99999L;

        given()
                .contentType(ContentType.JSON)
                .pathParam("conversationId", nonExistentConversationId)
                .when()
                .post("/conversations/{conversationId}/restore")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testRestoreConversation_UserNotParticipant() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("conversationId", testConversationId)
                .when()
                .post("/conversations/{conversationId}/restore")
                .then()
                .statusCode(403);
    }
}
