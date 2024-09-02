package com.intuit.BookMyTicket.service;

import com.intuit.BookMyTicket.model.City;
import com.intuit.BookMyTicket.model.Theater;
import com.intuit.BookMyTicket.service.implementation.CityTheaterServiceImpl;

import java.util.List;

public interface CityTheaterService {
    List<CityTheaterServiceImpl.CityDTO> getAllCities();
    List<Theater> getTheatersByCity(Long cityId);
    City saveCity(City city);
    Theater saveTheater(Long cityId, Theater theater);
}
