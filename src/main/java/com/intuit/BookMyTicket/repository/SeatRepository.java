package com.intuit.BookMyTicket.repository;

import com.intuit.BookMyTicket.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTheaterIdAndBookedFalse(Long theaterId);
    Optional<Seat> findBySeatNumberAndTheaterIdAndBookedFalse(Long seatNumber, Long theaterId);
    List<Seat> findByTheaterId(Long theaterId);
    Seat findByTheaterIdAndSeatNumber(Long theaterId, long seatNumber);
}
