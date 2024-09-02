package com.intuit.BookMyTicket.controller;

import com.intuit.BookMyTicket.dto.APIResponse;
import com.intuit.BookMyTicket.dto.ErrorResponse;
import com.intuit.BookMyTicket.dto.LockSeatsRequest;
import com.intuit.BookMyTicket.model.Booking;
import com.intuit.BookMyTicket.model.Seat;
import com.intuit.BookMyTicket.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/available-seats/{theaterId}")
    public List<Seat> getAvailableSeats(@PathVariable Long theaterId) {
        logger.info("Received request to get available seats for theaterId: {}", theaterId);
        return bookingService.getAvailableSeats(theaterId);
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookSeats(@RequestParam Long showId, @RequestParam Long theaterId, @RequestParam Long userId, @RequestBody LockSeatsRequest request) {
        logger.info("Received booking request for showId: {}, theaterId: {}, userId: {}, seatIds: {}", showId, theaterId, userId, request.getSeatNumbers());
        try {
            Booking booking = bookingService.bookSeats(showId, theaterId, userId, request.getSeatNumbers());
            return ResponseEntity.ok(booking);
        }  catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse("Failed to book seats: " + e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


}
