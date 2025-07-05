//package com.WHS.whair.service;
//
//import com.WHS.whair.entity.Flight;
//import com.WHS.whair.dto.FlightSearchResultDTO;
//import com.WHS.whair.repository.FlightRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//public class FlightService {
//    @Autowired
//    private FlightRepository flightRepository;
//
//    // 항공권 검색 (기존 Flight 엔티티 반환)
//    public List<Flight> searchFlights(String departureAirport, String arrivalAirport, LocalDate departureDate, LocalDate arrivalDate, String seatClass) {
//        return flightRepository.searchFlights(departureAirport, arrivalAirport, departureDate, arrivalDate, seatClass);
//    }
//
//    // 항공권 검색 (DTO 반환 - 가격 정보 포함)
//    public List<FlightSearchResultDTO> searchFlightsWithPrice(String departureAirport, String arrivalAirport, LocalDate departureDate, LocalDate arrivalDate, String seatClass) {
//        return flightRepository.searchFlightsWithPrice(departureAirport, arrivalAirport, departureDate, arrivalDate, seatClass);
//    }
//}