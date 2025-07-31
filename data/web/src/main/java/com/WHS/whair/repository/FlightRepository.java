package com.WHS.whair.repository;

import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.entity.Flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    // 항공편 검색
    @Query("SELECT new com.WHS.whair.dto.FlightSearchResultDTO(" +
           "flight.id, flight.flightNumber, flight.departureAirport, flight.arrivalAirport, " +
           "flight.departureTime, flight.arrivalTime, flight.airline, flight.aircraftModel, " +
           "seat.seatClass, MIN(seat.seatPrice), MIN(seat.fuelPrice)) " +
           "FROM Flight flight JOIN flight.seats seat " +
           "WHERE flight.departureAirport = :departureAirport " +
           "AND flight.arrivalAirport = :arrivalAirport " +
           "AND FUNCTION('DATE', flight.departureTime) = :departureDate " +
           "AND FUNCTION('DATE', flight.arrivalTime) = :arrivalDate " +
           "AND seat.seatClass IN :seatClasses " +
           "GROUP BY flight.id, flight.flightNumber, flight.departureAirport, flight.arrivalAirport, " +
           "flight.departureTime, flight.arrivalTime, flight.airline, flight.aircraftModel, seat.seatClass")
    List<FlightSearchResultDTO> searchFlightsWithPrice(
        @Param("departureAirport") String departureAirport,
        @Param("arrivalAirport") String arrivalAirport,
        @Param("departureDate") LocalDate departureDate,
        @Param("arrivalDate") LocalDate arrivalDate,
        @Param("seatClasses") List<String> seatClasses
    );
    
    // 항공편 상세 정보 조회
    @Query("SELECT new com.WHS.whair.dto.FlightSearchResultDTO(" +
           "flight.id, flight.flightNumber, flight.departureAirport, flight.arrivalAirport, " +
           "flight.departureTime, flight.arrivalTime, flight.airline, flight.aircraftModel, " +
           "seat.seatClass, MIN(seat.seatPrice), MIN(seat.fuelPrice)) " +
           "FROM Flight flight JOIN flight.seats seat " +
           "WHERE flight.id = :flightId " +
           "AND seat.seatClass = :seatClass " +
           "GROUP BY flight.id, flight.flightNumber, flight.departureAirport, flight.arrivalAirport, " +
           "flight.departureTime, flight.arrivalTime, flight.airline, flight.aircraftModel, seat.seatClass")
    FlightSearchResultDTO findFlightDetailByIdAndSeatClass(
        @Param("flightId") Long flightId,
        @Param("seatClass") String seatClass
    );
} 
