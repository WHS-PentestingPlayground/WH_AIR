package com.WHS.whair.repository;

import com.WHS.whair.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // =================================================================
    // 좌석 현황 조회 관련
    // =================================================================
    
    // 항공편 전체 좌석 조회
    @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId ORDER BY seat.seatNumber")
    List<Seat> findAllSeatsByFlightId(@Param("flightId") Long flightId);
    
    // 항공편 특정 클래스 좌석 조회
    @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass ORDER BY seat.seatNumber")
    List<Seat> findSeatsByFlightIdAndClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
    
    // 예약된 좌석 번호 목록 조회
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.isReserved = true")
    List<String> findReservedSeatNumbers(@Param("flightId") Long flightId);
    
    // 특정 클래스의 예약된 좌석 번호 목록 조회
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = true")
    List<String> findReservedSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
    
    // 선택 가능한 좌석 번호 목록 조회
    @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = false")
    List<String> findAvailableSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);

    // =================================================================
    // 좌석 유효성 검증 관련
    // =================================================================
    
    // 좌석 존재 여부 확인
    @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    long countExistingSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 선택한 좌석들이 모두 예약 가능한지 확인
    @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
    long countAvailableSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 특정 좌석의 클래스 정보 조회
    @Query("SELECT DISTINCT seat.seatClass FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    List<String> findSeatClassesBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 좌석 예약 상태 일괄 확인
    @Query("SELECT seat.seatNumber, seat.isReserved FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    List<Object[]> findSeatStatusBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 선택한 좌석들의 총 가격 계산
    @Query("SELECT SUM(seat.seatPrice + seat.fuelPrice) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    java.math.BigDecimal calculateTotalPrice(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 좌석 예약 가능 여부 검증
    @Query("SELECT COUNT(seat) = :seatCount FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
    boolean validateSeatAvailability(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers, @Param("seatCount") long seatCount);
    
    // 좌석 번호로 좌석 엔티티 조회
    @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
    List<Seat> findSeatsBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);

    // =================================================================
    // 좌석 예약 상태 관리
    // =================================================================
    
    // 좌석 예약 상태 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE Seat seat SET seat.isReserved = true " +
           "WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
    int reserveSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
    
    // 좌석 예약 취소
    @Modifying
    @Transactional
    @Query("UPDATE Seat seat SET seat.isReserved = false " +
           "WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = true")
    int cancelReservation(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
} 