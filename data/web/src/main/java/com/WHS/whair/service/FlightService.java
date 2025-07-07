package com.WHS.whair.service;

import com.WHS.whair.entity.Flight;
import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.repository.FlightRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// 항공편 검색 및 기본 정보 조회 서비스
@Service
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;

    // 항공편 검색
    public List<FlightSearchResultDTO> searchFlights(String departureAirport, String arrivalAirport, 
                                                     LocalDate departureDate, LocalDate arrivalDate, 
                                                     List<String> seatClasses) {
        return flightRepository.searchFlightsWithPrice(departureAirport, arrivalAirport, 
                                                       departureDate, arrivalDate, seatClasses);
    }

   // 항공편 상세 정보 조회
    public FlightSearchResultDTO getFlightDetail(Long flightId, String seatClass) {
        return flightRepository.findFlightDetailByIdAndSeatClass(flightId, seatClass);
    }
} 
