package com.intuit.BookMyTicket.service.implementation;
import com.intuit.BookMyTicket.model.Booking;
import com.intuit.BookMyTicket.model.Seat;
import com.intuit.BookMyTicket.repository.BookingRepository;
import com.intuit.BookMyTicket.repository.SeatRepository;
import com.intuit.BookMyTicket.service.BookingService;
import com.intuit.BookMyTicket.service.SeatService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final CircuitBreaker circuitBreaker;
    private final SeatService seatService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, SeatRepository seatRepository,
                              CircuitBreaker circuitBreaker, SeatService seatService) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.circuitBreaker = circuitBreaker;
        this.seatService = seatService;
    }

    @Override
    public List<Seat> getAvailableSeats(Long theaterId) {
        logger.info("Fetching available seats for theaterId: {}", theaterId);
        Supplier<List<Seat>> seatSupplier = () -> seatRepository.findByTheaterIdAndBookedFalse(theaterId);
        return executeWithCircuitBreaker(seatSupplier, t -> Collections.emptyList());
    }

    @Override
    @Transactional
    public Booking bookSeats(Long showId, Long theaterId, Long userId, List<Long> seatNumbers) {
        boolean locksAcquired = seatService.lockSeats(theaterId, seatNumbers);

        if (!locksAcquired) {
            logger.warn("Unable to acquire locks for seats: {}", seatNumbers);
            throw new RuntimeException("Unable to acquire locks for seats, please try again later.");
        }

        try {
            logger.info("Processing booking for showId: {}, theaterId: {}, userId: {}, seatIds: {}", showId, theaterId, userId, seatNumbers);
            List<Long> seatIds = new ArrayList<>();
            for (Long seatNumber : seatNumbers) {
                Seat seat = seatRepository.findBySeatNumberAndTheaterIdAndBookedFalse(seatNumber, theaterId)
                        .orElseThrow(() -> new RuntimeException("Seat " + seatNumbers + " is not available in theater " + theaterId));

                seat.setBooked(true);
                seatIds.add(seat.getId());
                seatRepository.save(seat);
            }

            Booking booking = new Booking();
            booking.setShowId(showId);
            booking.setTheaterId(theaterId);
            booking.setUserId(userId);
            booking.setSeatIds(seatIds);
            booking.setBookingTime(java.time.LocalDateTime.now());

            return bookingRepository.save(booking);
        } finally {
            seatService.releaseLocks(theaterId, seatNumbers);
        }
    }


    // Generic method to handle Circuit Breaker execution with fallback
    private <T> T executeWithCircuitBreaker(Supplier<T> supplier, java.util.function.Function<Throwable, T> fallback) {
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, supplier).get();
        } catch (Exception e) {
            logger.error("Error during Circuit Breaker execution", e);
            return fallback.apply(e);
        }
    }
}
