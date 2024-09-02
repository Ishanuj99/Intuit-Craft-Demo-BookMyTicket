package com.intuit.BookMyTicket.controller;
import com.intuit.BookMyTicket.model.Movie;
import com.intuit.BookMyTicket.model.Show;
import com.intuit.BookMyTicket.service.MovieShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieShowController {

    private static final Logger logger = LoggerFactory.getLogger(MovieShowController.class);

    private final MovieShowService movieShowService;

    public MovieShowController(MovieShowService movieShowService) {
        this.movieShowService = movieShowService;
    }

    @GetMapping("/{movieId}")
    public Movie getMovieDetails(@PathVariable Long movieId) {
        logger.info("Received request to get movie details for movieId: {}", movieId);
        return movieShowService.getMovieDetails(movieId);
    }

    @GetMapping("/{movieId}/shows")
    public List<Show> getShowsByMovie(@PathVariable Long movieId) {
        logger.info("Received request to get shows for movieId: {}", movieId);
        return movieShowService.getShowsByMovie(movieId);
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        logger.info("Received request to create a new movie: {}", movie.getTitle());
        Movie createdMovie = movieShowService.saveMovie(movie);
        return ResponseEntity.ok(createdMovie);
    }

    @PostMapping("/{movieId}/shows")
    public ResponseEntity<Show> createShow(@PathVariable Long movieId, @RequestBody Show show) {
        logger.info("Received request to create a new show for movieId: {}", movieId);
        show.setMovieId(movieId);
        Show createdShow = movieShowService.saveShow(show);
        return ResponseEntity.ok(createdShow);
    }
}

