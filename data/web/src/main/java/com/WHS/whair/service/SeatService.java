package com.WHS.whair.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
  
import com.WHS.whair.repository.SeatRepository;

@Service
public class SeatService {

  @Autowired
  private SeatRepository seatRepository;

  /* 특정 항공편의 좌석 정보 조회 */
  public Map<String, List<String>> getSeatStatus(Long flightId, String seatClass) {
    try {
      List<Object[]> seatStatus = seatRepository.findSeatStatusByClass(flightId, seatClass);

      List<String> reservedSeats = new ArrayList<>();
      List<String> availableSeats = new ArrayList<>();

      // 쿼리 결과 분류
      for (Object[] row :seatStatus) {
        String seatNumber = (String) row[0];
        Boolean isReserved = (Boolean) row[1];

        if(isReserved) {
          reservedSeats.add(seatNumber);
        } else {
          availableSeats.add(seatNumber);
        }
      }
      return Map.of(
        "reservedSeats", reservedSeats,
        "availableSeats", availableSeats
      );
    } catch (Exception e) {
      return Map.of(
        "reservedSeats", new ArrayList<String>(),
        "availableSeats", new ArrayList<String>()
      );
    }
  }

  /* 좌석 유효성 검증 */
  public boolean validateSeatSelection(Long flightId, List<String> selectedSeats) {
    try {
      // 1. 좌석 존재 여부 확인
      long existingSeatsCount = seatRepository.countExistingSeats(flightId, selectedSeats);
      if (existingSeatsCount != selectedSeats.size()) {
          return false;
      }
      
      // 2. 좌석 예약 가능 여부 확인
      long availableSeats = seatRepository.countAvailableSeats(flightId, selectedSeats);
      if (availableSeats != selectedSeats.size()) {
          return false;
      }

      // 3. 추가 검증: 좌석 클래스 일치 여부 확인
      List<String> seatClasses = seatRepository.findSeatClassesBySeatNumbers(flightId, selectedSeats);
      if (seatClasses.size() > 1) {
        return false;
      }
        
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /* 좌석 예약 상태 업데이트 */

  // 좌석 예약 처리
  public boolean reserveSeats(Long flightId, List<String> seatNumbers) {
    try {
      if (flightId == null || seatNumbers == null || seatNumbers.isEmpty()) {
        return false;
      }
      
      int reservedCount = seatRepository.reserveSeats(flightId, seatNumbers);
      return reservedCount == seatNumbers.size();
    } catch (Exception e) {
        return false;
    }
  }

  // 좌석 예약 취소
  public boolean cancelReservation(Long flightId, List<String> seatNumbers) {
    try {
      if (flightId == null || seatNumbers == null || seatNumbers.isEmpty()) {
        return false;
      }

      int cancelledCount = seatRepository.cancelReservation(flightId, seatNumbers);
      return cancelledCount == seatNumbers.size();
    } catch (Exception e) {
      return false;
    }
  }
}
