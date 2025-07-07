package com.WHS.whair.repository;

import com.WHS.whair.entity.Flight;
import com.WHS.whair.dto.FlightSearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

// =================================================================
// FlightRepository - 항공편 검색 및 기본 정보 조회 전용
// =================================================================
// 
// [주요 기능]
// - 항공편 검색 (출발지, 도착지, 날짜, 좌석 클래스별)
// - 항공편 상세 정보 조회 (가격 정보 포함)
// 
// [분리된 기능들]
// - 좌석 관련: SeatRepository
// - 예약 관련: ReservationRepository
// - 사용자 관련: UserRepository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // =================================================================
    // 항공편 검색 관련
    // =================================================================
    
    // 복잡한 조건으로 항공편 검색
    // - JOIN을 통한 좌석 정보 포함 검색
    @Query("SELECT DISTINCT flight FROM Flight flight JOIN flight.seats seat " +
           "WHERE flight.departureAirport = :departureAirport " +
           "AND flight.arrivalAirport = :arrivalAirport " +
           "AND FUNCTION('DATE', flight.departureTime) = :departureDate " +
           "AND FUNCTION('DATE', flight.arrivalTime) = :arrivalDate " +
           "AND seat.seatClass = :seatClass")
    List<Flight> searchFlights(
        @Param("departureAirport") String departureAirport,
        @Param("arrivalAirport") String arrivalAirport,
        @Param("departureDate") LocalDate departureDate,
        @Param("arrivalDate") LocalDate arrivalDate,
        @Param("seatClass") String seatClass
    );

    // 항공편 검색 결과를 DTO로 반환 (가격 정보 포함)
    // - MIN 함수를 사용하여 각 클래스별 최저가 조회
    // - GROUP BY를 통한 중복 제거
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
    
    // 항공편 ID와 좌석 클래스로 상세 정보 조회
    // - 특정 항공편의 특정 클래스 정보만 조회
    // - 좌석 선택 페이지에서 사용
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
