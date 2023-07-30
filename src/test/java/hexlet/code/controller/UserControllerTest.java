package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.component.JWTUtils;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LoginDto;
import hexlet.code.entity.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_EMAIL_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_FIRSTNAME_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_LASTNAME_USER;
import static hexlet.code.utils.TestUtils.NOT_VALID_PASSWORD_USER;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.SECOND_USER;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.getInfoFromJson;
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
        utils.createNewUser(FIRST_USER);

        final LoginDto loginDto = new LoginDto(
                FIRST_USER.getEmail(),
                FIRST_USER.getPassword()
        );

        final var loginRequest = post(LOGIN)
                .content(asJson(loginDto))
                .contentType(APPLICATION_JSON);

        final var response =  utils.performUnauthorizedRequest(loginRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final String token = response.getContentAsString().trim();

        final String expectedUsername = FIRST_USER.getEmail();
        final String actualUsername = jwtUtils.readJWSToken(token)
                .get(SPRING_SECURITY_FORM_USERNAME_KEY)
                .toString();

        assertEquals(expectedUsername, actualUsername);
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
        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());

        final var response = utils.createNewUser(FIRST_USER)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
        assertEquals(ONE_ITEM_REPOSITORY_SIZE, userRepository.count());

        final User createdUser = utils.getUserByEmail(FIRST_USER.getEmail());
        assertNotEquals(FIRST_USER.getPassword(), createdUser.getPassword());
        assertTrue(passwordEncoder.matches(FIRST_USER.getPassword(), createdUser.getPassword()));
    }

    @Test
    public void testCreateUserWithNotValidEmailFail() throws Exception {
        utils.createNewUser(NOT_VALID_EMAIL_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidFirstNameFail() throws Exception {
        utils.createNewUser(NOT_VALID_FIRSTNAME_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidLastNameFail() throws Exception {
        utils.createNewUser(NOT_VALID_LASTNAME_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Test
    public void testCreateUserWithNotValidPasswordFail() throws Exception {
        utils.createNewUser(NOT_VALID_PASSWORD_USER)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
    }

    @Nested
    class GetUpdateDeleteTests {
        private static User firstUser;
        private static String firstUserEmail;
        private static Long firstUserId;

        @BeforeEach
        public void createFirstUser() throws Exception {
            utils.createNewUser(FIRST_USER);
            firstUser = userRepository.findAll().get(0);
            firstUserEmail = firstUser.getEmail();
            firstUserId = utils.getUserByEmail(firstUserEmail).getId();
        }

        @Test
        public void testGetAllUsers() throws Exception {
            utils.createNewUser(SECOND_USER);
            final int expectedCount = (int) userRepository.count();

            final var getRequest = get(USER_CONTROLLER_PATH);

            final var response = utils.performUnauthorizedRequest(getRequest)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
            assertFalse(response.getContentAsString().contains(SECOND_USER.getPassword()));

            final List<User> users = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
            assertEquals(expectedCount, users.size());
        }

        @Test
        public void testGetUserById() throws Exception {
            final var getRequest = get(USER_CONTROLLER_PATH + ID, firstUserId);

            final var response = utils.performAuthorizedRequest(getRequest, firstUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            final User actualUser = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

            assertEquals(firstUser.getId(), actualUser.getId());
            assertEquals(firstUser.getEmail(), actualUser.getEmail());
            assertEquals(firstUser.getFirstName(), actualUser.getFirstName());
            assertEquals(firstUser.getLastName(), actualUser.getLastName());
            assertFalse(response.getContentAsString().contains(FIRST_USER.getPassword()));
        }

        @Test
        public void testUpdateUser() throws Exception {
            final var response = utils.performAuthorizedRequest(
                    utils.createUserUpdateRequest(firstUserId, SECOND_USER), firstUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            assertFalse(response.getContentAsString().contains(SECOND_USER.getPassword()));

            assertTrue(userRepository.existsById(firstUserId));
            assertNull(userRepository.findByEmail(firstUserEmail).orElse(null));
            assertNotNull(userRepository.findByEmail(SECOND_USER.getEmail()).orElse(null));
        }

        @Test
        public void testUpdateAnotherUserFail() throws Exception {
            utils.createNewUser(SECOND_USER);
            final String secondUserEmail = SECOND_USER.getEmail();

            utils.performAuthorizedRequest(utils.createUserUpdateRequest(firstUserId, SECOND_USER), secondUserEmail)
                    .andExpect(status().isForbidden());
        }

        @Test
        public void testUpdateUserWithNotValidEmailFail() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createUserUpdateRequest(firstUserId, NOT_VALID_EMAIL_USER), firstUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            checkMatchActualUserByEmailWithFirstUser(firstUserEmail);
        }

        @Test
        public void testUpdateUserWithNotValidFirstNameFail() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createUserUpdateRequest(firstUserId, NOT_VALID_FIRSTNAME_USER), firstUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            checkMatchActualUserByEmailWithFirstUser(firstUserEmail);
        }

        @Test
        public void testUpdateUserWithNotValidLastNameFail() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createUserUpdateRequest(firstUserId, NOT_VALID_LASTNAME_USER), firstUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            checkMatchActualUserByEmailWithFirstUser(firstUserEmail);
        }

        @Test
        public void testUpdateUserWithNotValidPasswordFail() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createUserUpdateRequest(firstUserId, NOT_VALID_PASSWORD_USER), firstUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            checkMatchActualUserByEmailWithFirstUser(firstUserEmail);
        }

        @Test
        public void testUpdateNonExistUserFail() throws Exception {
            final Long nonExistUserId = firstUserId + 1;
            assertFalse(userRepository.findById(nonExistUserId).isPresent());

            utils.performAuthorizedRequest(utils.createUserUpdateRequest(nonExistUserId, SECOND_USER), firstUserEmail)
                    .andExpect(status().isNotFound());
        }

        @Test
        public void testDeleteUser() throws Exception {
            final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, firstUserId);

            utils.performAuthorizedRequest(deleteRequest, firstUserEmail)
                    .andExpect(status().isOk());

            assertEquals(EMPTY_REPOSITORY_SIZE, userRepository.count());
        }

        @Test
        public void testDeleteAnotherUserFail() throws Exception {
            utils.createNewUser(SECOND_USER);
            final int expectedCount = (int) userRepository.count();
            final String secondUserEmail = SECOND_USER.getEmail();

            final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, firstUserId);

            utils.performAuthorizedRequest(deleteRequest, secondUserEmail)
                    .andExpect(status().isForbidden());

            assertEquals(expectedCount, userRepository.count());
            assertThat(userRepository.findById(firstUserId)).isPresent();
        }

        private void checkMatchActualUserByEmailWithFirstUser(final String email) {
            final User actualUser = utils.getUserByEmail(email);

            assertEquals(firstUser.getFirstName(), actualUser.getFirstName());
            assertEquals(firstUser.getLastName(), actualUser.getLastName());
            assertEquals(firstUser.getEmail(), actualUser.getEmail());
        }
    }
}
