package com.intuit.BookMyTicket.service;

import com.intuit.BookMyTicket.model.Movie;
import com.intuit.BookMyTicket.model.Show;

import java.util.List;

public interface MovieShowService {
    Movie getMovieDetails(Long movieId);
    List<Show> getShowsByMovie(Long movieId);
    Movie saveMovie(Movie movie);
    Show saveShow(Show show);
}
