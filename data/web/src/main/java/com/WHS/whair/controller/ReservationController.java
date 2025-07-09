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
import com.WHS.whair.entity.User;
import com.WHS.whair.repository.UserRepository;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    private final UserRepository userRepository;
    
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

    // 사용자 쿠폰 조회 API
    @GetMapping("/user/coupons")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserCoupons(HttpServletRequest request) {
        try {
            // 세션에서 사용자 정보 가져오기
            User sessionUser = (User) request.getSession().getAttribute("user");
            if (sessionUser == null) {
                return ResponseEntity.status(401).build();
            }

            // DB에서 최신 사용자 정보 조회
            User user = userRepository.findById(sessionUser.getId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).build();
            }

            Map<String, Object> response = new HashMap<>();

            // 쿠폰 정보 파싱
            String userCoupon = user.getCoupon();
            Map<String, Object> availableCoupons = new HashMap<>();

            if (userCoupon != null && !userCoupon.trim().isEmpty()) {
                // 쿠폰명에서 숫자 추출
                String discountStr = userCoupon.replaceAll("[^0-9]", "");
                double discountRate = 0.0;
                if (!discountStr.isEmpty()) {
                    int discount = Integer.parseInt(discountStr);
                    discountRate = discount / 100.0;
                }
                // DisplayName은 DB에 저장된 이름 그대로 사용
                availableCoupons.put(userCoupon, discountRate);
            }

            response.put("userCoupon", userCoupon);
            response.put("availableCoupons", availableCoupons);
            response.put("points", user.getPoint());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 