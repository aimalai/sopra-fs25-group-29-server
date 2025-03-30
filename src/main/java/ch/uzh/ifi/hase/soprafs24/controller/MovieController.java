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
    public ResponseEntity<String> searchMovies(@RequestParam String query) {
        String response = movieService.searchMovies(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<String> getMovieDetails(@RequestParam String id) {
        String response = movieService.getMovieDetails(id);
        return ResponseEntity.ok(response);
    }
}
