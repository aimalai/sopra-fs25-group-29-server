package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchMovies(@RequestParam String query,
                                               @RequestParam(required = false) String sort) {
        String response = movieService.searchCombined(query, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<String> getDetails(@RequestParam String id,
                                             @RequestParam String media_type) {
        String response = movieService.getMediaDetails(id, media_type);
        return ResponseEntity.ok(response);
    }
}
