package com.WHS.whair.service;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.User;
import com.WHS.whair.entity.Flight;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.UserRepository;
import com.WHS.whair.repository.FlightRepository;
import com.WHS.whair.repository.SeatRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;
    
    /* 메모리 기반 장바구니 기능 구현 [1~2단계] */
    // Key: 세션 ID (String, UUID), Value: 해당 세션의 결제 정보 (PendingPayment)
    private static final Map<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    // [1단계] 장바구니 생성 : 결제 프로세스 시작 및 세션ID 발급
    public String initiatePaymentSession(Long userId, Long flightId, List<String> seatNumbers) {
        // 가격 정보 조회
        Map<String, Integer> priceInfo = getPriceInfo(flightId, seatNumbers.get(0));

        // 비어있는 장바구니 객체 생성
        PendingPayment pendingPayment = new PendingPayment(
            userId,
            flightId,
            seatNumbers,
            priceInfo.get("seatPrice"),
            priceInfo.get("fuelPrice")
        );

        // 고유 세션 ID 생성 및 Map에 저장
        String sessionId = UUID.randomUUID().toString();
        pendingPayments.put(sessionId, pendingPayment);

        // 세션ID 반환
        return sessionId;
    }

    // [2단계] 장바구니에 할인 쿠폰 적용
    public Map<String, Object> applyCouponToSession(String sessionId, String couponCode, String targetPriceType) {
        // 세션 ID로 장바구니 정보 조회
        PendingPayment session = pendingPayments.get(sessionId);
        if (session == null) {
            throw new RuntimeException("유효하지 않은 결제 세션입니다.");
        }

        // 쿠폰 유효성 검증
        CouponValidationResult validation = validateCoupon(couponCode, session.getUserId());
        if (!validation.isValid) {
            throw new RuntimeException("유효하지 않은 쿠폰입니다.");
        }

        // 장바구니에 쿠폰 정보 업데이트
        session.applyCoupon(targetPriceType, couponCode, validation.getDiscountRate());

        // 업데이트된 가격 정보 반환
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("%s 쿠폰이 적용되었습니다.", targetPriceType));
        response.put("updatedPriceInfo", session.getCurrentPriceInfo());

        return response;
    }
    /* 예약 처리 (메인 프로세스) [3단계] */

    // [3단계] 최종 결제 : sessionId로 장바구니 정보 최종 확정 및 결제 완료    
    @Transactional
    public List<Reservation> createReservations(String sessionId, Integer usedPoints, String passengerName, LocalDate passengerBirth) {
        // 세션 ID로 장바구니 정보 조회
        PendingPayment session = pendingPayments.get(sessionId);
        if (session == null) {
            throw new RuntimeException("만료되었거나 이미 처리된 요청입니다.");
        }

        // 🚨 '재확인' (Time-of-Use) : TOCTOU 취약점의 핵심
        // 장바구니에 쿠폰을 담은 시점과, 지금 결제하는 시점 사이에 다른 사람이 쿠폰을 써버렸을 수 있으므로 DB를 통해 진짜 유효성을 다시 한번 검증
        validateSessionCoupon(session);

        // 최종 결제 금액으로 포인트 사용량 검증
        int totalFinalPrice = session.calculateTotalPrice();
        if (usedPoints != totalFinalPrice) {
            throw new RuntimeException(String.format("결제 포인트(%d)가 실제 결제 금액(%d)과 일치하지 않습니다.",  usedPoints, totalFinalPrice));
        }
        
        // 포인트 차감 처리
        if (usedPoints > 0) {
            int updatedRows = userRepository.deductPoints(session.getUserId(), usedPoints);
            if (updatedRows == 0) {
                throw new RuntimeException("포인트가 부족합니다.");
            }
        }
        
        // 좌석 예약 처리
        int reservedSeats = seatRepository.reserveSeats(session.getFlightId(), session.getSeatNumbers());
        if (reservedSeats != session.getSeatNumbers().size()) {
            throw new RuntimeException("좌석 예약에 실패했습니다.");
        }
        
        // 예약 레코드 생성
        User user = userRepository.findById(session.getUserId()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Flight flight = flightRepository.findById(session.getFlightId()).orElseThrow(() -> new RuntimeException("항공편을 찾을 수 없습니다."));
        List<Seat> seats = seatRepository.findSeatsByFlightIdAndNumbers(session.getFlightId(), session.getSeatNumbers());
        List<Reservation> reservations = seats.stream().map(seat -> {
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setFlight(flight);
            reservation.setSeat(seat);
            reservation.setPassengerName(passengerName);
            reservation.setPassengerBirth(passengerBirth);
            reservation.setBookedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());
            
            return reservationRepository.save(reservation);
        }).toList();

        // 쿠폰 사용 처리
        processSessionCouponUsage(session);

        return reservations;
    }

    /* 🔧 내부 메서드 */

    // 좌석 가격 정보 조회 메서드
    private Map<String, Integer> getPriceInfo(Long flightId, String seatNumber) {
        Map<String, BigDecimal> priceInfo = seatRepository.findAllPriceInfo(flightId, seatNumber).orElseThrow(() -> new RuntimeException("좌석 가격 정보를 찾을 수 없습니다: " + seatNumber));

        Map<String, Integer> result = new HashMap<>();
        result.put("seatPrice", priceInfo.get("seatPrice").intValue());
        result.put("fuelPrice", priceInfo.get("fuelPrice").intValue());

        return result;
    }

    // 쿠폰 검증 메서드
    private CouponValidationResult validateCoupon(String couponCode, Long userId) {
        // 입력값 기본 검증
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return new CouponValidationResult(false, 0.0, null);
        }

        // 쿠폰 조회
        String userCoupon = userRepository.findCouponByUserId(userId).orElse(null);

        // 입력된 쿠폰과 사용자 쿠폰 일치 검증
        if (userCoupon != null && userCoupon.equals(couponCode.trim())) {
            double discountRate = extractDiscountRate(couponCode.trim());
            return new CouponValidationResult(true, discountRate, couponCode.trim());
        }

        return new CouponValidationResult(false, 0.0, null);
    }

    // 쿠폰 검증 메서드
    private void validateSessionCoupon(PendingPayment session) {
        if (session.getAppliedSeatCoupon() != null) {
            if (!validateCoupon(session.getAppliedSeatCoupon(), session.getUserId()).isValid) {
                throw new RuntimeException("쿠폰이 만료되어 운임비 할인이 적용되지 않았습니다.");
            }
        }
        if (session.getAppliedFuelCoupon() != null) {
            if (!validateCoupon(session.getAppliedFuelCoupon(), session.getUserId()).isValid) {
                throw new RuntimeException("쿠폰이 만료되어 유류할증료 할인이 적용되지 않았습니다.");
            }
        }
    }

    // 쿠폰 할인율 추출 메서드
    private double extractDiscountRate(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return 0.0;
        }

        String numbers = couponCode.replaceAll("[^0-9]", "");
        if (numbers.isEmpty()) {
            return 0.0;
        }

        try {
            int discountPercent = Integer.parseInt(numbers);
            if (discountPercent > 99) {
                discountPercent = 99;
            }
            return discountPercent / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // 쿠폰 사용 처리 메서드
    private void processSessionCouponUsage(PendingPayment session) {
        // 운임비 쿠폰 사용 처리
        if (session.getAppliedSeatCoupon() != null) {
            userRepository.useCouponByCode(session.getUserId(), session.getAppliedSeatCoupon());
        }

        // 유류할증료 쿠폰 사용 처리
        if (session.getAppliedFuelCoupon() != null) {
            userRepository.useCouponByCode(session.getUserId(), session.getAppliedFuelCoupon());
        }
    }

    /* 📋 내부 클래스 (데이터 전달용) */

    // 장바구니 상태 관리 클래스
    @Data
    @AllArgsConstructor
    private static class PendingPayment {
        private final Long userId;
        private final Long flightId;
        private final List<String> seatNumbers;

        private int originalSeatPrice;
        private int originalFuelPrice;

        private String appliedSeatCoupon;
        private String appliedFuelCoupon;

        private double seatDiscountRate = 0.0;
        private double fuelDiscountRate = 0.0;

        public PendingPayment(Long userId, Long flightId, List<String> seatNumbers, int seatPrice, int fuelPrice) {
            this.userId = userId;
            this.flightId = flightId;
            this.seatNumbers = seatNumbers;
            this.originalSeatPrice = seatPrice;
            this.originalFuelPrice = fuelPrice;
        }
    
        public void applyCoupon(String type, String code, double rate) {
            if ("seat".equals(type)) {
                this.appliedSeatCoupon = code;
                this.seatDiscountRate = rate;
            } else if ("fuel".equals(type)) {
                this.appliedFuelCoupon = code;
                this.fuelDiscountRate = rate;
            }
        }

        public int calculateTotalPrice() {
            int finalSeatPrice = (int) (originalSeatPrice * (1 - seatDiscountRate));
            int finalFuelPrice = (int) (originalFuelPrice * (1 - fuelDiscountRate));
            return (finalSeatPrice + finalFuelPrice) * seatNumbers.size();
        }

        public Map<String, Object> getCurrentPriceInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("totalPrice", calculateTotalPrice());
            info.put("seatPrice", originalSeatPrice);
            info.put("fuelPrice", originalFuelPrice);
            info.put("seatDiscountRate", seatDiscountRate);
            info.put("fuelDiscountRate", fuelDiscountRate);
            info.put("appliedSeatCoupon", appliedSeatCoupon);
            info.put("appliedFuelCoupon", appliedFuelCoupon);
            return info;
        }
    }

    // 쿠폰 검증 결과 담는 클래스
    private static class CouponValidationResult {
        final boolean isValid;
        final double discountRate;
        final String couponCode;

        CouponValidationResult(boolean isValid, double discountRate, String couponCode) {
            this.isValid = isValid;
            this.discountRate = discountRate;
            this.couponCode = couponCode;
        }

        public double getDiscountRate() {
            return discountRate;
        }
    }

    /* 📋 예약 관리 메서드들 */
    
    // 사용자별 예약 목록 조회
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdWithFlightAndSeat(userId);
    }
    
    // 예약 취소
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new RuntimeException("예약 취소 권한이 없습니다.");
        }
        
        List<String> seatNumbers = List.of(reservation.getSeat().getSeatNumber());
        seatRepository.cancelReservation(reservation.getFlight().getId(), seatNumbers);
        
        reservationRepository.delete(reservation);
    }
    
    // 예약 상세 조회
    public Reservation getReservationDetail(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new RuntimeException("예약 조회 권한이 없습니다.");
        }
        
        return reservation;
    }
}