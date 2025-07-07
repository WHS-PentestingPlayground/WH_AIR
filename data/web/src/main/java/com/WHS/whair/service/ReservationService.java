package com.WHS.whair.service;

import com.WHS.whair.dto.PaymentRequestDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.entity.Flight;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.entity.Reservation;
import com.WHS.whair.repository.UserRepository;
import com.WHS.whair.repository.SeatRepository;
import com.WHS.whair.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

// =================================================================
// ReservationService - 탑승자 정보 입력 및 결제 처리
// =================================================================
//
// [주요 기능]
// - STEP 2: 탑승자 정보 검증
// - STEP 3: 결제 처리 (포인트/쿠폰)
// - 예약 완료 처리 (Reservation 엔티티 저장)
@Service
public class ReservationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private SeatService seatService;

    // [STEP 2] 탑승자 정보 관련
    
    // 탑승자 정보 유효성 검증
    public Map<String, Object> validatePassengerInfo(List<Map<String, String>> passengers) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 탑승자 정보 필수 필드 검증
            for (int i = 0; i < passengers.size(); i++) {
                Map<String, String> passenger = passengers.get(i);
                
                String name = passenger.get("name");
                String birth = passenger.get("birth");
                
                if (name == null || name.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", (i + 1) + "번째 탑승자의 이름을 입력해주세요.");
                    return result;
                }
                
                if (birth == null || birth.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("message", (i + 1) + "번째 탑승자의 생년월일을 입력해주세요.");
                    return result;
                }
                
                // 생년월일 형식 검증 (YYYY-MM-DD)
                if (!birth.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    result.put("success", false);
                    result.put("message", (i + 1) + "번째 탑승자의 생년월일 형식이 올바르지 않습니다. (YYYY-MM-DD)");
                    return result;
                }
            }
            
            result.put("success", true);
            result.put("message", "탑승자 정보가 유효합니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "탑승자 정보 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    // [STEP 3] 결제 처리 관련
    
    // 사용자 포인트 조회
    public Integer getUserPoints(Long userId) {
        try {
            return reservationRepository.findUserPoints(userId);
        } catch (Exception e) {
            return 0;
        }
    }

    // 사용자 쿠폰 조회
    public String getUserCoupon(Long userId) {
        try {
            return reservationRepository.findUserCoupon(userId);
        } catch (Exception e) {
            return null;
        }
    }

    // 쿠폰 할인율 계산
    public BigDecimal calculateCouponDiscount(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 쿠폰 코드별 할인율 매핑
        Map<String, BigDecimal> couponDiscounts = new HashMap<>();
        couponDiscounts.put("WELCOME10", new BigDecimal("0.10")); // 10% 할인
        couponDiscounts.put("SAVE20", new BigDecimal("0.20"));    // 20% 할인
        couponDiscounts.put("VIP30", new BigDecimal("0.30"));     // 30% 할인
        couponDiscounts.put("DAEBAK99", new BigDecimal("0.99"));  // 99% 할인 (테스트용)

        return couponDiscounts.getOrDefault(couponCode.toUpperCase(), BigDecimal.ZERO);
    }

    // 포인트 사용 가능 여부 확인
    public boolean canUsePoints(Long userId, BigDecimal requestedPoints) {
        Integer currentPoints = getUserPoints(userId);
        if (currentPoints == null) {
            return false;
        }
        return currentPoints >= requestedPoints.intValue();
    }

    // 최종 결제 금액 계산
    public Map<String, Object> calculateFinalPrice(Long flightId, List<String> selectedSeats, 
                                                 String couponCode, BigDecimal usedPoints) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 기본 티켓 가격 계산
            BigDecimal basePrice = seatRepository.calculateTotalPrice(flightId, selectedSeats);
            
            // 2. 쿠폰 할인 적용
            BigDecimal couponDiscount = calculateCouponDiscount(couponCode);
            BigDecimal discountAmount = basePrice.multiply(couponDiscount);
            BigDecimal priceAfterCoupon = basePrice.subtract(discountAmount);
            
            // 3. 포인트 할인 적용
            BigDecimal finalPrice = priceAfterCoupon.subtract(usedPoints);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }

            result.put("basePrice", basePrice);
            result.put("couponDiscount", discountAmount);
            result.put("priceAfterCoupon", priceAfterCoupon);
            result.put("usedPoints", usedPoints);
            result.put("finalPrice", finalPrice);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "가격 계산 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    // 결제 및 예약 완료 처리 (STEP 3 최종 단계)
    @Transactional
    public Map<String, Object> processReservation(PaymentRequestDto paymentRequest, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 탑승자 정보 검증
            Map<String, Object> passengerValidation = validatePassengerInfo(paymentRequest.getPassengers());
            if (!(Boolean) passengerValidation.get("success")) {
                return passengerValidation;
            }
            
            // 2. 최종 좌석 유효성 검증 (SeatService 활용)
            if (!seatService.validateSeatSelection(paymentRequest.getFlightId(), paymentRequest.getSelectedSeats())) {
                result.put("success", false);
                result.put("message", "선택한 좌석 중 예약 불가능한 좌석이 있습니다.");
                return result;
            }
            
            // 3. 좌석 수와 탑승자 수 일치 검증
            if (paymentRequest.getSelectedSeats().size() != paymentRequest.getPassengers().size()) {
                result.put("success", false);
                result.put("message", "좌석 수와 탑승자 수가 일치하지 않습니다.");
                return result;
            }
            
            // 4. 포인트 사용 가능 여부 확인
            if (paymentRequest.getUsedPoints() != null && paymentRequest.getUsedPoints().compareTo(BigDecimal.ZERO) > 0) {
                if (!canUsePoints(userId, paymentRequest.getUsedPoints())) {
                    result.put("success", false);
                    result.put("message", "사용 가능한 포인트가 부족합니다.");
                    return result;
                }
            }
            
            // 5. 실제 좌석 예약 처리
            int reservedCount = seatRepository.reserveSeats(paymentRequest.getFlightId(), paymentRequest.getSelectedSeats());
            
            if (reservedCount != paymentRequest.getSelectedSeats().size()) {
                result.put("success", false);
                result.put("message", "일부 좌석이 이미 예약되었습니다. 다시 시도해주세요.");
                return result;
            }
            
            // 6. 포인트 차감 처리
            if (paymentRequest.getUsedPoints() != null && paymentRequest.getUsedPoints().compareTo(BigDecimal.ZERO) > 0) {
                int pointsDeducted = reservationRepository.deductUserPoints(userId, paymentRequest.getUsedPoints().intValue());
                if (pointsDeducted == 0) {
                    result.put("success", false);
                    result.put("message", "포인트 차감에 실패했습니다.");
                    return result;
                }
            }
            
            // 7. 쿠폰 사용 처리
            if (paymentRequest.getCouponCode() != null && !paymentRequest.getCouponCode().trim().isEmpty()) {
                int couponUsed = reservationRepository.useUserCoupon(userId, paymentRequest.getCouponCode());
                if (couponUsed == 0) {
                    result.put("success", false);
                    result.put("message", "쿠폰 사용에 실패했습니다.");
                    return result;
                }
            }
            
            // 8. reservations 테이블에 예약 정보 저장
            List<Long> reservationIds = saveReservations(userId, paymentRequest);
            
            // 9. 예약 완료 응답
            result.put("success", true);
            result.put("message", "예약이 완료되었습니다.");
            result.put("reservationIds", reservationIds);
            result.put("flightId", paymentRequest.getFlightId());
            result.put("selectedSeats", paymentRequest.getSelectedSeats());
            result.put("passengers", paymentRequest.getPassengers());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    // 예약 정보 저장 헬퍼 메서드
    private List<Long> saveReservations(Long userId, PaymentRequestDto paymentRequest) {
        List<Long> reservationIds = new ArrayList<>();
        
        try {
            // 필요한 엔티티들 조회
            // User 엔티티는 이미 userId로 검증되었으므로 간단히 생성
            User user = new User();
            user.setId(userId);
            
            List<Seat> seats = seatRepository.findSeatsBySeatNumbers(
                paymentRequest.getFlightId(), 
                paymentRequest.getSelectedSeats()
            );
            
            if (seats.size() != paymentRequest.getSelectedSeats().size()) {
                throw new RuntimeException("일부 좌석 정보를 찾을 수 없습니다.");
            }
            
            // 각 좌석별로 예약 정보 생성
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                Map<String, String> passenger = paymentRequest.getPassengers().get(i);
                
                Reservation reservation = new Reservation();
                reservation.setUser(user);
                reservation.setFlight(seat.getFlight());
                reservation.setSeat(seat);
                reservation.setPassengerName(passenger.get("name"));
                reservation.setPassengerBirth(LocalDate.parse(passenger.get("birth")));
                reservation.setStatus("BOOKED");
                reservation.setBookedAt(LocalDateTime.now());
                reservation.setUpdatedAt(LocalDateTime.now());
                
                Reservation savedReservation = reservationRepository.save(reservation);
                reservationIds.add(savedReservation.getId());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("예약 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return reservationIds;
    }
} 