package hexlet.code.controller;

import hexlet.code.config.SpringConfigForIT;
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

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.AT_WORK_TASK_STATUS;
import static hexlet.code.utils.TestUtils.EMPTY_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.NEW_TASK_STATUS;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Nested
    class GetUpdateDeleteTests {
        private static Long taskStatusId;

        @BeforeEach
        public void createFirstLabel() throws Exception {
            utils.createNewTaskStatus(NEW_TASK_STATUS, existingUserEmail);
            taskStatusId = taskStatusRepository.findAll().get(0).getId();
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
        public void testDeleteTaskStatus() throws Exception {
            final var deleteRequest = delete(TASK_STATUS_CONTROLLER_PATH + ID, taskStatusId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isOk());

            assertEquals(EMPTY_REPOSITORY_SIZE, taskStatusRepository.count());
        }
    }
}
