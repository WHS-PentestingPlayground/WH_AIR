package com.WHS.whair.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.WHS.whair.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
  
  /* 특정 항공편의 좌석 정보 조회 */

  // 📋 전체 좌석 정보 → List<Seat> (엔티티 전체)
  @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId ORDER BY seat.seatNumber")
  List<Seat> findAllSeatsByFlightId(@Param("flightId") Long flightId);
  
  // 📋 클래스별 좌석 정보 → List<Seat> (가격, 상태 포함)
  @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass ORDER BY seat.seatNumber")
  List<Seat> findSeatsByFlightIdAndClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
  
  // 📊 전체 예약 현황 → List<String> ["11A", "12F"]
  @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.isReserved = true")
  List<String> findReservedSeatNumbers(@Param("flightId") Long flightId);
  
  // 🔴 예약된 좌석 → List<String> ["11A", "12F"] (UI 빨간색)
  @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = true")
  List<String> findReservedSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);
  
  // 🔵 선택 가능 좌석 → List<String> ["11B", "11C", ...] (UI 하늘색)
  @Query("SELECT seat.seatNumber FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass AND seat.isReserved = false")
  List<String> findAvailableSeatNumbersByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);

  // 📋 클래스별 예약 현황 → List<Object[]> [["11A", true], ["12F", false], ...]
  @Query("SELECT seat.seatNumber, seat.isReserved FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatClass = :seatClass")
  List<Object[]> findSeatStatusByClass(@Param("flightId") Long flightId, @Param("seatClass") String seatClass);

  /* 좌석 유효성 검증 */

  // 항공편의 전체 좌석 조회
  @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
  long countExistingSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
  

  // 선택 좌석 예약 가능 여부 확인
  @Query("SELECT COUNT(seat) FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers AND seat.isReserved = false")
  long countAvailableSeats(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
  
  // 선택 좌석 클래스 정보 조회
  @Query("SELECT DISTINCT seat.seatClass FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
  List<String> findSeatClassesBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
  
  // 좌석 예약 상태 일괄 확인
  @Query("SELECT seat.seatNumber, seat.isReserved FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
  List<Object[]> findSeatStatusBySeatNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
  
  // 특정 좌석 번호들로 좌석 엔티티 조회
  @Query("SELECT seat FROM Seat seat WHERE seat.flight.id = :flightId AND seat.seatNumber IN :seatNumbers")
  List<Seat> findSeatsByFlightIdAndNumbers(@Param("flightId") Long flightId, @Param("seatNumbers") List<String> seatNumbers);
  
  /* 좌석 예약 상태 업데이트 */
 
  // 좌석 예약 처리
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

@Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatClass = 'business' AND s.isReserved = false")
List<Seat> findAvailableBusinessSeats(@Param("flightId") Long flightId);
    
    List<Seat> findByFlightId(Long flightId);

