package com.intuit.BookMyTicket.service.implementation;
import com.intuit.BookMyTicket.model.City;
import com.intuit.BookMyTicket.model.Theater;
import com.intuit.BookMyTicket.repository.CityRepository;
import com.intuit.BookMyTicket.repository.TheaterRepository;
import com.intuit.BookMyTicket.service.CityTheaterService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CityTheaterServiceImpl implements CityTheaterService {

    private static final Logger logger = LoggerFactory.getLogger(CityTheaterServiceImpl.class);

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final CircuitBreaker circuitBreaker;


    @Autowired
    public CityTheaterServiceImpl(CityRepository cityRepository, TheaterRepository theaterRepository,
                                  CircuitBreaker circuitBreaker) {
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
        this.circuitBreaker = circuitBreaker;
    }

    @Data
    public class CityDTO {
        private Long id;
        private String name;
        private List<TheaterDTO> theaters;
    }

    @Data
    public class TheaterDTO {
        private Long id;
        private String name;
    }

    public List<CityDTO> getAllCities() {
        return cityRepository.findAll().stream().map(city -> {
            CityDTO dto = new CityDTO();
            dto.setId(city.getId());
            dto.setName(city.getName());
            dto.setTheaters(city.getTheaters().stream().map(theater -> {
                TheaterDTO theaterDto = new TheaterDTO();
                theaterDto.setId(theater.getId());
                theaterDto.setName(theater.getName());
                return theaterDto;
            }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Theater> getTheatersByCity(Long cityId) {
        logger.info("Fetching theaters for cityId: {}", cityId);
        return executeWithCircuitBreaker(() -> theaterRepository.findByCityId(cityId), t -> fallbackGetTheatersByCity(cityId, t));
    }

    private <T> T executeWithCircuitBreaker(Supplier<T> supplier, java.util.function.Function<Throwable, T> fallback) {
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, supplier).get();
        } catch (Exception e) {
            logger.error("Error during Circuit Breaker execution", e);
            return fallback.apply(e);
        }
    }

    // Fallback methods
    private List<City> fallbackGetAllCities(Throwable t) {
        logger.error("Fallback: Error fetching all cities", t);
        return Collections.emptyList();
    }

    private List<Theater> fallbackGetTheatersByCity(Long cityId, Throwable t) {
        logger.error("Fallback: Error fetching theaters for cityId: {}", cityId, t);
        return Collections.emptyList();
    }

    @Override
    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    @Override
    public Theater saveTheater(Long cityId, Theater theater) {
        City city = cityRepository.findById(cityId).orElseThrow(() -> new RuntimeException("City not found"));
        theater.setCity(city);
        return theaterRepository.save(theater);
    }
}