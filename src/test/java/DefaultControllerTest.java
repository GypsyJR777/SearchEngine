import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gypsyjr.main.controllers.DefaultController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        classes = DefaultController.class)
@AutoConfigureMockMvc
public class DefaultControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    public void getIndexPage_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
