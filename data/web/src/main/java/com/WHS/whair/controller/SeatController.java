package com.WHS.whair.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody; 
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import com.WHS.whair.service.SeatService;

@RequestMapping("/api/flights")
@RestController
public class SeatController {

  @Autowired
  private SeatService seatService;

  // 좌석 현황 조회 API
  @GetMapping("/{flightId}/seats")
  public ResponseEntity<Map<String, List<String>>> getSeatMap(@PathVariable Long flightId, @RequestParam String seatClass) {
    try {
      Map<String, List<String>> seatData = seatService.getSeatStatus(flightId, seatClass);
      return ResponseEntity.ok(seatData);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  // 예약 가능한 좌석 조회 API
  @GetMapping("/{flightId}/available-seats")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getAvailableSeats(@PathVariable Long flightId, @RequestParam String seatClass) {
      try {
          // SeatService의 getSeatStatus를 사용해서 availableSeats 추출
          Map<String, List<String>> seatStatus = seatService.getSeatStatus(flightId, seatClass);
          List<String> availableSeats = seatStatus.get("availableSeats");
          
          Map<String, Object> response = new HashMap<>();
          response.put("availableSeats", availableSeats);
          response.put("count", availableSeats.size());
          
          return ResponseEntity.ok(response);
      } catch (Exception e) {
          Map<String, Object> error = new HashMap<>();
          error.put("success", false);
          error.put("message", "좌석 정보를 가져오는데 실패했습니다.");
          return ResponseEntity.internalServerError().body(error);
      }
  }

  // 좌석 예약 처리 API
  @PostMapping("/{flightId}/book")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> bookSeats(@PathVariable Long flightId, @RequestBody Map<String, Object> bookingData) {
    try {
      // 예약 데이터 검증
      List<String> selectedSeats = (List<String>) bookingData.get("selectedSeats");
      List<Map<String, String>> passengers = (List<Map<String, String>>) bookingData.get("passengers");
      
      if (selectedSeats == null || selectedSeats.isEmpty()) {
          Map<String, Object> error = new HashMap<>();
          error.put("success", false);
          error.put("message", "좌석을 선택해주세요.");
          return ResponseEntity.badRequest().body(error);
      }
      
      // 좌석 유효성 검증
      if (!seatService.validateSeatSelection(flightId, selectedSeats)) {
          Map<String, Object> error = new HashMap<>();
          error.put("success", false);
          error.put("message", "선택한 좌석이 유효하지 않습니다.");
          return ResponseEntity.badRequest().body(error);
      }
      
      // 좌석 예약 처리
      boolean reservationSuccess = seatService.reserveSeats(flightId, selectedSeats);
      
      if (reservationSuccess) {
          Map<String, Object> result = new HashMap<>();
          result.put("success", true);
          result.put("message", "예약이 완료되었습니다.");
          result.put("reservedSeats", selectedSeats);
          return ResponseEntity.ok(result);
      } else {
          Map<String, Object> error = new HashMap<>();
          error.put("success", false);
          error.put("message", "좌석 예약에 실패했습니다.");
          return ResponseEntity.badRequest().body(error);
      }
    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("success", false);
      error.put("message", "예약 처리 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(error);
    }
  }
}
