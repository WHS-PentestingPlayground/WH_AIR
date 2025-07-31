package com.WHS.whair.controller;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.service.ReservationService;
import com.WHS.whair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.WHS.whair.entity.User;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;




@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    private final UserRepository userRepository;

    /* 통합 처리 프로세스 */

    // [1단계] : 장바구니 생성
    @PostMapping("/payment/initiate")
    public ResponseEntity<Map<String, Object>> initiatePaymentSession(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 사용자 인증 확인
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getName() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // DB에서 사용자 정보 조회
            User dbUser = userRepository.findByName(user.getName()).orElse(null);
            if (dbUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 요청 파라미터 추출
            Long flightId = Long.valueOf(request.get("flightId").toString());
            List<String> seatNumbers = (List<String>) request.get("seatNumbers");

            // 장바구니 생셩 및 세션 ID 발급
            String sessionId = reservationService.initiatePaymentSession(dbUser.getId(), flightId, seatNumbers);

            // 응답 반환
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("message", "결제 세션이 생성되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 세션 생성 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "결제 세션 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // [2단계] : 쿠폰 적용
    @PostMapping("/payment/apply-coupon")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = request.get("sessionId").toString();
            String couponCode = request.get("couponCode").toString();
            String targetPriceType = request.get("targetPriceType").toString();

            Map<String, Object> result = reservationService.applyCouponToSession(sessionId, couponCode, targetPriceType);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("쿠폰 적용 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 적용 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // [3단계] : 최종 결제 및 예약 
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReservations(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            // 요청 파라미터 추출
            String sessionId = request.get("sessionId").toString();
            Integer usedPoints = Integer.valueOf(request.get("usedPoints").toString());
            String passengerName = request.get("passengerName").toString();
            LocalDate passengerBirth = LocalDate.parse(request.get("passengerBirth").toString());

            // 예약 생성
            List<Reservation> reservations = reservationService.createReservations(sessionId, usedPoints, passengerName, passengerBirth);

            // 응답 반환
            response.put("success", true);
            response.put("message", "예약이 완료되었습니다.");
            response.put("reservations", reservations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("예약 생성 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /* 예약 조회 API */
    @GetMapping("/my-reservations")
    public ResponseEntity<Map<String, Object>> getMyReservations(HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getName() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // DB에서 사용자 정보 조회
            User dbUser = userRepository.findByName(user.getName()).orElse(null);
            if (dbUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = dbUser.getId();
            log.debug("예약 목록 조회 요청: userId={}", userId);

            List<Reservation> reservations = reservationService.getUserReservations(userId);
            
            response.put("success", true);
            response.put("reservations", reservations);
            response.put("count", reservations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("예약 목록 조회 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "예약 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /* 예약 취소 API */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long reservationId,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getName() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // DB에서 사용자 정보 조회
            User dbUser = userRepository.findByName(user.getName()).orElse(null);
            if (dbUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = dbUser.getId();
            log.debug("예약 취소 요청: userId={}, reservationId={}", userId, reservationId);

            reservationService.cancelReservation(reservationId, userId);
            
            response.put("success", true);
            response.put("message", "예약이 취소되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("예약 취소 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /* 예약 상세 조회 API */
    @GetMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservationDetail(@PathVariable Long reservationId, HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getName() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // DB에서 사용자 정보 조회
            User dbUser = userRepository.findByName(user.getName()).orElse(null);
            if (dbUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = dbUser.getId();
            log.debug("예약 상세 조회 요청: userId={}, reservationId={}", userId, reservationId);

            Reservation reservation = reservationService.getReservationDetail(reservationId, userId);
            
            response.put("success", true);
            response.put("reservation", reservation);
            response.put("message", "예약 상세 조회가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("예약 상세 조회 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "예약 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 