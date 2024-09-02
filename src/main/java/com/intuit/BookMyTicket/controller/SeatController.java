package com.intuit.BookMyTicket.controller;

import com.intuit.BookMyTicket.dto.LockSeatsRequest;
import com.intuit.BookMyTicket.dto.APIResponse;
import com.intuit.BookMyTicket.model.Seat;
import com.intuit.BookMyTicket.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
public class SeatController {

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/theaters/{theaterId}")
    public List<Seat> getAvailableSeats(@PathVariable Long theaterId) {
        logger.info("Fetching available seats for theaterId: {}", theaterId);
        return seatService.getAvailableSeats(theaterId);
    }

    @PostMapping("/theaters/{theaterId}/lock")
    public ResponseEntity<APIResponse> lockSeats(@PathVariable Long theaterId, @RequestBody LockSeatsRequest request) {
        logger.info("Locking seats {} for theaterId: {}", request.getSeatNumbers(), theaterId);
        boolean success = seatService.lockSeats(theaterId, request.getSeatNumbers());

        APIResponse response = new APIResponse();
        if (success) {
            response.setMessage("Seats locked successfully");
            response.setSuccess(HttpStatus.ACCEPTED);
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Some or all seats are not available");
            response.setSuccess(HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping("/theaters/{theaterId}/book")
    public boolean bookSeats(@PathVariable Long theaterId, @RequestBody List<Long> seatNumbers) {
        logger.info("Booking seats {} for theaterId: {}", seatNumbers, theaterId);
        return seatService.bookSeats(theaterId, seatNumbers);
    }

    @PostMapping("/add")
    public ResponseEntity<Seat> addSeat(@RequestBody Seat seat) {
        logger.info("Adding new seat with seatNumber {} for theaterId: {}", seat.getSeatNumber(), seat.getTheaterId());
        Seat createdSeat = seatService.addSeat(seat);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeat);
    }
}
