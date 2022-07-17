import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gypsyjr.main.Config;
import ru.gypsyjr.main.Storage;
import ru.gypsyjr.main.controllers.IndexingController;
import ru.gypsyjr.main.models.Site;
import ru.gypsyjr.main.models.Status;
import ru.gypsyjr.main.repository.SiteRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        classes = {IndexingController.class, SiteRepository.class})
@AutoConfigureMockMvc
public class IndexingControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    Storage storage;

    @MockBean
    Config config;

    @MockBean
    SiteRepository siteRepository;

    @Test
    public void getStartIndexing_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/startIndexing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(true)));
    }

    @Test
    public void getStopIndexing_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/stopIndexing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(true)));
    }

    @Test
    public void postIndexingPage_success() throws Exception {
        Mockito.when(storage.indexPage("http://www.playback.ru/catalog/1511.html/")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/indexPage?url=http://www.playback.ru/catalog/1511.html/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(true)));
    }

    @Test
    public void postIndexingPage_wrong() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/indexPage?url=http://www.play.ru")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(false)));
    }

    @Test
    public void postIndexingPage_bad() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/indexPage")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
