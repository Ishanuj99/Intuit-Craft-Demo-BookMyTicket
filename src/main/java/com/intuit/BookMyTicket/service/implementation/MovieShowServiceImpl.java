package com.intuit.BookMyTicket.service.implementation;
import com.intuit.BookMyTicket.model.Movie;
import com.intuit.BookMyTicket.model.Show;
import com.intuit.BookMyTicket.repository.MovieRepository;
import com.intuit.BookMyTicket.repository.ShowRepository;
import com.intuit.BookMyTicket.service.MovieShowService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Service
public class MovieShowServiceImpl implements MovieShowService {

    private static final Logger logger = LoggerFactory.getLogger(MovieShowServiceImpl.class);

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final CircuitBreaker circuitBreaker;

    public MovieShowServiceImpl(MovieRepository movieRepository, ShowRepository showRepository,
                                CircuitBreaker circuitBreaker) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Movie getMovieDetails(Long movieId) {
        logger.info("Fetching movie details for movieId: {}", movieId);
        Supplier<Movie> movieSupplier = () -> movieRepository.findById(movieId).orElse(null);
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, movieSupplier).get();
        } catch (Exception e) {
            logger.error("Error fetching movie details for movieId: {}", movieId, e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "shows", key = "#movieId")
    public List<Show> getShowsByMovie(Long movieId) {
        logger.info("Fetching shows for movieId: {}", movieId);
        Supplier<List<Show>> showsSupplier = () -> showRepository.findByMovieId(movieId);
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, showsSupplier).get();
        } catch (Exception e) {
            logger.error("Error fetching shows for movieId: {}", movieId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    @CachePut(value = "shows", key = "#movieId")
    public Show saveShow(Show show) {
        return showRepository.save(show);
    }
}
