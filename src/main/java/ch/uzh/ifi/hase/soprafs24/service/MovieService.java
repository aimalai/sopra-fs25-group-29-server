package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MovieService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String searchMovies(String query) {
        String url = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        return restTemplate.getForObject(url, String.class);
    }

    public String searchTV(String query) {
        String url = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/search/tv")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        return restTemplate.getForObject(url, String.class);
    }

    public String searchCombined(String query, String sort, boolean onlyComplete, int page, int pageSize) {
        try {
            String movieResponse = searchMovies(query);
            String tvResponse = searchTV(query);
            JsonNode movieNode = objectMapper.readTree(movieResponse);
            JsonNode tvNode = objectMapper.readTree(tvResponse);
            ArrayNode movieResults = (ArrayNode) movieNode.get("results");
            ArrayNode tvResults = (ArrayNode) tvNode.get("results");
            for (JsonNode movie : movieResults) {
                ((ObjectNode) movie).put("media_type", "movie");
            }
            for (JsonNode tv : tvResults) {
                ((ObjectNode) tv).put("media_type", "tv");
            }
            ArrayNode combined = objectMapper.createArrayNode();
            combined.addAll(movieResults);
            combined.addAll(tvResults);
            List<JsonNode> filteredSorted = StreamSupport.stream(combined.spliterator(), false)
                    .filter(r -> !onlyComplete || (
                            r.hasNonNull("poster_path") &&
                            !r.get("poster_path").asText().isEmpty() &&
                            ((r.hasNonNull("release_date") && !r.get("release_date").asText().isEmpty()) ||
                             (r.hasNonNull("first_air_date") && !r.get("first_air_date").asText().isEmpty())) &&
                            r.hasNonNull("overview") &&
                            !r.get("overview").asText().isEmpty()
                    ))
                    .sorted(getComparator(sort))
                    .collect(Collectors.toList());
            int totalCount = filteredSorted.size();
            int fromIndex = Math.min((page - 1) * pageSize, totalCount);
            int toIndex = Math.min(fromIndex + pageSize, totalCount);
            List<JsonNode> paginatedResults = filteredSorted.subList(fromIndex, toIndex);
            ArrayNode paginatedArray = objectMapper.createArrayNode();
            paginatedArray.addAll(paginatedResults);
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.set("results", paginatedArray);
            responseNode.put("totalCount", totalCount);
            return responseNode.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error combining search results", e);
        }
    }

    private Comparator<JsonNode> getComparator(String sort) {
        if (sort == null) {
            return Comparator.comparingInt(a -> 0);
        }
        return switch (sort) {
            case "popularity" -> Comparator.comparingDouble(a -> -a.path("popularity").asDouble());
            case "rating"     -> Comparator.comparingDouble(a -> -a.path("vote_average").asDouble());
            case "newest"     -> Comparator.comparing(this::extractDate).reversed();
            case "oldest"     -> Comparator.comparing(this::extractDate);
            default           -> Comparator.comparingInt(a -> 0);
        };
    }

    private LocalDate extractDate(JsonNode node) {
        String dateStr = node.has("release_date") && !node.get("release_date").asText().isEmpty()
                ? node.get("release_date").asText()
                : node.has("first_air_date") ? node.get("first_air_date").asText() : "";
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    public String getMediaDetails(String id, String mediaType) {
        if ("movie".equalsIgnoreCase(mediaType)) {
            return getMovieDetails(id);
        } else if ("tv".equalsIgnoreCase(mediaType)) {
            return getTVDetails(id);
        } else {
            throw new IllegalArgumentException("Invalid media type");
        }
    }

    private String getMovieDetails(String id) {
        String detailsUrl = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/movie/{id}")
                .queryParam("api_key", apiKey)
                .buildAndExpand(id)
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        String creditsUrl = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/movie/{id}/credits")
                .queryParam("api_key", apiKey)
                .buildAndExpand(id)
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        return buildDetailsResponse(detailsUrl, creditsUrl, true);
    }

    private String getTVDetails(String id) {
        String detailsUrl = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/tv/{id}")
                .queryParam("api_key", apiKey)
                .buildAndExpand(id)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
                
        String creditsUrl = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/tv/{id}/credits")
                .queryParam("api_key", apiKey)
                .buildAndExpand(id)
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        return buildDetailsResponse(detailsUrl, creditsUrl, false);
    }

    private String buildDetailsResponse(String detailsUrl, String creditsUrl, boolean isMovie) {
        try {
            String detailsResponse = restTemplate.getForObject(detailsUrl, String.class);
            String creditsResponse = restTemplate.getForObject(creditsUrl, String.class);
            JsonNode detailsNode = objectMapper.readTree(detailsResponse);
            JsonNode creditsNode = objectMapper.readTree(creditsResponse);

            StringBuilder castBuilder = new StringBuilder();
            JsonNode castArray = creditsNode.get("cast");
            if (castArray != null && castArray.isArray()) {
                int count = 0;
                Iterator<JsonNode> it = castArray.elements();
                while (it.hasNext() && count < 5) {
                    JsonNode actor = it.next();
                    String name = actor.get("name").asText();
                    if (castBuilder.length() > 0) {
                        castBuilder.append(", ");
                    }
                    castBuilder.append(name);
                    count++;
                }
            }

            StringBuilder genreBuilder = new StringBuilder();
            JsonNode genresArray = detailsNode.get("genres");
            if (genresArray != null && genresArray.isArray()) {
                Iterator<JsonNode> it = genresArray.elements();
                while (it.hasNext()) {
                    JsonNode genreNode = it.next();
                    String genreName = genreNode.get("name").asText();
                    if (genreBuilder.length() > 0) {
                        genreBuilder.append(", ");
                    }
                    genreBuilder.append(genreName);
                }
            }

            String title = isMovie ? detailsNode.get("title").asText() : detailsNode.get("name").asText();
            String description = detailsNode.get("overview").asText();
            double ratings = detailsNode.get("vote_average").asDouble();
            int voteCount = detailsNode.get("vote_count").asInt();
            String releaseDate = isMovie
                    ? detailsNode.get("release_date").asText()
                    : detailsNode.get("first_air_date").asText();
            String posterPath = detailsNode.get("poster_path").asText();

            return String.format(
                    "{\"id\":%s,\"title\":\"%s\",\"description\":\"%s\",\"ratings\":%.1f,\"vote_count\":%d,"
                            + "\"release_date\":\"%s\",\"poster_path\":\"%s\",\"genre\":\"%s\",\"cast\":\"%s\"}",
                    detailsNode.get("id").asText(),
                    title,
                    description.replace("\"", "\\\""),
                    ratings,
                    voteCount,
                    releaseDate,
                    posterPath,
                    genreBuilder.toString(),
                    castBuilder.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error fetching details", e);
        }
    }

    public String getTrending() {
        String url = UriComponentsBuilder
                .fromUriString("https://api.themoviedb.org/3/trending/all/day")
                .queryParam("api_key", apiKey)
                .toUriString();
        return restTemplate.getForObject(url, String.class);
    }
}
