package com.intuit.BookMyTicket.service;

import com.intuit.BookMyTicket.model.Seat;

import java.util.List;

public interface SeatService {
    List<Seat> getAvailableSeats(Long theaterId);
    boolean lockSeats(Long theaterId, List<Long> seatNumbers);
    boolean bookSeats(Long theaterId, List<Long> seatNumbers);
    void releaseLocks(Long showId, List<Long> seatIds);
    public Seat addSeat(Seat seat);
}
