package com.WHS.whair.repository;

import com.WHS.whair.entity.Flight;
import com.WHS.whair.dto.FlightSearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 복잡한 조건으로 항공편 검색
 * - 메서드명 기반 쿼리로는 구현 불가능한 복잡한 조건
 */
public interface FlightRepository extends JpaRepository<Flight, Long> {
    // 항공편 검색
    @Query("SELECT DISTINCT flight FROM Flight flight JOIN flight.seats seat " +  // JOIN으로 좌석 정보 포함
    "WHERE flight.departureAirport = :departureAirport " +                  // 출발 공항 조건
    "AND flight.arrivalAirport = :arrivalAirport " +                        // 도착 공항 조건
    "AND FUNCTION('DATE', flight.departureTime) = :departureDate " +        // 출발 날짜만 추출하여 비교
    "AND FUNCTION('DATE', flight.arrivalTime) = :arrivalDate " +            // 도착 날짜만 추출하여 비교
    "AND seat.seatClass = :seatClass")                                      // 좌석 등급 필터
    List<Flight> searchFlights(
        @Param("departureAirport") String departureAirport,
        @Param("arrivalAirport") String arrivalAirport,
        @Param("departureDate") LocalDate departureDate,
        @Param("arrivalDate") LocalDate arrivalDate,
        @Param("seatClass") String seatClass
    );

    // 항공편 검색 결과 반환
    @Query("SELECT new com.WHS.whair.dto.FlightSearchResultDTO(" +
           "flight.id, flight.flightNumber, flight.departureAirport, flight.arrivalAirport, " +
           "flight.departureTime, flight.arrivalTime, flight.airline, flight.aircraftModel, " +
           "seat.seatClass, seat.price, seat.isReserved) " +
           "FROM Flight flight JOIN flight.seats seat " +
           "WHERE flight.departureAirport = :departureAirport " +
           "AND flight.arrivalAirport = :arrivalAirport " +
           "AND FUNCTION('DATE', flight.departureTime) = :departureDate " +
           "AND FUNCTION('DATE', flight.arrivalTime) = :arrivalDate " +
           "AND seat.seatClass = :seatClass")
    List<FlightSearchResultDTO> searchFlightsWithPrice(
        @Param("departureAirport") String departureAirport,
        @Param("arrivalAirport") String arrivalAirport,
        @Param("departureDate") LocalDate departureDate,
        @Param("arrivalDate") LocalDate arrivalDate,
        @Param("seatClass") String seatClass
    );
} 