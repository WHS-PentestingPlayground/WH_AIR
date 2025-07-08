package com.WHS.whair.controller;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /* STEP 2 & 3 : 탑승자 정보 + 포인트 결제 처리 API */
    @PostMapping("/finalize")
    public ResponseEntity<Map<String, Object>> finalizeReservation(@RequestBody Map<String, Object> requestData) {
        
        try {
            // 요청 데이터 추출
            Long userId = Long.valueOf(requestData.get("userId").toString());
            Long flightId = Long.valueOf(requestData.get("flightId").toString());
            List<String> seatNumbers = (List<String>) requestData.get("seatNumbers");
            String passengerName = (String) requestData.get("passengerName");
            String passengerBirthStr = (String) requestData.get("passengerBirth");
            
            // 포인트 사용량 (선택적)
            Integer usedPoints = requestData.get("usedPoints") != null ? 
                    Integer.valueOf(requestData.get("usedPoints").toString()) : 0;
            
            LocalDate passengerBirth = LocalDate.parse(passengerBirthStr);
            
            // 예약 생성 (포인트 차감)
            List<Reservation> reservations = reservationService.createReservations(
                    userId, flightId, seatNumbers, passengerName, passengerBirth, usedPoints);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "예약이 완료되었습니다.");
            response.put("reservationCount", reservations.size());
            response.put("reservationIds", reservations.stream().map(Reservation::getId).toList());
            response.put("usedPoints", usedPoints);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /* 사용자별 예약 목록 조회 API */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserReservations(@PathVariable Long userId) {
        try {
            List<Reservation> reservations = reservationService.getUserReservations(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservations", reservations);
            response.put("count", reservations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "예약 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /* 예약 취소 API */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam Long userId) {
        
        try {
            reservationService.cancelReservation(reservationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "예약이 취소되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /* 예약 상세 조회 API */
    @GetMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservationDetail(@PathVariable Long reservationId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "예약 상세 조회가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "예약 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 