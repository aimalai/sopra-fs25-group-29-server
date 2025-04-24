package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class MovieServiceUnitTest {

    private MovieService service;

    @Mock(lenient = true)
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder builder;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {
        when(builder.build()).thenReturn(restTemplate);
        service = new MovieService(builder);
    }

    @Test
    void movieDetails_ok() {
        String detail = """
            {
              "id":123,
              "title":"Test Movie",
              "overview":"Overview text.",
              "vote_average":8.5,
              "vote_count":150,
              "release_date":"2025-01-01",
              "poster_path":"/test.jpg",
              "genres":[{"id":1,"name":"Action"}]
            }
            """;
        String credits = """
            {
              "cast":[{"name":"Actor One"}],
              "crew":[{"job":"Director","name":"Dir One"}]
            }
            """;

        when(restTemplate.getForObject(contains("/movie/123?"), eq(String.class))).thenReturn(detail);
        when(restTemplate.getForObject(contains("/movie/123/credits"), eq(String.class))).thenReturn(credits);

        String result = service.getMediaDetails("123", "movie");

        assertTrue(result.contains("\"title\":\"Test Movie\""));
        assertTrue(result.contains("\"description\":\"Overview text.\""));
        assertTrue(result.contains("\"ratings\":8.5"));
        assertTrue(result.contains("\"vote_count\":150"));
        assertTrue(result.contains("\"release_date\":\"2025-01-01\""));
        assertTrue(result.contains("\"poster_path\":\"/test.jpg\""));
        assertTrue(result.contains("\"genre\":\"Action\""));
        assertTrue(result.contains("\"cast\":\"Actor One\""));

        verify(restTemplate, times(1)).getForObject(contains("/movie/123?"), eq(String.class));
        verify(restTemplate, times(1)).getForObject(contains("/movie/123/credits"), eq(String.class));
    }

    @Test
    void tvDetails_ok() {
        String detail = """
            {
              "id":321,
              "name":"Test Show",
              "overview":"TV overview.",
              "vote_average":7.2,
              "vote_count":80,
              "first_air_date":"2025-02-02",
              "poster_path":"/show.jpg",
              "genres":[{"id":2,"name":"Drama"}]
            }
            """;
        String credits = """
            {
              "cast":[{"name":"Actor TV"}],
              "crew":[{"job":"Producer","name":"Prod One"}]
            }
            """;

        when(restTemplate.getForObject(contains("/tv/321?"), eq(String.class))).thenReturn(detail);
        when(restTemplate.getForObject(contains("/tv/321/credits"), eq(String.class))).thenReturn(credits);

        String result = service.getMediaDetails("321", "tv");

        assertTrue(result.contains("\"title\":\"Test Show\""));
        assertTrue(result.contains("\"description\":\"TV overview.\""));
        assertTrue(result.contains("\"ratings\":7.2"));
        assertTrue(result.contains("\"vote_count\":80"));
        assertTrue(result.contains("\"release_date\":\"2025-02-02\""));
        assertTrue(result.contains("\"poster_path\":\"/show.jpg\""));
        assertTrue(result.contains("\"genre\":\"Drama\""));
        assertTrue(result.contains("\"cast\":\"Actor TV\""));

        verify(restTemplate, times(1)).getForObject(contains("/tv/321?"), eq(String.class));
        verify(restTemplate, times(1)).getForObject(contains("/tv/321/credits"), eq(String.class));
    }

    @Test
    void searchCombined_ok() throws Exception {
        when(restTemplate.getForObject(contains("/search/movie"), eq(String.class)))
            .thenReturn("{\"results\":[{\"id\":1}],\"total_pages\":1}");
        when(restTemplate.getForObject(contains("/search/tv"), eq(String.class)))
            .thenReturn("{\"results\":[],\"total_pages\":0}");

        String combined = service.searchCombined("q", null, false, 1, 5);
        JsonNode node = mapper.readTree(combined);

        assertTrue(node.has("results"));
        assertTrue(node.has("totalCount"));

        verify(restTemplate, times(1)).getForObject(contains("/search/movie"), eq(String.class));
        verify(restTemplate, times(1)).getForObject(contains("/search/tv"), eq(String.class));
    }

    @Test
    void searchCombined_error() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new RestClientException("fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.searchCombined("q", null, false, 1, 5)
        );
        assertEquals("Error combining search results", ex.getMessage());
    }
}
