package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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
import org.example.myapp.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class MessageAttachmentControllerTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    @Inject
    MessageRepository messageRepository;

    @InjectMock
    AttachmentService attachmentService;

    private Long testUserId;
    private Long testOtherUserId;
    private Long testConversationId;
    private Long testMessageId;

    @BeforeEach
    @Transactional
    void setUpData() {
        // Ensure test user with ID 2 exists as USER
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

        // Create test conversation
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
    @TestSecurity(user = "2", roles = {"USER"})
    public void testAddAttachment_Success() {
        Mockito.doNothing().when(attachmentService).addAttachment(eq(testMessageId), any(), eq(2L));

        String requestBody = "{ \"messageId\": " + testMessageId + ", \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"image/png\", \"size\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testAddAttachment_MessageNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.message.notfound"))
                .when(attachmentService).addAttachment(eq(99999L), any(), eq(2L));

        String requestBody = "{ \"messageId\": 99999, \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"image/png\", \"size\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testAddAttachment_UserNotParticipant() {
        Mockito.doThrow(new DomainException("error.message.notparticipant", 403))
                .when(attachmentService).addAttachment(eq(testMessageId), any(), eq(999L));

        String requestBody = "{ \"messageId\": " + testMessageId + ", \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"image/png\", \"size\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(403)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testAddAttachment_LimitExceeded() {
        Mockito.doThrow(new DomainException("error.attachment.limit", 429))
                .when(attachmentService).addAttachment(eq(testMessageId), any(), eq(2L));

        String requestBody = "{ \"messageId\": " + testMessageId + ", \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"image/png\", \"size\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(429)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"ADMIN"})
    public void testAddAttachment_AdminCanAdd() {
        Mockito.doNothing().when(attachmentService).addAttachment(eq(testMessageId), any(), eq(2L));

        String requestBody = "{ \"messageId\": " + testMessageId + ", \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"application/pdf\", \"size\": 2048000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(200);
    }

    @Test
    public void testAddAttachment_Unauthorized() {
        String requestBody = "{ \"messageId\": " + testMessageId + ", \"url\": \"https://bucket.s3.amazonaws.com/attachments/uuid\", \"type\": \"image/png\", \"size\": 1024000 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/messages/attachments")
                .then()
                .statusCode(401);
    }
}
