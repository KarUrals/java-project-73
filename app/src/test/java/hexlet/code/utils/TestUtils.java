package hexlet.code.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.JWTUtils;
import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;
import hexlet.code.repository.UserRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TestUtils {

    public static final UserDto FIRST_USER = new UserDto(
            "happy_dev@gmail.com",
            "John",
            "Clark",
            "k@4Fv"
    );

    public static final UserDto SECOND_USER = new UserDto(
            "happy_qa@gmail.com",
            "Kevin",
            "Gibson",
            "j3&aX"
    );

    public static final UserDto NOT_VALID_EMAIL_USER = new UserDto(
            "happy_dev.gmail.com",
            "John",
            "Clark",
            "k@4Fv"
    );
    public static final UserDto NOT_VALID_FIRSTNAME_USER = new UserDto(
            "happy_dev@gmail.com",
            "",
            "Clark",
            "k@4Fv"
    );

    public static final UserDto NOT_VALID_LASTNAME_USER = new UserDto(
            "happy_dev@gmail.com",
            "John",
            "",
            "k@4Fv"
    );

    public static final UserDto NOT_VALID_PASSWORD_USER = new UserDto(
            "happy_dev@gmail.com",
            "John",
            "Clark",
            "k@"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtils jwtUtils;

    public void tearDown() {
        userRepository.deleteAll();
    }

    public User getUserByEmail(final String email) {
        return userRepository.findByEmail(email).get();
    }


    public ResultActions createNewUser(final UserDto userDto) throws Exception {
        final var request = post(USER_CONTROLLER_PATH)
                .content(asJson(userDto))
                .contentType(APPLICATION_JSON);

        return performUnauthorizedRequest(request);
    }

    public ResultActions performAuthorizedRequest(
            final MockHttpServletRequestBuilder request,
            final String userEmail) throws Exception {
        final String token = jwtUtils.createJWSToken(Map.of("username", userEmail));
        request.header(AUTHORIZATION, token);

        return mockMvc.perform(request);
    }

    public ResultActions performUnauthorizedRequest(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T getInfoFromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }
}
