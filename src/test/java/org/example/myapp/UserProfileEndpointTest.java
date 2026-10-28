package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class UserProfileEndpointTest {

    @InjectMock
    UserService userService;

    @Test
    @TestSecurity(user = "100", roles = {"USER"})
    public void testExplicitNullOptionalFieldsArePassedForClearing() {
        User user = new User();
        user.setId(100L);
        Mockito.when(userService.updateProfile(eq(100L), any(UserDTO.class))).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"ageRange":null,"gender":null,"phone":null}
                        """)
                .when()
                .put("/users/me")
                .then()
                .statusCode(200);

        ArgumentCaptor<UserDTO> dtoCaptor = ArgumentCaptor.forClass(UserDTO.class);
        Mockito.verify(userService).updateProfile(eq(100L), dtoCaptor.capture());
        UserDTO dto = dtoCaptor.getValue();
        assertTrue(dto.isAgeRangeProvided());
        assertTrue(dto.isGenderProvided());
        assertTrue(dto.isPhoneProvided());
        assertNull(dto.getAgeRange());
        assertNull(dto.getGender());
        assertNull(dto.getPhone());
    }
}
