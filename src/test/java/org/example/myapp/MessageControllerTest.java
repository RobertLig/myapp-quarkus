package org.example.myapp;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
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

    @BeforeEach
    @Transactional
    void setUpData() {
        // Clear any old data or check safely
        User admin = userRepository.findById(1L);
        if (admin == null) {
            admin = new User();
            // If your ID field uses @GeneratedValue(strategy = GenerationType.IDENTITY),
            // JPA usually ignores assigned IDs. If you can assign IDs manually, do:
            // admin.setId(1L);
            admin.setRole(Role.ADMIN);
            userRepository.persistAndFlush(admin);
        } else {
            // Ensure role is ADMIN even if user already existed
            admin.setRole(Role.ADMIN);
            userRepository.persistAndFlush(admin);
        }

        Conversation conv = conversationRepository.findById(1L);
        if (conv == null) {
            conv = new Conversation();
            // conv.setId(1L); // Uncomment if your ID allows manual assignment
            conv.setType(ConversationType.USER_TO_ADMIN);
            conv.setCreatedAt(new Date());
            conversationRepository.persistAndFlush(conv);
        }
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
}