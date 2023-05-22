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

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.EMPTY_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
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
        utils.createNewUser(TestUtils.FIRST_USER);

        final LoginDto loginDto = new LoginDto(
                TestUtils.FIRST_USER.getEmail(),
                TestUtils.FIRST_USER.getPassword()
        );

        final var loginRequest = post(LOGIN)
                .content(TestUtils.asJson(loginDto))
                .contentType(APPLICATION_JSON);

        final var response =  utils.performUnauthorizedRequest(loginRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final String token = response.getContentAsString().trim();

        final String expectedUsername = TestUtils.FIRST_USER.getEmail();
        final String actualUsername = jwtUtils.readJWSToken(token)
                .get(SPRING_SECURITY_FORM_USERNAME_KEY)
                .toString();

        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    public void testLoginFail() throws Exception {
        final LoginDto loginDto = new LoginDto(
                TestUtils.FIRST_USER.getEmail(),
                TestUtils.FIRST_USER.getPassword()
        );

        final var loginRequest = post(LOGIN)
                .content(TestUtils.asJson(loginDto))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(loginRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateUser() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());

        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(TestUtils.asJson(TestUtils.FIRST_USER))
                .contentType(APPLICATION_JSON);

        final var response = utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(TestUtils.FIRST_USER.getPassword()));
        assertEquals(ONE_ITEM_REPOSITORY_SIZE, userRepository.count());

        final User createdUser = utils.getUserByEmail(TestUtils.FIRST_USER.getEmail());
        assertNotEquals(TestUtils.FIRST_USER.getPassword(), createdUser.getPassword());
        assertTrue(passwordEncoder.matches(TestUtils.FIRST_USER.getPassword(), createdUser.getPassword()));
    }

    @Test
    public void testCreateUserWithNotValidEmailFail() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_EMAIL_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidFirstNameFail() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_FIRSTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidLastNameFail() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_LASTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidPasswordFail() throws Exception {
        final var createRequest = post(USER_CONTROLLER_PATH)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_PASSWORD_USER))
                .contentType(APPLICATION_JSON);

        utils.performUnauthorizedRequest(createRequest)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testTwiceCreateTheSameUserFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER)
                .andExpect(status().isCreated());
        utils.createNewUser(TestUtils.FIRST_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(ONE_ITEM_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        utils.createNewUser(TestUtils.SECOND_USER);
        final int expectedCount = (int) userRepository.count();

        final var getRequest = get(USER_CONTROLLER_PATH);

        final var response = utils.performUnauthorizedRequest(getRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(TestUtils.FIRST_USER.getPassword()));
        assertFalse(response.getContentAsString().contains(TestUtils.SECOND_USER.getPassword()));

        final List<User> users = TestUtils.getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
        assertEquals(expectedCount, users.size());
    }

    @Test
    public void testGetUserById() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final User expectedUser = utils.getUserByEmail(email);
        final Long expectedUserId = expectedUser.getId();

        final var getRequest = get(USER_CONTROLLER_PATH + ID, expectedUserId);

        final var response = utils.performAuthorizedRequest(getRequest, email)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final User actualUser = TestUtils.getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(), actualUser.getLastName());
        assertFalse(response.getContentAsString().contains(TestUtils.FIRST_USER.getPassword()));
    }

    @Test
    public void testGetNonExistUserByIdFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final User expectedUser = utils.getUserByEmail(email);
        final Long expectedUserId = expectedUser.getId();

        final Long nonExistUserId = expectedUserId + 1;
        assertFalse(userRepository.findById(nonExistUserId).isPresent());

        final var getRequest = get(USER_CONTROLLER_PATH + ID, nonExistUserId);

        utils.performAuthorizedRequest(getRequest, email)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUser() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(TestUtils.asJson(TestUtils.SECOND_USER))
                .contentType(APPLICATION_JSON);

        final var response = utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(TestUtils.SECOND_USER.getPassword()));

        assertTrue(userRepository.existsById(userId));
        assertNull(userRepository.findByEmail(email).orElse(null));
        assertNotNull(userRepository.findByEmail(TestUtils.SECOND_USER.getEmail()).orElse(null));
    }

    @Test
    public void testUpdateAnotherUserFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        utils.createNewUser(TestUtils.SECOND_USER);
        final String firstUserEmail = TestUtils.FIRST_USER.getEmail();
        final String secondUserEmail = TestUtils.SECOND_USER.getEmail();
        final Long firstUserId = utils.getUserByEmail(firstUserEmail).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, firstUserId)
                .content(TestUtils.asJson(TestUtils.SECOND_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, secondUserEmail)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateUserWithNotValidEmailFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_EMAIL_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(TestUtils.FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(TestUtils.FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(TestUtils.FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidFirstNameFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_FIRSTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(TestUtils.FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(TestUtils.FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(TestUtils.FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidLastNameFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_LASTNAME_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(TestUtils.FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(TestUtils.FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(TestUtils.FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateUserWithNotValidPasswordFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, userId)
                .content(TestUtils.asJson(TestUtils.NOT_VALID_PASSWORD_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isUnprocessableEntity());

        final User createdUser = utils.getUserByEmail(email);

        assertEquals(TestUtils.FIRST_USER.getFirstName(), createdUser.getFirstName());
        assertEquals(TestUtils.FIRST_USER.getLastName(), createdUser.getLastName());
        assertEquals(TestUtils.FIRST_USER.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testUpdateNonExistUserFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final Long nonExistUserId = userId + 1;
        assertFalse(userRepository.findById(nonExistUserId).isPresent());

        final var updateRequest = put(USER_CONTROLLER_PATH + ID, nonExistUserId)
                .content(TestUtils.asJson(TestUtils.SECOND_USER))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, email)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUser() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, userId);

        utils.performAuthorizedRequest(deleteRequest, email)
                .andExpect(status().isOk());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testDeleteAnotherUserFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        utils.createNewUser(TestUtils.SECOND_USER);
        final int expectedCount = (int) userRepository.count();

        final String firstUserEmail = TestUtils.FIRST_USER.getEmail();
        final String secondUserEmail = TestUtils.SECOND_USER.getEmail();
        final Long firstUserId = utils.getUserByEmail(firstUserEmail).getId();

        final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, firstUserId);

        utils.performAuthorizedRequest(deleteRequest, secondUserEmail)
                .andExpect(status().isForbidden());

        assertEquals(expectedCount, userRepository.count());
        assertThat(userRepository.findById(firstUserId)).isPresent();
    }

    @Test
    public void testDeleteNonExistUserFail() throws Exception {
        utils.createNewUser(TestUtils.FIRST_USER);
        final String email = TestUtils.FIRST_USER.getEmail();
        final Long userId = utils.getUserByEmail(email).getId();

        final Long nonExistUserId = userId + 1;
        assertFalse(userRepository.findById(nonExistUserId).isPresent());

        final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, nonExistUserId);

        utils.performAuthorizedRequest(deleteRequest, email)
                .andExpect(status().isNotFound());
    }
}
