package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.MessageEventRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.EventType;
import org.example.myapp.model.Message;
import org.example.myapp.model.MessageEvent;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.service.MessageEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class MessageEventControllerTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    @Inject
    MessageRepository messageRepository;

    @InjectMock
    MessageEventService eventService;

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
    public void testCreateEvent_Read_Success() {
        MessageEvent mockEvent = new MessageEvent();
        mockEvent.setType(EventType.READ);
        mockEvent.setUser(userRepository.findById(2L));
        mockEvent.setMessage(messageRepository.findById(testMessageId));
        mockEvent.setCreatedAt(new Date());

        Mockito.when(eventService.createEvent(any(MessageEventRequest.class), eq(2L)))
                .thenReturn(mockEvent);

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"messageId\": " + testMessageId + 
                ", \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("type", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_Delivered_Success() {
        MessageEvent mockEvent = new MessageEvent();
        mockEvent.setType(EventType.DELIVERED);
        mockEvent.setUser(userRepository.findById(2L));
        mockEvent.setMessage(messageRepository.findById(testMessageId));
        mockEvent.setCreatedAt(new Date());

        Mockito.when(eventService.createEvent(any(MessageEventRequest.class), eq(2L)))
                .thenReturn(mockEvent);

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"messageId\": " + testMessageId + 
                ", \"type\": \"DELIVERED\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("type", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_TypingStart_Success() {
        MessageEvent mockEvent = new MessageEvent();
        mockEvent.setType(EventType.TYPING_START);
        mockEvent.setUser(userRepository.findById(2L));
        mockEvent.setCreatedAt(new Date());

        Mockito.when(eventService.createEvent(any(MessageEventRequest.class), eq(2L)))
                .thenReturn(mockEvent);

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"TYPING_START\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("type", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_ConversationNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.conversation.notfound"))
                .when(eventService).createEvent(any(MessageEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": 99999, \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_UserNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.user.notfound"))
                .when(eventService).createEvent(any(MessageEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "999", roles = {"USER"})
    public void testCreateEvent_UserNotParticipant() {
        Mockito.doThrow(new DomainException("error.message.notparticipant", 403))
                .when(eventService).createEvent(any(MessageEventRequest.class), eq(999L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(403)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_MessageNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.message.notfound"))
                .when(eventService).createEvent(any(MessageEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"messageId\": 99999, \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testCreateEvent_MessageNotInConversation() {
        Mockito.doThrow(new DomainException("error.message.notinconversation", 400))
                .when(eventService).createEvent(any(MessageEventRequest.class), eq(2L));

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"messageId\": 99999, \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"ADMIN"})
    public void testCreateEvent_AdminCanCreate() {
        MessageEvent mockEvent = new MessageEvent();
        mockEvent.setType(EventType.READ);
        mockEvent.setUser(userRepository.findById(2L));
        mockEvent.setMessage(messageRepository.findById(testMessageId));
        mockEvent.setCreatedAt(new Date());

        Mockito.when(eventService.createEvent(any(MessageEventRequest.class), eq(2L)))
                .thenReturn(mockEvent);

        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"messageId\": " + testMessageId + 
                ", \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateEvent_Unauthorized() {
        String requestBody = "{ \"conversationId\": " + testConversationId + 
                ", \"type\": \"READ\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .statusCode(401);
    }

}
