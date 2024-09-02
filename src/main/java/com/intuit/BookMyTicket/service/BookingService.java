package com.intuit.BookMyTicket.service;

import com.intuit.BookMyTicket.model.Booking;
import com.intuit.BookMyTicket.model.Seat;

import java.util.List;

public interface BookingService {
    List<Seat> getAvailableSeats(Long theaterId);
    Booking bookSeats(Long showId, Long theaterId, Long userId, List<Long> seatIds);
}
