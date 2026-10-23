package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.BlockUserRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.Role;
import org.example.myapp.model.User;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.service.BlockUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class BlockUserControllerTest {

    @Inject
    UserRepository userRepository;

    @InjectMock
    BlockUserService blockUserService;

    private Long testUserId;
    private Long testOtherUserId;

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

        // Ensure test user with ID 3 exists for the blocked user
        User user3 = userRepository.findById(3L);
        if (user3 == null) {
            userRepository.getEntityManager()
                    .createNativeQuery("INSERT INTO users (id, role, emailverified, termsaccepted) VALUES (3, 'USER', false, false)")
                    .executeUpdate();
            user3 = userRepository.findById(3L);
        }
        testOtherUserId = 3L;
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testBlock_Success() {
        Mockito.doNothing().when(blockUserService).block(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testBlock_SelfBlock() {
        Mockito.doThrow(new DomainException("error.block.self", 403))
                .when(blockUserService).block(eq(2L), any());

        String requestBody = "{ \"blockedId\": 2 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(403)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testBlock_UserNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.user.notfound"))
                .when(blockUserService).block(eq(2L), any());

        String requestBody = "{ \"blockedId\": 99999 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testBlock_AlreadyBlocked() {
        Mockito.doThrow(new DomainException("error.block.exists", 400))
                .when(blockUserService).block(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"ADMIN"})
    public void testBlock_AdminCanBlock() {
        Mockito.doNothing().when(blockUserService).block(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(200);
    }

    @Test
    public void testBlock_Unauthorized() {
        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testBlock_ValidationError() {
        String requestBody = "{}";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/block")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testUnblock_Success() {
        Mockito.doNothing().when(blockUserService).unblock(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/unblock")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testUnblock_BlockNotFound() {
        Mockito.doThrow(new EntityNotFoundException("error.block.notfound"))
                .when(blockUserService).unblock(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/unblock")
                .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "2", roles = {"ADMIN"})
    public void testUnblock_AdminCanUnblock() {
        Mockito.doNothing().when(blockUserService).unblock(eq(2L), any());

        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/unblock")
                .then()
                .statusCode(200);
    }

    @Test
    public void testUnblock_Unauthorized() {
        String requestBody = "{ \"blockedId\": " + testOtherUserId + " }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/unblock")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "2", roles = {"USER"})
    public void testUnblock_ValidationError() {
        String requestBody = "{}";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/unblock")
                .then()
                .statusCode(400);
    }
}
