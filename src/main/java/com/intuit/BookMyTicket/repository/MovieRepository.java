package com.intuit.BookMyTicket.repository;
import com.intuit.BookMyTicket.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}