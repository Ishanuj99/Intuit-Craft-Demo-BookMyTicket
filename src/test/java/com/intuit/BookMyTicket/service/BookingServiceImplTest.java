package com.intuit.BookMyTicket.service;

import com.intuit.BookMyTicket.model.Booking;
import com.intuit.BookMyTicket.model.Seat;
import com.intuit.BookMyTicket.repository.BookingRepository;
import com.intuit.BookMyTicket.repository.SeatRepository;
import com.intuit.BookMyTicket.service.implementation.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuccessfulBooking() {
        Long showId = 1L;
        Long theaterId = 1L;
        Long userId = 1L;
        List<Long> seatNumbers = Arrays.asList(1L, 2L, 3L);

        Seat seat1 = new Seat();
        seat1.setId(1L);
        seat1.setSeatNumber(1);
        seat1.setBooked(false);

        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setSeatNumber(2);
        seat2.setBooked(false);

        Seat seat3 = new Seat();
        seat3.setId(3L);
        seat3.setSeatNumber(3);
        seat3.setBooked(false);

        // Mocking seat locking
        when(seatService.lockSeats(theaterId, seatNumbers)).thenReturn(true);

        // Mocking seat repository responses
        when(seatRepository.findBySeatNumberAndTheaterIdAndBookedFalse(1L, theaterId)).thenReturn(Optional.of(seat1));
        when(seatRepository.findBySeatNumberAndTheaterIdAndBookedFalse(2L, theaterId)).thenReturn(Optional.of(seat2));
        when(seatRepository.findBySeatNumberAndTheaterIdAndBookedFalse(3L, theaterId)).thenReturn(Optional.of(seat3));

        // Mocking the save operation
        when(seatRepository.save(any(Seat.class))).thenReturn(seat1, seat2, seat3);

        // Mocking booking repository save operation
        Booking expectedBooking = new Booking();
        expectedBooking.setShowId(showId);
        expectedBooking.setTheaterId(theaterId);
        expectedBooking.setUserId(userId);
        expectedBooking.setSeatIds(Arrays.asList(1L, 2L, 3L));
        when(bookingRepository.save(any(Booking.class))).thenReturn(expectedBooking);

        // Call the method to be tested
        Booking actualBooking = bookingService.bookSeats(showId, theaterId, userId, seatNumbers);

        // Verify the results
        assertEquals(expectedBooking, actualBooking);
        verify(seatService, times(1)).lockSeats(theaterId, seatNumbers);
        verify(seatRepository, times(3)).save(any(Seat.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(seatService, times(1)).releaseLocks(theaterId, seatNumbers);
    }

    @Test
    void testBookingFailureDueToLocking() {
        Long showId = 1L;
        Long theaterId = 1L;
        Long userId = 1L;
        List<Long> seatNumbers = Arrays.asList(1L, 2L, 3L);

        // Mocking seat locking failure
        when(seatService.lockSeats(theaterId, seatNumbers)).thenReturn(false);

        // Call the method and expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.bookSeats(showId, theaterId, userId, seatNumbers);
        });

        // Verify the exception message
        assertEquals("Unable to acquire locks for seats, please try again later.", exception.getMessage());
        verify(seatService, times(1)).lockSeats(theaterId, seatNumbers);
        verify(seatRepository, times(0)).save(any(Seat.class));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(seatService, times(0)).releaseLocks(theaterId, seatNumbers);
    }

    @Test
    void testBookingFailureDueToUnavailableSeat() {
        Long showId = 1L;
        Long theaterId = 1L;
        Long userId = 1L;
        List<Long> seatNumbers = Arrays.asList(1L, 2L, 3L);

        // Mocking seat locking
        when(seatService.lockSeats(theaterId, seatNumbers)).thenReturn(true);

        // Mocking seat repository response for an unavailable seat
        when(seatRepository.findBySeatNumberAndTheaterIdAndBookedFalse(1L, theaterId)).thenReturn(Optional.empty());

        // Call the method and expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.bookSeats(showId, theaterId, userId, seatNumbers);
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Seat [1, 2, 3] is not available in theater 1"));
        verify(seatService, times(1)).lockSeats(theaterId, seatNumbers);
        verify(seatRepository, times(1)).findBySeatNumberAndTheaterIdAndBookedFalse(1L, theaterId);
        verify(seatRepository, times(0)).save(any(Seat.class));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(seatService, times(1)).releaseLocks(theaterId, seatNumbers);
    }
}

