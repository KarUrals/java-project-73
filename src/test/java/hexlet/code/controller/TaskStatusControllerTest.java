package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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

import java.util.List;
import java.util.NoSuchElementException;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.AT_WORK_TASK_STATUS;
import static hexlet.code.utils.TestUtils.EMPTY_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NEW_TASK_STATUS;
import static hexlet.code.utils.TestUtils.NOT_VALID_TASK_STATUS;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.getInfoFromJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    }

    @Test
    void testCreateNewTaskStatus() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());

        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isCreated());

        assertEquals(ONE_ITEM_REPOSITORY_SIZE, taskStatusRepository.count());
    }

    @Test
    void testCreateNewTaskStatusWithNotValidNameFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());

        utils.createNewTaskStatus(NOT_VALID_TASK_STATUS, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());
    }

//    @Test
    void testTwiceCreateTheSameTaskStatusFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());

        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isCreated());
        utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(ONE_ITEM_REPOSITORY_SIZE, taskStatusRepository.count());
    }

    @Test
    void testCreateNewTaskStatusUnauthorizedFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());

        final var createRequest = post(TASK_STATUS_CONTROLLER_PATH)
                .content(asJson(NEW_TASK_STATUS))
                .contentType(APPLICATION_JSON);

        try {
            utils.performUnauthorizedRequest(createRequest);
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }

        assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());
    }

    @Nested
    class GetUpdateDeleteTests {
        private static Long taskStatusId;

        @BeforeEach
        public void createFirstLabel() throws Exception {
            utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
            taskStatusId = taskStatusRepository.findAll().get(0).getId();
        }

//        @Test
        void testGetAllTaskStatuses() throws Exception {
            utils.createNewTaskStatus(AT_WORK_TASK_STATUS, existingUserEmail);
            final int expectedCount = (int) taskStatusRepository.count();

            final var getRequest = get(TASK_STATUS_CONTROLLER_PATH);

            final var response = utils.performUnauthorizedRequest(getRequest)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            List<TaskStatus> statuses = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
            assertEquals(expectedCount, statuses.size());
        }

//        @Test
        void testGetTaskStatusById() throws Exception {
            final var getRequest = get(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

            final var response = utils.performUnauthorizedRequest(getRequest)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            final TaskStatus actualTaskStatus = getInfoFromJson(response.getContentAsString(),
                                                                new TypeReference<>() { });

            assertEquals(NEW_TASK_STATUS.getName(), actualTaskStatus.getName());
        }

        @Test
        public void testGetNonExistTaskStatusByIdFail() throws Exception {
            final Long nonExistTaskStatusId = taskStatusId + 1;
            assertFalse(taskStatusRepository.findById(nonExistTaskStatusId).isPresent());

            final var getRequest = get(TASK_STATUS_CONTROLLER_PATH + ID, nonExistTaskStatusId);

            utils.performAuthorizedRequest(getRequest, existingUserEmail)
                    .andExpect(status().isNotFound());
        }

        @Test
        void testUpdateTaskStatus() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createTaskStatusUpdateRequest(taskStatusId, AT_WORK_TASK_STATUS), existingUserEmail)
                    .andExpect(status().isOk());

            Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
            Assertions.assertNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
            Assertions.assertNotNull(taskStatusRepository.findByName(AT_WORK_TASK_STATUS.getName()).orElse(null));
        }

        @Test
        void testUpdateTaskStatusWithNotValidNameFail() throws Exception {
            utils.performAuthorizedRequest(
                    utils.createTaskStatusUpdateRequest(taskStatusId, NOT_VALID_TASK_STATUS), existingUserEmail)
                    .andExpect(status().isUnprocessableEntity());

            Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
            Assertions.assertNotNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
        }

        @Test
        void testUpdateTaskStatusUnauthorizedFailsFail() throws Exception {
            try {
                utils.performUnauthorizedRequest(
                        utils.createTaskStatusUpdateRequest(taskStatusId, AT_WORK_TASK_STATUS));
            } catch (NoSuchElementException e) {
                assertThat(e.getMessage()).isEqualTo("No value present");
            }

            Assertions.assertTrue(taskStatusRepository.existsById(taskStatusId));
            Assertions.assertNull(taskStatusRepository.findByName(AT_WORK_TASK_STATUS.getName()).orElse(null));
            Assertions.assertNotNull(taskStatusRepository.findByName(NEW_TASK_STATUS.getName()).orElse(null));
        }

        @Test
        public void testUpdateNonExistTaskStatusFail() throws Exception {
            final Long nonExistTaskStatusId = taskStatusId + 1;
            assertFalse(taskStatusRepository.findById(nonExistTaskStatusId).isPresent());
            utils.performAuthorizedRequest(
                    utils.createTaskStatusUpdateRequest(nonExistTaskStatusId, AT_WORK_TASK_STATUS), existingUserEmail)
                    .andExpect(status().isNotFound());
        }

        @Test
        public void testDeleteTaskStatus() throws Exception {
            final var deleteRequest = delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isOk());

            assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());
        }

        @Test
        public void testDeleteTaskStatusUnauthorizedFail() throws Exception {
            final var deleteRequest = delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

            try {
                utils.performUnauthorizedRequest(deleteRequest);
            } catch (NoSuchElementException e) {
                assertThat(e.getMessage()).isEqualTo("No value present");
            }

            assertThat(taskStatusRepository.findById(taskStatusId)).isPresent();
        }

        @Test
        public void testDeleteNonExistTaskStatusFail() throws Exception {
            final Long nonExistTaskStatusId = taskStatusId + 1;
            assertFalse(taskStatusRepository.findById(nonExistTaskStatusId).isPresent());

            final var deleteRequest = delete(TASK_STATUS_CONTROLLER_PATH + ID, nonExistTaskStatusId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isNotFound());
        }
    }
}
