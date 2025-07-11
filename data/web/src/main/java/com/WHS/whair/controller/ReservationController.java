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
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    private final UserRepository userRepository;

    /* 쿠폰 적용 API */
    @PostMapping("/apply-coupon")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getId() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = user.getId();
            log.debug("쿠폰 적용 요청: userId={}", userId);

            // 요청 파라미터 추출
            String couponCode = (String) request.get("couponCode");
            String targetPriceType = (String) request.get("targetPriceType");
            Long flightId = Long.valueOf(request.get("flightId").toString());
            String seatNumber = (String) request.get("seatNumber");

            // 입력값 검증
            if (couponCode == null || targetPriceType == null || flightId == null || seatNumber == null) {
                response.put("success", false);
                response.put("message", "잘못된 요청입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 쿠폰 적용
            Map<String, Object> result = reservationService.applyCoupon(userId, couponCode, targetPriceType, flightId, seatNumber);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("쿠폰 적용 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 적용 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /* STEP 2 & 3 : 탑승자 정보 + 포인트 결제 처리 API */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReservations(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) httpRequest.getAttribute("user");
            if (user == null || user.getId() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = user.getId();
            log.debug("예약 생성 요청: userId={}", userId);

            // 요청 파라미터 추출
            Long flightId = Long.valueOf(request.get("flightId").toString());
            List<String> seatNumbers = (List<String>) request.get("seatNumbers");
            String passengerName = (String) request.get("passengerName");
            String passengerBirthStr = (String) request.get("passengerBirth");
            Integer usedPoints = request.get("usedPoints") != null ? 
                    Integer.valueOf(request.get("usedPoints").toString()) : 0;
            String seatCoupon = request.get("seatCoupon") != null ? request.get("seatCoupon").toString() : null;
            String fuelCoupon = request.get("fuelCoupon") != null ? request.get("fuelCoupon").toString() : null;

            LocalDate passengerBirth = LocalDate.parse(passengerBirthStr);
            
            // 예약 생성
            reservationService.createReservations(userId, flightId, seatNumbers, passengerName, passengerBirth, usedPoints, seatCoupon, fuelCoupon);
            
            // 간소화된 응답 (예약 완료 확인만 - 엔티티 정보 제외)
            response.put("success", true);
            response.put("message", "예약이 완료되었습니다.");
            response.put("reservationCount", seatNumbers.size());
            response.put("seatNumbers", seatNumbers);
            response.put("passengerName", passengerName);
            response.put("flightId", flightId);
            
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
            if (user == null || user.getId() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = user.getId();
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
            if (user == null || user.getId() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = user.getId();
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
            if (user == null || user.getId() == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = user.getId();
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