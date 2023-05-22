package hexlet.code.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.JWTUtils;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.entity.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TestUtils {
    public static final int EMPTY_REPOSITORY_SIZE = 0;
    public static final int ONE_ITEM_REPOSITORY_SIZE = 1;

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

    public static final TaskStatusDto NEW_TASK_STATUS = new TaskStatusDto(
            "new"
    );

    public static final TaskStatusDto AT_WORK_TASK_STATUS = new TaskStatusDto(
            "at work"
    );

    public static final TaskStatusDto NOT_VALID_TASK_STATUS = new TaskStatusDto(
            ""
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private JWTUtils jwtUtils;

    public void tearDown() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();
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

    public ResultActions createNewTaskStatus(final TaskStatusDto taskStatusDto, final String email) throws Exception {
        final var request = post(TASK_STATUS_CONTROLLER_PATH)
                .content(asJson(taskStatusDto))
                .contentType(APPLICATION_JSON);

        return performAuthorizedRequest(request, email);
    }

    public ResultActions createNewLabel(final LabelDto labelDto, final String email) throws Exception {
        final var request = post(LABEL_CONTROLLER_PATH)
                .content(asJson(labelDto))
                .contentType(APPLICATION_JSON);

        return performAuthorizedRequest(request, email);
    }

    public ResultActions createNewTask(final TaskDto taskDto, final String email) throws Exception {
        final var request = post(TASK_CONTROLLER_PATH)
                .content(asJson(taskDto))
                .contentType(APPLICATION_JSON);

        return performAuthorizedRequest(request, email);
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
