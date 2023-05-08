package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.component.JWTUtils;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LoginDto;
import hexlet.code.entity.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_EMAIL_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_FIRSTNAME_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_LASTNAME_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_PASSWORD_USER;
import static hexlet.code.utils.TestUtils.SECOND_USER;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.getInfoFromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@Transactional
public class UserControllerTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUtils utils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtils jwtUtils;

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void testLogin() throws Exception {
        utils.createNewUser(FIRST_USER);

        final LoginDto loginDto = new LoginDto(
                FIRST_USER.getEmail(),
                FIRST_USER.getPassword()
        );
        final String expectedToken = jwtUtils.createJWSToken(Map.of("username", FIRST_USER.getEmail()));

        final var loginRequest = post(LOGIN)
                .content(asJson(loginDto))
                .contentType(APPLICATION_JSON);

        final var response =  utils.performUnauthorizedRequest(loginRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertEquals(expectedToken, response.getContentAsString().trim());
    }

    @Test
    public void testLoginFail() throws Exception {
        final LoginDto loginDto = new LoginDto(
                FIRST_USER.getEmail(),
                FIRST_USER.getPassword()
        );

        final var loginRequest = post(LOGIN)
                .content(asJson(loginDto))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(loginRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateUser() throws Exception {
        assertEquals(0, userRepository.count());

        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(asJson(FIRST_USER))
                .contentType(APPLICATION_JSON);

        final var response = utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
        assertEquals(1, userRepository.count());

        final User createdUser = utils.getUserByEmail(FIRST_USER.getEmail());
        assertNotEquals(FIRST_USER.getPassword(), createdUser.getPassword());
        assertTrue(passwordEncoder.matches(FIRST_USER.getPassword(), createdUser.getPassword()));
    }

    @Test
    public void testCreateUserWithNotValidEmail() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(asJson(NOT_VALID_EMAIL_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidFirstName() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(asJson(NOT_VALID_FIRSTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidLastName() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(asJson(NOT_VALID_LASTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidPassword() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(asJson(NOT_VALID_PASSWORD_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testTwiceCreateTheSameUserFail() throws Exception {
        utils.createNewUser(FIRST_USER)
                .andExpect(status().isCreated());
        utils.createNewUser(FIRST_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(1, userRepository.count());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        utils.createNewUser(FIRST_USER);
        utils.createNewUser(SECOND_USER);

        final var getRequest = get(USER_CONTROLLER_PATH);

        final var response = utils.performUnauthorizedRequest(getRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
        assertFalse(response.getContentAsString().contains(SECOND_USER.getPassword()));

        final List<User> users = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
        assertThat(users).hasSize(2);
    }

    @Test
    public void testGetUserById() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final User expectedUser = utils.getUserByEmail(email);
        final Long expectedUserId = expectedUser.getId();

        final var getRequest = get(USER_CONTROLLER_PATH + ID, expectedUserId);

        final var response = utils.performAuthorizedRequest(getRequest, email)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final User actualUser = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(), actualUser.getLastName());
        assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
    }

    @Test
    public void testUpdateUser() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(SECOND_USER))
                .contentType(APPLICATION_JSON);

        final var response = utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(SECOND_USER.getPassword()));

        assertTrue(userRepository.existsById(userId));
        assertNull(userRepository.findByEmail(email).orElse(null));
        assertNotNull(userRepository.findByEmail(SECOND_USER.getEmail()).orElse(null));
    }

    @Test
    public void testUpdateAnotherUser() throws Exception {
        utils.createNewUser(FIRST_USER);
        utils.createNewUser(SECOND_USER);
        final String firstUserEmail = FIRST_USER.getEmail();
        final String secondUserEmail = SECOND_USER.getEmail();
        final Long firstUserId = utils.getUserByEmail(firstUserEmail).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, firstUserId)
                .content(asJson(SECOND_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, secondUserEmail)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateUserWithNotValidEmail() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(NOT_VALID_EMAIL_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidFirstName() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(NOT_VALID_FIRSTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidLastName() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(NOT_VALID_LASTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidPassword() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(asJson(NOT_VALID_PASSWORD_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testDeleteUser() throws Exception {
        utils.createNewUser(FIRST_USER);
        final String email = FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, userId);

        utils.performAuthorizedRequest(deleteRequest, email)
                .andExpect(status().isOk());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testDeleteUserUnauthorizedFails() throws Exception {
        utils.createNewUser(FIRST_USER);
        utils.createNewUser(SECOND_USER);
        final String firstUserEmail = FIRST_USER.getEmail();
        final String secondUserEmail = SECOND_USER.getEmail();
        final Long firstUserId = utils.getUserByEmail(firstUserEmail).getId();

        final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, firstUserId);

        utils.performAuthorizedRequest(deleteRequest, secondUserEmail)
                .andExpect(status().isForbidden());

        assertEquals(2, userRepository.count());
    }
}
