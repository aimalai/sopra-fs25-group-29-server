package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchMovies(
            @RequestParam String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "false") boolean onlyComplete,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "5") int pageSize) {
        String response = movieService.searchCombined(query, sort, onlyComplete, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<String> getDetails(
            @RequestParam String id,
            @RequestParam("media_type") String mediaType) {
        String response = movieService.getMediaDetails(id, mediaType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending")
    public ResponseEntity<String> getTrending() {
        String json = movieService.getTrending();
        return ResponseEntity.ok(json);
    }
}
