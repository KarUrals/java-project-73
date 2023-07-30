package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskDto;
import hexlet.code.entity.Label;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
import hexlet.code.entity.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.LabelControllerTest.FIRST_LABEL;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.EMPTY_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NEW_TASK_STATUS;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.SECOND_USER;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.getInfoFromJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
class TaskControllerTest {
    private static final String DEFAULT_TASK_DESCRIPTION = "Description";
    private static final String TASK_NAME = "New task name";
    private static final String ANOTHER_TASK_NAME = "Another task name";
    private static final String NOT_VALID_TASK_NAME = "";
    private static User existingUser;
    private static String existingUserEmail;
    private static TaskStatus existingTaskStatus;
    private static Set<Long> labelsIds;
    private static TaskDto newTaskDto;
    private static TaskDto anotherTaskDto;
    private static TaskDto notValidTaskDto;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TestUtils utils;
    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    public void initialization() throws Exception {
        utils.createNewUser(FIRST_USER);
        existingUser = userRepository.findAll().stream().
                filter(Objects::nonNull).findFirst().get();
        existingUserEmail = existingUser.getEmail();

        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        existingTaskStatus = taskStatusRepository.findAll().stream().
                filter(Objects::nonNull).findFirst().get();

        utils.createNewLabel(FIRST_LABEL, existingUserEmail);
        Label label = labelRepository.findAll().stream().
                filter(Objects::nonNull).findFirst().get();
        labelsIds = new HashSet<>();
        labelsIds.add(label.getId());

        newTaskDto = buildTaskDto(TASK_NAME, existingUser, existingTaskStatus, labelsIds);
        anotherTaskDto = buildTaskDto(ANOTHER_TASK_NAME, existingUser, existingTaskStatus, labelsIds);
        notValidTaskDto = buildTaskDto(NOT_VALID_TASK_NAME, existingUser, existingTaskStatus, labelsIds);
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    void testCreateNewTask() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());

        utils.createNewTask(newTaskDto, existingUserEmail)
                .andExpect(status().isCreated());

        assertEquals(ONE_ITEM_REPOSITORY_SIZE, taskRepository.count());
    }

    @Test
    void testCreateNewTaskWithNotValidNameFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());

        utils.createNewTask(notValidTaskDto, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());
    }

    @Test
    void testTwiceCreateTheSameTask() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());

        utils.createNewTask(newTaskDto, existingUserEmail)
                .andExpect(status().isCreated());
        utils.createNewTask(newTaskDto, existingUserEmail)
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateNewTaskUnauthorizedFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());

        final var createRequest = post(TASK_CONTROLLER_PATH)
                .content(asJson(newTaskDto))
                .contentType(APPLICATION_JSON);

        try {
            utils.performUnauthorizedRequest(createRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }

        assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());
    }

    @Nested
    class GetUpdateDeleteTests {
        private static Long taskId;

        @BeforeEach
        public void createFirstLabel() throws Exception {
            utils.createNewTask(newTaskDto, existingUserEmail);
            taskId = taskRepository.findAll().get(0).getId();
        }

        @Test
        void testGetAllTasks() throws Exception {
            utils.createNewTask(anotherTaskDto, existingUserEmail);
            final int expectedCount = (int) taskRepository.count();

            final var getRequest = get(TASK_CONTROLLER_PATH);

            final var response = utils.performAuthorizedRequest(getRequest, existingUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            List<Task> tasks = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
            assertEquals(expectedCount, tasks.size());
        }

        @Test
        void testGetTaskById() throws Exception {
            final var getRequest = get(TASK_CONTROLLER_PATH + ID, taskId);

            final var response = utils.performAuthorizedRequest(getRequest, existingUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            final Task actualTask = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

            assertEquals(newTaskDto.getName(), actualTask.getName());
        }

        @Test
        void testUpdateTask() throws Exception {
            utils.performAuthorizedRequest(utils.createTaskUpdateRequest(taskId, anotherTaskDto), existingUserEmail)
                    .andExpect(status().isOk());

            Assertions.assertTrue(taskRepository.existsById(taskId));
            Assertions.assertNull(taskRepository.findByName(newTaskDto.getName()).orElse(null));
            Assertions.assertNotNull(taskRepository.findByName(anotherTaskDto.getName()).orElse(null));
        }

        @Test
        public void testUpdateAnotherUserTaskFail() throws Exception {
            utils.createNewUser(SECOND_USER);
            String secondUserEmail = SECOND_USER.getEmail();

            utils.performAuthorizedRequest(utils.createTaskUpdateRequest(taskId, anotherTaskDto), secondUserEmail)
                    .andExpect(status().isOk());

            Assertions.assertTrue(taskRepository.existsById(taskId));
            Assertions.assertNull(taskRepository.findByName(newTaskDto.getName()).orElse(null));
            Assertions.assertNotNull(taskRepository.findByName(anotherTaskDto.getName()).orElse(null));
        }

        @Test
        void testUpdateTaskWithNotValidNameFail() throws Exception {
            utils.performAuthorizedRequest(utils.createTaskUpdateRequest(taskId, notValidTaskDto), existingUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            Assertions.assertTrue(taskRepository.existsById(taskId));
            Assertions.assertNotNull(taskRepository.findByName(newTaskDto.getName()).orElse(null));
        }

        @Test
        public void testDeleteTask() throws Exception {
            final var deleteRequest = delete(TASK_CONTROLLER_PATH + ID, taskId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isOk());

            assertEquals(EMPTY_REPOSITORY_SIZE, taskRepository.count());
        }

        @Test
        public void testDeleteAnotherUserTaskFail() throws Exception {
            utils.createNewUser(SECOND_USER);
            String secondUserEmail = SECOND_USER.getEmail();

            final var deleteRequest = delete(TASK_CONTROLLER_PATH + ID, taskId);

            utils.performAuthorizedRequest(deleteRequest, secondUserEmail)
                    .andExpect(status().isForbidden());
        }

        @Test
        public void testDeleteTaskStatusAssignedToTaskFail() throws Exception {
            TaskStatus existingTaskStatus = taskStatusRepository.findAll().stream().
                    filter(Objects::nonNull).findFirst().get();
            final Long taskStatusId = existingTaskStatus.getId();

            final var deleteRequest = delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            assertThat(taskStatusRepository.findById(taskStatusId)).isPresent();
        }

        @Test
        public void testDeleteUserAssignedToTaskFail() throws Exception {
            User existingUser = userRepository.findAll().stream().
                    filter(Objects::nonNull).findFirst().get();
            final Long userId = existingUser.getId();

            final var deleteRequest = delete(USER_CONTROLLER_PATH + ID, userId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            assertThat(userRepository.findById(userId)).isPresent();
        }

        @Test
        public void testDeleteLabelAssignedToTaskFail() throws Exception {
            Label existingLabel = labelRepository.findAll().stream().
                    filter(Objects::nonNull).findFirst().get();
            final Long labelId = existingLabel.getId();

            final var deleteRequest = delete(LABEL_CONTROLLER_PATH + ID, labelId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            assertThat(labelRepository.findById(labelId)).isPresent();
        }
    }

    private TaskDto buildTaskDto(final String name,
                                 final User user,
                                 final TaskStatus taskStatus,
                                 final Set<Long> labelsIds) {

        return  new TaskDto(
                name,
                DEFAULT_TASK_DESCRIPTION,
                taskStatus.getId(),
                user.getId(),
                labelsIds
        );
    }
}
