package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.entity.Label;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.LabelRepository;
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

import java.util.List;
import java.util.NoSuchElementException;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.EMPTY_REPOSITORY_SIZE;
import static hexlet.code.utils.TestUtils.FIRST_USER;
import static hexlet.code.utils.TestUtils.ONE_ITEM_REPOSITORY_SIZE;
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
class LabelControllerTest {
    public static final LabelDto FIRST_LABEL = new LabelDto("Feature");
    public static final LabelDto SECOND_LABEL = new LabelDto("Bug");
    private static final LabelDto NOT_VALID_LABEL = new LabelDto("");
    private static String existingUserEmail;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void initialization() throws Exception {
        utils.createNewUser(FIRST_USER);
        existingUserEmail = userRepository.findAll().get(0).getEmail();
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    void testCreateNewLabel() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());

        utils.createNewLabel(FIRST_LABEL, existingUserEmail)
                .andExpect(status().isCreated());

        assertEquals(ONE_ITEM_REPOSITORY_SIZE, labelRepository.count());
    }

    @Test
    void testCreateNewLabelWithNotValidNameFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());

        utils.createNewLabel(NOT_VALID_LABEL, existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());
    }

    @Test
    void testCreateNewLabelUnauthorizedFail() throws Exception {
        assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());

        final var createRequest = post(LABEL_CONTROLLER_PATH)
                .content(asJson(FIRST_LABEL))
                .contentType(APPLICATION_JSON);

        try {
            utils.performUnauthorizedRequest(createRequest);
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("No value present");
        }

        assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());
    }

    @Nested
    class GetUpdateDeleteTests {
        private static Long labelId;

        @BeforeEach
        public void createFirstLabel() throws Exception {
            utils.createNewLabel(FIRST_LABEL, existingUserEmail);
            labelId = labelRepository.findAll().get(0).getId();
        }

        @Test
        void testGetAllLabels() throws Exception {
            utils.createNewLabel(SECOND_LABEL, existingUserEmail);
            final int expectedCount = (int) labelRepository.count();

            final var getRequest = get(LABEL_CONTROLLER_PATH);

            final var response = utils.performAuthorizedRequest(getRequest, existingUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            List<Label> labels = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });
            assertEquals(expectedCount, labels.size());
        }

        @Test
        void testGetAllLabelsUnauthorizedFail() throws Exception {
            utils.createNewLabel(SECOND_LABEL, existingUserEmail);

            final var getRequest = get(LABEL_CONTROLLER_PATH);

            try {
                utils.performUnauthorizedRequest(getRequest);
            } catch (NoSuchElementException e) {
                assertThat(e.getMessage()).isEqualTo("No value present");
            }
        }

        @Test
        void testGetLabelById() throws Exception {
            final var getRequest = get(LABEL_CONTROLLER_PATH + ID, labelId);

            final var response = utils.performAuthorizedRequest(getRequest, existingUserEmail)
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            final TaskStatus actualLabel = getInfoFromJson(response.getContentAsString(), new TypeReference<>() { });

            assertEquals(FIRST_LABEL.getName(), actualLabel.getName());
        }

        @Test
        void testUpdateLabel() throws Exception {
            utils.performAuthorizedRequest(utils.createLabelUpdateRequest(labelId, SECOND_LABEL), existingUserEmail)
                    .andExpect(status().isOk());

            Assertions.assertTrue(labelRepository.existsById(labelId));
            Assertions.assertNull(labelRepository.findByName(FIRST_LABEL.getName()).orElse(null));
            Assertions.assertNotNull(labelRepository.findByName(SECOND_LABEL.getName()).orElse(null));
        }

        @Test
        public void testDeleteLabel() throws Exception {
            final var deleteRequest = delete(LABEL_CONTROLLER_PATH + ID, labelId);

            utils.performAuthorizedRequest(deleteRequest, existingUserEmail)
                    .andExpect(status().isOk());

            assertEquals(EMPTY_REPOSITORY_SIZE, labelRepository.count());
        }
    }
}
