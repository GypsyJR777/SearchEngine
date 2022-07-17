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
import ru.gypsyjr.main.controllers.StatisticController;
import ru.gypsyjr.main.models.ApiStatistics;
import ru.gypsyjr.main.models.Site;
import ru.gypsyjr.main.models.Statistic;
import ru.gypsyjr.main.models.Total;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = StatisticController.class)
@AutoConfigureMockMvc
public class StatisticControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    Storage storage;

    @Test
    public void getStatistic_success() throws Exception {
        ApiStatistics statistics = new ApiStatistics();
        Statistic statistic = new Statistic();

        statistic.setTotal(new Total());
        statistic.setDetailed(List.of(new Site()));

        statistics.setStatistics(statistic);
        statistics.setResult(true);

        Mockito.when(storage.getStatistic()).thenReturn(statistics);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
