package com.intuit.BookMyTicket.repository;

import com.intuit.BookMyTicket.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
