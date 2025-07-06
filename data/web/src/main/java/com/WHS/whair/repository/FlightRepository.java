package com.WHS.whair.repository;

import com.WHS.whair.entity.Flight;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.dto.FlightSearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =================================================================
 * FlightRepository - 항공편 및 좌석 관리 통합 리포지토리
 * =================================================================
 * 
 * [섹션 구성]
 * 1. 항공편 검색 관련 (기존 기능)
 * 2. 좌석 현황 조회 관련 (신규 추가)
 * 3. 예약 처리 관련 (신규 추가)
 * 4. 좌석 유효성 검증 관련 (신규 추가)
 * 
 * [데이터베이스 구조]
 * - seats 테이블: seat_number, class, is_reserved, seat_price, fuel_price
 * - reservations 테이블: 예약 관리 (별도 테이블)
 */
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // =================================================================
    // 1. 항공편 검색 관련 (기존 기능)
    // =================================================================
    
    /**
     * 복잡한 조건으로 항공편 검색
     * - 메서드명 기반 쿼리로는 구현 불가능한 복잡한 조건 처리
     * - JOIN을 통한 좌석 정보 포함 검색
     */
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

    /**
     * 항공편 검색 결과를 DTO로 반환 (가격 정보 포함)
     * - MIN 함수를 사용하여 각 클래스별 최저가 조회
     * - GROUP BY를 통한 중복 제거
     */
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
    
    /**
     * 항공편 ID와 좌석 클래스로 상세 정보 조회
     * - 특정 항공편의 특정 클래스 정보만 조회
     * - 좌석 선택 페이지에서 사용
     */
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

    // =================================================================
    // 2. 좌석 현황 조회 관련 (신규 추가)
    // =================================================================
    
    // 항공편 전체 좌석 조회
    @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId ORDER BY seat.seatNumber")
    List<Seat> findAllSeatsByFlightId(@Param("flightId") Long flightId);
    
    // 항공편 특정 클래스 좌석 조회
    @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass ORDER BY seat.seatNumber")
    List<Seat> findSeatsByFlightIdAndClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
    
    /**
     * 예약된 좌석 번호 목록 조회
     * - is_reserved = true인 좌석들의 번호만 조회
     * - 좌석 상태 표시에 사용
     */
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.isReserved = true")
    List<String> findReservedSeatNumbers(@Param("flightId") Long flightId);
    
    /**
     * 특정 클래스의 예약된 좌석 번호 목록 조회
     * - 클래스별 예약 현황 조회
     * - 좌석 선택 제한에 사용
     */
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = true")
    List<String> findReservedSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
    
    /**
     * 선택 가능한 좌석 번호 목록 조회
     * - is_reserved = false인 좌석들의 번호만 조회
     * - 좌석 선택 옵션 제공에 사용
     */
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = false")
    List<String> findAvailableSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);

    // =================================================================
    // 3. 예약 처리 관련 (신규 추가)
    // =================================================================
    
    /**
     * 좌석 예약 상태 업데이트
     * - 선택한 좌석들을 예약됨(is_reserved = true)으로 변경
     * - 예약 처리 시 사용
     * - @Modifying: UPDATE 쿼리 실행을 위한 어노테이션
     */
    @Modifying
    @Transactional
    @Query("UPDATE Seat seat SET seat.isReserved = true " +
           "WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
    int reserveSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    /**
     * 좌석 예약 취소 (필요시 사용)
     * - 예약된 좌석을 다시 선택 가능(is_reserved = false)으로 변경
     * - 예약 취소 기능에 사용
     */
    @Modifying
    @Transactional
    @Query("UPDATE Seat seat SET seat.isReserved = false " +
           "WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = true")
    int cancelReservation(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);

    // =================================================================
    // 4. 좌석 유효성 검증 관련 (신규 추가)
    // =================================================================
    
    /**
     * 좌석 존재 여부 확인
     * - 입력한 좌석 번호가 해당 항공편에 실제 존재하는지 확인
     * - 잘못된 좌석 번호 입력 방지
     */
    @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    long countExistingSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    /**
     * 선택한 좌석들이 모두 예약 가능한지 확인
     * - is_reserved = false인 좌석만 카운트
     * - 동시 예약 방지를 위한 검증에 사용
     */
    @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
    long countAvailableSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    /**
     * 특정 좌석의 클래스 정보 조회
     * - 선택한 좌석이 요청한 클래스와 일치하는지 확인
     * - 클래스 제한 검증에 사용
     */
    @Query("SELECT DISTINCT seat.seatClass FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    List<String> findSeatClassesBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    /**
     * 좌석 예약 상태 일괄 확인
     * - 여러 좌석의 예약 상태를 한 번에 조회
     * - 좌석 상태 확인에 사용
     */
    @Query("SELECT seat.seatNumber, seat.isReserved FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    List<Object[]> findSeatStatusBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
} 
