package com.intuit.BookMyTicket.controller;
import com.intuit.BookMyTicket.model.City;
import com.intuit.BookMyTicket.model.Theater;
import com.intuit.BookMyTicket.service.CityTheaterService;
import com.intuit.BookMyTicket.service.implementation.CityTheaterServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CityTheaterController {

    private final CityTheaterService cityTheaterService;

    public CityTheaterController(CityTheaterService cityTheaterService) {
        this.cityTheaterService = cityTheaterService;
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityTheaterServiceImpl.CityDTO>> getAllCities() {
        List<CityTheaterServiceImpl.CityDTO> cities = cityTheaterService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/cities/{cityId}/theaters")
    public ResponseEntity<List<Theater>> getTheatersByCity(@PathVariable Long cityId) {
        List<Theater> theaters = cityTheaterService.getTheatersByCity(cityId);
        return ResponseEntity.ok(theaters);
    }

    @PostMapping("/cities")
    public ResponseEntity<City> createCity(@RequestBody City city) {
        City savedCity = cityTheaterService.saveCity(city);
        return ResponseEntity.ok(savedCity);
    }

    @PostMapping("/cities/{cityId}/theaters")
    public ResponseEntity<Theater> createTheater(@PathVariable Long cityId, @RequestBody Theater theater) {
        Theater savedTheater = cityTheaterService.saveTheater(cityId, theater);
        return ResponseEntity.ok(savedTheater);
    }

}

