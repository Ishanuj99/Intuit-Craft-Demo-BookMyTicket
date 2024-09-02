package com.intuit.BookMyTicket.service.implementation;

import com.intuit.BookMyTicket.model.Seat;
import com.intuit.BookMyTicket.repository.SeatRepository;
import com.intuit.BookMyTicket.service.SeatService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

    private final SeatRepository seatRepository;
    private final StringRedisTemplate redisTemplate;
    private final CircuitBreaker circuitBreaker;

    public SeatServiceImpl(SeatRepository seatRepository, StringRedisTemplate redisTemplate, CircuitBreaker circuitBreaker) {
        this.seatRepository = seatRepository;
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @Cacheable(value = "availableSeats", key = "#theaterId")
    public List<Seat> getAvailableSeats(Long theaterId) {
        logger.info("Fetching available seats for theaterId: {}", theaterId);
        return seatRepository.findByTheaterId(theaterId).stream()
                .filter(seat -> !seat.isBooked())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean lockSeats(Long theaterId, List<Long> seatNumbers) {
        try {
            for (Long seatNumber : seatNumbers) {
                String seatKey = "seat:" + theaterId + ":" + seatNumber;

                // Attempt to acquire a lock for each individual seat
                boolean seatLocked = redisTemplate.opsForValue().setIfAbsent(seatKey, "locked", 10, TimeUnit.SECONDS);

                if (!seatLocked) {
                    // If any seat fails to lock, release all previously locked seats and return false
                    unlockSeats(theaterId, seatNumbers);
                    logger.warn("Unable to lock seat {} for theaterId: {}", seatNumber, theaterId);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // Handle exception and potentially unlock seats
            unlockSeats(theaterId, seatNumbers);
            logger.error("Exception occurred while locking seats: ", e);
            return false;
        }
    }

    private void unlockSeats(Long theaterId, List<Long> seatNumbers) {
        for (Long seatNumber : seatNumbers) {
            String seatKey = "seat:" + theaterId + ":" + seatNumber;
            redisTemplate.delete(seatKey);
        }
    }

    public void releaseLocks(Long showId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String seatKey = "seat:" + showId + ":" + seatId;
            redisTemplate.delete(seatKey);
        }
    }

    @Override
    @Transactional
    public boolean bookSeats(Long theaterId, List<Long> seatNumbers) {
        String lockKey = "lock:theater:" + theaterId;
        boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.MINUTES);
        if (!locked) {
            logger.warn("Unable to acquire lock for theaterId: {}", theaterId);
            return false;
        }

        try {
            for (Long seatNumber : seatNumbers) {
                Seat seat = seatRepository.findByTheaterIdAndSeatNumber(theaterId, seatNumber);
                if (seat == null || seat.isBooked()) {
                    logger.error("Seat {} not available for theaterId: {}", seatNumber, theaterId);
                    return false;
                }
                seat.setBooked(true);
                seatRepository.save(seat);
            }
            return true;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public Seat addSeat(Seat seat) {
        return seatRepository.save(seat);
    }
}
