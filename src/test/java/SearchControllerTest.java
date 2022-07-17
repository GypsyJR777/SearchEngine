import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gypsyjr.main.Storage;
import ru.gypsyjr.main.controllers.SearchController;
import ru.gypsyjr.main.models.Search;
import ru.gypsyjr.main.models.Site;
import ru.gypsyjr.main.repository.SiteRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SearchController.class)
@AutoConfigureMockMvc
public class SearchControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    Storage storage;

    @MockBean
    SiteRepository siteRepository;

    @Test
    public void getSearchWithoutSite_success() throws Exception {
        Search search = new Search();

        Mockito.when(storage.search("query", "site", 0, 20)).thenReturn(search);

        Mockito.when(siteRepository.findSiteByUrl("site")).thenReturn(new Site());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/search?query=query&site=site")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(false)));
    }

    @Test
    public void getSearchWithSite_success() throws Exception {
        Search search = new Search();
        Site site = new Site();

        site.setUrl("");

        Mockito.when(storage.search("query", "site", 0, 20)).thenReturn(search);
        Mockito.when(siteRepository.findSiteByUrl("site")).thenReturn(site);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/search?query=query&site=site")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getSearchWithoutQuery_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/search?&site=site")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result", is(false)));
    }
}
