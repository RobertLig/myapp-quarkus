package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.EventType;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.service.TypingEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class TypingEventControllerTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    @InjectMock
    TypingEventService typingEventService;

    private Long testUserId;
    private Long testOtherUserId;
    private Long testConversationId;

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
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_TypingStart_Success() {
        Mockito.doNothing()
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_TypingStop_Success() {
        Mockito.doNothing()
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_STOP\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_InvalidEventType() {
        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"SENT\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_MissingConversationId() {
        String requestBody = "{ \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_MissingType() {
        String requestBody = "{ \"conversationId\": " + testConversationId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_ConversationNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.conversation.notfound"))
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": 99999, \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testTypingEvent_UserNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.user.notfound"))
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testTypingEvent_UserNotParticipant() {
        Mockito.doThrow(new DomainException("error.message.notparticipant", 403))
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(999L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(403)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"ADMIN"})
    public void testTypingEvent_AdminCanCreate() {
        Mockito.doNothing()
                .when(typingEventService).handleTypingEvent(any(TypingEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(200);
    }

    @Test
    public void testTypingEvent_Unauthorized() {
        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/typing")
                .then()
                .statusCode(401);
    }

}
