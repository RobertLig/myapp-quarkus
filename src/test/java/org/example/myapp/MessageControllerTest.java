package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.Message;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class MessageControllerTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    @Inject
    MessageRepository messageRepository;

    private Long testUserId;
    private Long testOtherUserId;
    private Long testConversationId;
    private Long testMessageId;

    @BeforeEach
    @Transactional
    void setUpData() {
        // Ensure test user with ID 1 exists as ADMIN (for existing replyToSupport tests)
        User admin = userRepository.findById(1L);
        if (admin == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (1, 'ADMIN', false, false)")
                    .executeUpdate();
            admin = userRepository.findById(1L);
        } else {
            admin.setRole(Role.ADMIN);
        }

        // Ensure test user with ID 2 exists as USER for deleteMessage tests
        User user2 = userRepository.findById(2L);
        if (user2 == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (2, 'USER', false, false)")
                    .executeUpdate();
            user2 = userRepository.findById(2L);
        } else {
            user2.setRole(Role.USER);
        }
        testUserId = 2L;

        // Ensure test user with ID 3 exists for the "other participant" test
        User user3 = userRepository.findById(3L);
        if (user3 == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (3, 'USER', false, false)")
                    .executeUpdate();
            user3 = userRepository.findById(3L);
        }
        testOtherUserId = 3L;

        // Ensure test user with ID 999 exists for the "not participant" test
        User user999 = userRepository.findById(999L);
        if (user999 == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (999, 'USER', false, false)")
                    .executeUpdate();
        }

        // Create USER_TO_ADMIN conversation for existing replyToSupport tests
        Conversation supportConv = conversationRepository.findById(1L);
        if (supportConv == null) {
            supportConv = new Conversation();
            supportConv.setType(ConversationType.USER_TO_ADMIN);
            supportConv.setCreatedAt(new Date());
            conversationRepository.persistAndFlush(supportConv);
        }

        // Add admin as participant to support conversation
        boolean adminInSupportConv = supportConv.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(1L));
        if (!adminInSupportConv) {
            ConversationParticipant supportParticipant = new ConversationParticipant();
            supportParticipant.setUser(admin);
            supportParticipant.setConversation(supportConv);
            supportParticipant.setJoinedAt(new Date());
            conversationParticipantRepository.persistAndFlush(supportParticipant);
        }

        // Add user 2 as participant to support conversation (so admin can reply to them)
        boolean user2InSupportConv = supportConv.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(2L));
        if (!user2InSupportConv) {
            ConversationParticipant supportParticipant2 = new ConversationParticipant();
            supportParticipant2.setUser(user2);
            supportParticipant2.setConversation(supportConv);
            supportParticipant2.setJoinedAt(new Date());
            conversationParticipantRepository.persistAndFlush(supportParticipant2);
        }

        // Create test conversation for deleteMessage tests
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.USER_TO_USER);
        conversation.setCreatedAt(new Date());
        conversationRepository.persistAndFlush(conversation);
        testConversationId = conversation.getId();

        // Add user 2 as participant
        ConversationParticipant participant1 = new ConversationParticipant();
        participant1.setUser(user2);
        participant1.setConversation(conversation);
        participant1.setJoinedAt(new Date());
        conversationParticipantRepository.persistAndFlush(participant1);

        // Add user 3 as participant
        ConversationParticipant participant2 = new ConversationParticipant();
        participant2.setUser(user3);
        participant2.setConversation(conversation);
        participant2.setJoinedAt(new Date());
        conversationParticipantRepository.persistAndFlush(participant2);

        // Add admin as participant for admin delete test
        ConversationParticipant participantAdmin = new ConversationParticipant();
        participantAdmin.setUser(admin);
        participantAdmin.setConversation(conversation);
        participantAdmin.setJoinedAt(new Date());
        conversationParticipantRepository.persistAndFlush(participantAdmin);

        // Create test message from user 2
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(user2);
        message.setContent("Test message");
        message.setCreatedAt(new Date());
        messageRepository.persistAndFlush(message);
        testMessageId = message.getId();
    }

    @Test
    @TestSecurity(user = "1", roles = {"ADMIN"})
    public void testReplyToSupport_Success() {
        String requestBody = "{ \"conversationId\": 1, \"content\": \"Hello from automated test!\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/support/reply")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("content", is("Hello from automated test!"));
    }

    @Test
    @TestSecurity(user = "1", roles = {"ADMIN"})
    public void testReplyToSupport_ValidationFailure() {
        String requestBody = "{ \"content\": \"\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/support/reply")
                .then()
                .statusCode(400); // Bad Request from Bean Validation
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testReplyToSupport_ForbiddenForRegularUser() {
        String requestBody = "{ \"conversationId\": 1, \"content\": \"User trying to reply\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/support/reply")
                .then()
                .statusCode(403); // Forbidden because role is USER, not ADMIN
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testDeleteMessage_SenderDeletes() {
        given()
                .pathParam("messageId", testMessageId)
                .when()
                .delete("/messages/{messageId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "3", roles = {"USER"})
    public void testDeleteMessage_RecipientDeletes() {
        given()
                .pathParam("messageId", testMessageId)
                .when()
                .delete("/messages/{messageId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "1", roles = {"ADMIN"})
    public void testDeleteMessage_AdminCanDelete() {
        given()
                .pathParam("messageId", testMessageId)
                .when()
                .delete("/messages/{messageId}")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testDeleteMessage_MessageNotFound() {
        Long nonExistentMessageId = 99999L;

        given()
                .pathParam("messageId", nonExistentMessageId)
                .when()
                .delete("/messages/{messageId}")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testDeleteMessage_UserNotParticipant() {
        given()
                .pathParam("messageId", testMessageId)
                .when()
                .delete("/messages/{messageId}")
                .then()
                .statusCode(403);
    }
}