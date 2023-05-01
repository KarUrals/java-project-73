package hexlet.code.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUsers() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/api/users"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("John", "Smith", "j.smith@ya.ru");
        assertThat(response.getContentAsString()).contains("Jack", "Doe", "doe.j@ya.ru");
        assertThat(response.getContentAsString()).contains("Jessica","Simpson", "simpson@ya.ru");
        assertThat(response.getContentAsString()).contains("Robert", "Lock", "r.lock@ya.ru");
    }

    @Test
    void testGetUser() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/api/users/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("John", "Smith", "j.smith@ya.ru");
        assertThat(response.getContentAsString()).doesNotContain("j4saw5");
    }

    @Test
    void testCreateUser() throws Exception {
        MockHttpServletResponse responsePost = mockMvc
                .perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{" +
                                        "\"firstName\": \"Johny\", " +
                                        "\"lastName\": \"Walker\", " +
                                        "\"email\": \"sdfs@ya.ru\", " +
                                        "\"password\": \"sdf68q\", " +
                                        "\"createdAt\": \"2023-04-28T15:55:22.342+00:00\"" +
                                        "}")
                )
                .andReturn()
                .getResponse();

        assertThat(responsePost.getStatus()).isEqualTo(201);

        MockHttpServletResponse response = mockMvc
                .perform(get("/api/users"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("Johny", "Walker", "sdfs@ya.ru");
    }

    @Test
    void testUpdateUser() throws Exception {
        MockHttpServletResponse responsePost = mockMvc
                .perform(
                        put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{" +
                                        "\"firstName\": \"Johny\", " +
                                        "\"lastName\": \"Walker\", " +
                                        "\"email\": \"sdfs@ya.ru\", " +
                                        "\"password\": \"sdf68q\"" +
                                        "}")
                )
                .andReturn()
                .getResponse();

        assertThat(responsePost.getStatus()).isEqualTo(200);

        MockHttpServletResponse response = mockMvc
                .perform(get("/api/users"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("Johny", "Walker", "sdfs@ya.ru");
    }

    @Test
    void testDeleteUser() throws Exception {
        MockHttpServletResponse responsePost = mockMvc
                .perform(delete("/api/users/1"))
                .andReturn()
                .getResponse();

        assertThat(responsePost.getStatus()).isEqualTo(200);

        MockHttpServletResponse response = mockMvc
                .perform(get("/api/users"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).doesNotContain("John", "Smith");
    }
}
