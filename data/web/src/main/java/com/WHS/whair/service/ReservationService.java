package com.WHS.whair.service;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.User;
import com.WHS.whair.entity.Flight;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.UserRepository;
import com.WHS.whair.repository.FlightRepository;
import com.WHS.whair.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

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
    
    /* 예약 처리 (메인 프로세스) */
    @Transactional
    public List<Reservation> createReservations(
            Long userId, Long flightId, List<String> seatNumbers,
            String passengerName, LocalDate passengerBirth,
            Integer usedPoints,
            String seatCoupon, String fuelCoupon) {
        
        // 엔티티 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("항공편을 찾을 수 없습니다."));
        
        // 좌석 유효성 재검증
        if (!seatService.validateSeatSelection(flightId, seatNumbers)) {
            throw new RuntimeException("선택한 좌석이 이미 예약되었습니다.");
        }
        
        // 좌석 가격 정보 조회
        Map<String, Integer> priceInfo = getPriceInfo(flightId, seatNumbers.get(0));
        int seatOriginalPrice = priceInfo.get("seatPrice");
        int fuelOriginalPrice = priceInfo.get("fuelPrice");

        // 쿠폰 할인 적용
        CouponApplicationResult couponResult = applyCouponDiscounts(userId, seatCoupon, fuelCoupon, seatOriginalPrice, fuelOriginalPrice);

        // 최종 가격 계산
        int passengerCount = seatNumbers.size();
        int totalFinalPrice = couponResult.getTotalPrice() * passengerCount;

        // 포인트 사용량과 결제 금액 정확성 검증
        if (usedPoints != totalFinalPrice) {
            throw new RuntimeException(String.format(
                "결제 포인트(%d)가 실제 결제 금액(%d)과 일치하지 않습니다.", 
                usedPoints, totalFinalPrice
            ));
        }
        
        // 포인트 차감 처리 (실제 결제 금액만큼)
        if (usedPoints > 0) {
            int updatedRows = userRepository.deductPoints(userId, usedPoints);
            if (updatedRows == 0) {
                throw new RuntimeException("포인트가 부족합니다.");
            }
        }
        
        // 좌석 예약 처리
        int reservedSeats = seatRepository.reserveSeats(flightId, seatNumbers);
        if (reservedSeats != seatNumbers.size()) {
            throw new RuntimeException("좌석 예약에 실패했습니다.");
        }
        
        // 예약 레코드 생성
        List<Seat> seats = seatRepository.findSeatsByFlightIdAndNumbers(flightId, seatNumbers);
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
        processCouponUsage(couponResult, userId);

        return reservations;
    }

    

    /* 쿠폰 적용 API용 메서드 (UI 피드백용) */
    @Transactional
    public Map<String, Object> applyCoupon(Long userId, String couponCode, String targetPriceType, Long flightId, String seatNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 쿠폰 검증
            CouponValidationResult validation = validateCoupon(couponCode, userId);

            if(!validation.isValid) {
                response.put("success", false);
                response.put("message", "유효하지 않은 쿠폰입니다.");
                return response;
            }

            // 좌석 가격 조회
            Map<String, Integer> priceInfo = getPriceInfo(flightId, seatNumber);

            // 비용 종류에 따른 가격 선택
            int basePrice;
            String priceTypeName;

            if("seat".equals(targetPriceType)) {
                basePrice = priceInfo.get("seatPrice");
                priceTypeName = "운임비";
            } else if ("fuel".equals(targetPriceType)) {
                basePrice = priceInfo.get("fuelPrice");
                priceTypeName = "유류할증료";
            } else {
                response.put("success", false);
                response.put("message", "잘못된 정보입니다.");
                return response;
            }

            // 할인 금액 계산
            int discountAmount = (int) Math.floor(basePrice * validation.discountRate);
            int finalPrice = basePrice - discountAmount;

            // 할인율을 퍼센트로 변환 (UI 표시용)
            int discountPercent = (int) Math.floor(validation.discountRate * 100);

            // 성공 응답 반환
            response.put("success", true);
            response.put("couponCode", couponCode);
            response.put("priceTypeName", priceTypeName);
            response.put("originalPrice", basePrice);
            response.put("discountAmount", discountAmount);
            response.put("discountPercent", discountPercent);
            response.put("finalPrice", finalPrice);
            response.put("message", String.format("쿠폰 적용 성공: %s에 %s 쿠폰이 적용되었습니다. (%d%% 할인)", priceTypeName, couponCode, discountPercent));

            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "쿠폰 적용에 실패했습니다: " + e.getMessage());
            return response;
        }
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

    // 쿠폰 할인 적용 메서드 (최종 가격 계산)
    private CouponApplicationResult applyCouponDiscounts(Long userId, String seatCouponCode, String fuelCouponCode, int seatOriginalPrice, int fuelOriginalPrice) {

        // 쿠폰 검증 및 할인율 계산
        CouponValidationResult seatCouponResult = validateCoupon(seatCouponCode, userId);
        CouponValidationResult fuelCouponResult = validateCoupon(fuelCouponCode, userId);
        
        // 할인 금액 계산
        int seatDiscount = (int)Math.floor(seatOriginalPrice * seatCouponResult.discountRate);
        int fuelDiscount = (int)Math.floor(fuelOriginalPrice * fuelCouponResult.discountRate);

        // 최종 가격 계산
        int finalSeatPrice = seatOriginalPrice - seatDiscount;
        int finalFuelPrice = fuelOriginalPrice - fuelDiscount;
        int totalPrice = finalSeatPrice + finalFuelPrice;

        // 결과 반환
        return new CouponApplicationResult(seatCouponResult, fuelCouponResult, seatOriginalPrice, fuelOriginalPrice, seatDiscount, fuelDiscount, finalSeatPrice, finalFuelPrice, totalPrice);
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
    private void processCouponUsage(CouponApplicationResult result, Long userId) {
        // 운임비 쿠폰 사용 처리
        if (result.getSeatCouponResult().isValid && result.getSeatCouponResult().couponCode != null) {
            userRepository.useCoupon(userId);
        }

        // 유류할증료 쿠폰 사용 처리
        if (result.getFuelCouponResult().isValid && result.getFuelCouponResult().couponCode != null) {
            userRepository.useCoupon(userId);
        }
    }

    /* 📋 쿠폰 관련 내부 클래스 (데이터 전달용) */

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
    }

    // 쿠폰 적용 결과 담는 클래스
    private static class CouponApplicationResult {
        private final CouponValidationResult seatCouponResult;
        private final CouponValidationResult fuelCouponResult;
        private final int seatOriginalPrice;
        private final int fuelOriginalPrice;
        private final int seatDiscount;
        private final int fuelDiscount;
        private final int finalSeatPrice;
        private final int finalFuelPrice;
        private final int totalPrice;

        CouponApplicationResult(CouponValidationResult seatCouponResult, CouponValidationResult fuelCouponResult, int seatOriginalPrice, int fuelOriginalPrice, int seatDiscount, int fuelDiscount, int finalSeatPrice, int finalFuelPrice, int totalPrice) {
            this.seatCouponResult = seatCouponResult;
            this.fuelCouponResult = fuelCouponResult;
            this.seatOriginalPrice = seatOriginalPrice;
            this.fuelOriginalPrice = fuelOriginalPrice;
            this.seatDiscount = seatDiscount;
            this.fuelDiscount = fuelDiscount;
            this.finalSeatPrice = finalSeatPrice;
            this.finalFuelPrice = finalFuelPrice;
            this.totalPrice = totalPrice;
        }

        public CouponValidationResult getSeatCouponResult() {
            return seatCouponResult;
        }

        public CouponValidationResult getFuelCouponResult() {
            return fuelCouponResult;
        }
        
        public int getTotalPrice() {
            return totalPrice;
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