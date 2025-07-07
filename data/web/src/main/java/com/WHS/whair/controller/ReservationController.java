package com.WHS.whair.controller;

import com.WHS.whair.dto.PaymentRequestDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.ReservationService;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// =================================================================
// ReservationController - STEP 2 & 3: 탑승자 정보 입력 및 결제 처리 API
// =================================================================
// 
// [주요 기능]
// - 탑승자 정보 검증 API (STEP 2)
// - 사용자 포인트/쿠폰 조회 API (STEP 3)
// - 가격 계산 API (STEP 3)
// - 예약 완료 처리 API (STEP 3)
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // =================================================================
    // 현재 사용자 정보 관련 API
    // =================================================================

    // 현재 로그인된 사용자 정보 조회 API
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromCookie(request);
            if (token == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(error);
            }

            String username = jwtUtil.validateAndExtractUsername(token);
            if (username == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "유효하지 않은 토큰입니다.");
                return ResponseEntity.status(401).body(error);
            }

            User user = userService.findByName(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getId());
            response.put("username", user.getName());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "사용자 정보를 가져오는데 실패했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // JWT 토큰 추출 헬퍼 메서드
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // =================================================================
    // STEP 2: 탑승자 정보 관련 API
    // =================================================================

    // 탑승자 정보 유효성 검증 API
    @PostMapping("/validate-passengers")
    public ResponseEntity<Map<String, Object>> validatePassengerInfo(@RequestBody Map<String, Object> requestData) {
        try {
            List<Map<String, String>> passengers = (List<Map<String, String>>) requestData.get("passengers");
            
            if (passengers == null || passengers.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "탑승자 정보를 입력해주세요.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = reservationService.validatePassengerInfo(passengers);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "탑승자 정보 검증 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // =================================================================
    // STEP 3: 결제 처리 관련 API
    // =================================================================

    // 사용자 포인트 조회 API
    @GetMapping("/users/{userId}/points")
    public ResponseEntity<Map<String, Object>> getUserPoints(@PathVariable Long userId) {
        try {
            Integer points = reservationService.getUserPoints(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("points", points);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "포인트 정보를 가져오는데 실패했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 사용자 쿠폰 조회 API
    @GetMapping("/users/{userId}/coupon")
    public ResponseEntity<Map<String, Object>> getUserCoupon(@PathVariable Long userId) {
        try {
            String coupon = reservationService.getUserCoupon(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("coupon", coupon);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "쿠폰 정보를 가져오는데 실패했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 최종 결제 금액 계산 API
    @PostMapping("/calculate-price")
    public ResponseEntity<Map<String, Object>> calculateFinalPrice(@RequestBody Map<String, Object> requestData) {
        try {
            BigDecimal totalAmount = new BigDecimal(requestData.get("totalAmount").toString());
            BigDecimal usedPoints = requestData.get("usedPoints") != null ? 
                                  new BigDecimal(requestData.get("usedPoints").toString()) : BigDecimal.ZERO;
            String couponCode = (String) requestData.get("couponCode");
            
            // 쿠폰 할인 계산
            BigDecimal couponDiscount = reservationService.calculateCouponDiscount(couponCode);
            BigDecimal discountAmount = totalAmount.multiply(couponDiscount);
            
            // 최종 금액 계산
            BigDecimal finalAmount = totalAmount.subtract(usedPoints).subtract(discountAmount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("couponDiscount", discountAmount);
            result.put("finalAmount", finalAmount);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "가격 계산 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 예약 완료 처리 API (STEP 3 최종 단계)
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processReservation(@RequestBody PaymentRequestDto paymentRequest,
                                                                 @RequestParam Long userId) {
        try {
            if (paymentRequest == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "예약 정보가 필요합니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = reservationService.processReservation(paymentRequest, userId);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "예약 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 