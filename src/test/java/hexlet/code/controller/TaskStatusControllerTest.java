package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.AT_WORK_TASK_STATUS;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NEW_TASK_STATUS;
import static hexlet.code.utils.TestUtils.NOT_VALID_TASK_STATUS;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.getInfoFromJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@Transactional
class TaskStatusControllerTest {
    private static String existingUserEmail;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void initialization() throws Exception {
        utils.createNewUser(FIRST_USER);
        existingUserEmail = FIRST_USER.getEmail();
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
        taskStatusRepository.deleteAll();
    }

    @Test
    void testCreateNewTaskStatus() throws Exception {
        Assertions.assertEquals(0, taskStatusRepository.count());

        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isCreated());

        Assertions.assertEquals(1, taskStatusRepository.count());
    }

    @Test
    void testCreateNewTaskStatusWithNotValidFirstName() throws Exception {
        Assertions.assertEquals(0, taskStatusRepository.count());

        utils.createNewTaskStatus(NOT_VALID_TASK_STATUS, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        Assertions.assertEquals(0, taskStatusRepository.count());
    }

    @Test
    void testTwiceCreateTheSameTaskStatusFail() throws Exception {
        Assertions.assertEquals(0, taskStatusRepository.count());

        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isCreated());
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        Assertions.assertEquals(1, taskStatusRepository.count());
    }

    @Test
    void testCreateNewTaskStatusUnauthorizedFails() throws Exception {
        Assertions.assertEquals(0, taskStatusRepository.count());

        final var createRequest = MockMvcRequestBuilders.post(TASK_STATUS_CONTROLLER_PATH)
                .content(asJson(NEW_TASK_STATUS))
                .contentType(APPLICATION_JSON);

        try {
            utils.performUnauthorizedRequest(createRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }

        Assertions.assertEquals(0, taskStatusRepository.count());
    }

    @Test
    void testGetAllTaskStatuses() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        utils.createNewTaskStatus(AT_WORK_TASK_STATUS, existingUserEmail);
        final int expectedCount = (int) taskStatusRepository.count();

        final var getRequest = MockMvcRequestBuilders.get(TASK_STATUS_CONTROLLER_PATH);

        final var response = utils.performUnauthorizedRequest(getRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<TaskStatus> statuses = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
        Assertions.assertEquals(expectedCount, statuses.size());
    }

    @Test
    void testGetTaskStatusById() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var getRequest = MockMvcRequestBuilders.get(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

        final var response = utils.performUnauthorizedRequest(getRequest)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final TaskStatus actualTaskStatus = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

        Assertions.assertEquals(NEW_TASK_STATUS.getName(), actualTaskStatus.getName());
    }

    @Test
    void testUpdateTaskStatus() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var updateRequest = MockMvcRequestBuilders.put(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId)
                .content(asJson(AT_WORK_TASK_STATUS))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, existingUserEmail)
                .andExpect(status().isOk());

        Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
        Assertions.assertNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
        Assertions.assertNotNull(taskStatusRepository.findByName(AT_WORK_TASK_STATUS.getName()).orElse(null));
    }

    @Test
    void testUpdateTaskStatusWithNotValidName() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var updateRequest = MockMvcRequestBuilders.put(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId)
                .content(asJson(NOT_VALID_TASK_STATUS))
                .contentType(APPLICATION_JSON);

        utils.performAuthorizedRequest(updateRequest, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
        Assertions.assertNotNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
    }

    @Test
    void testUpdateTaskStatusUnauthorizedFails() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var updateRequest = MockMvcRequestBuilders.put(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId)
                .content(asJson(AT_WORK_TASK_STATUS))
                .contentType(APPLICATION_JSON);

        try {
            utils.performUnauthorizedRequest(updateRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }

        Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
        Assertions.assertNull(taskStatusRepository.findByName(AT_WORK_TASK_STATUS.getName()).orElse(null));
        Assertions.assertNotNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
    }

    @Test
    public void testDeleteTaskStatus() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var deleteRequest = MockMvcRequestBuilders.delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

        utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                .andExpect(status().isOk());

        Assertions.assertEquals(0, taskStatusRepository.count());
    }

    @Test
    public void testDeleteTaskStatusUnauthorizedFails() throws Exception {
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
        final Long taskStatusId = taskStatusRepository.findAll().get(0).getId();

        final var deleteRequest = MockMvcRequestBuilders.delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

        try {
            utils.performUnauthorizedRequest(deleteRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }
        assertThat(taskStatusRepository.findById(taskStatusId)).isPresent();
    }
}
