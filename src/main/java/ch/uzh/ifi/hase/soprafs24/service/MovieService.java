package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Service
public class MovieService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public String searchCombined(String query) {
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

            ObjectNode combinedNode = objectMapper.createObjectNode();
            combinedNode.set("results", combined);

            return combinedNode.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error combining search results", e);
        }
    }

    public String getMovieDetails(String id) {
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
            String cast = castBuilder.toString();
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
            String genre = genreBuilder.toString();
            String title = detailsNode.get("title").asText();
            String description = detailsNode.get("overview").asText();
            double ratings = detailsNode.get("vote_average").asDouble();
            int voteCount = detailsNode.get("vote_count").asInt();
            String releaseDate = detailsNode.get("release_date").asText();
            String posterPath = detailsNode.get("poster_path").asText();
            StringBuilder result = new StringBuilder();
            result.append("{");
            result.append("\"id\":").append(id).append(",");
            result.append("\"title\":\"").append(title).append("\",");
            result.append("\"description\":\"").append(description.replace("\"", "\\\"")).append("\",");
            result.append("\"ratings\":").append(ratings).append(",");
            result.append("\"vote_count\":").append(voteCount).append(",");
            result.append("\"release_date\":\"").append(releaseDate).append("\",");
            result.append("\"poster_path\":\"").append(posterPath).append("\",");
            result.append("\"genre\":\"").append(genre).append("\",");
            result.append("\"cast\":\"").append(cast).append("\"");
            result.append("}");
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching movie details", e);
        }
    }

    public String getTVDetails(String id) {
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
            String cast = castBuilder.toString();
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
            String genre = genreBuilder.toString();
            String title = detailsNode.get("name").asText();
            String description = detailsNode.get("overview").asText();
            double ratings = detailsNode.get("vote_average").asDouble();
            int voteCount = detailsNode.get("vote_count").asInt();
            String releaseDate = detailsNode.get("first_air_date").asText();
            String posterPath = detailsNode.get("poster_path").asText();
            StringBuilder result = new StringBuilder();
            result.append("{");
            result.append("\"id\":").append(id).append(",");
            result.append("\"title\":\"").append(title).append("\",");
            result.append("\"description\":\"").append(description.replace("\"", "\\\"")).append("\",");
            result.append("\"ratings\":").append(ratings).append(",");
            result.append("\"vote_count\":").append(voteCount).append(",");
            result.append("\"release_date\":\"").append(releaseDate).append("\",");
            result.append("\"poster_path\":\"").append(posterPath).append("\",");
            result.append("\"genre\":\"").append(genre).append("\",");
            result.append("\"cast\":\"").append(cast).append("\"");
            result.append("}");
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching TV details", e);
        }
    }
}
