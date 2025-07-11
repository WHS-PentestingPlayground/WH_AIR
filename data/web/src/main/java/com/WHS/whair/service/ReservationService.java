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
    
    /**
     * 예약 생성 (포인트 차감 방식)
     * @param userId 사용자 ID
     * @param flightId 항공편 ID
     * @param seatNumbers 선택된 좌석 번호들
     * @param passengerName 탑승자 이름
     * @param passengerBirth 탑승자 생년월일
     * @param usedPoints 사용할 포인트
     * @return 생성된 예약 목록
     */
    @Transactional
    public List<Reservation> createReservations(
            Long userId, Long flightId, List<String> seatNumbers,
            String passengerName, LocalDate passengerBirth,
            Integer usedPoints,
            String seatCoupon, String fuelCoupon, Integer seatOriginalPrice, Integer fuelOriginalPrice) {
        
        // 1. 엔티티 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("항공편을 찾을 수 없습니다."));
        
        // 2. 좌석 유효성 재검증
        if (!seatService.validateSeatSelection(flightId, seatNumbers)) {
            throw new RuntimeException("선택한 좌석이 이미 예약되었습니다.");
        }
        
        // 3. 쿠폰 할인 계산 및 사용 처리
        double seatDiscountRate = 0.0;
        double fuelDiscountRate = 0.0;
        if (seatCoupon != null && !seatCoupon.trim().isEmpty()) {
            String discountStr = seatCoupon.replaceAll("[^0-9]", "");
            if (!discountStr.isEmpty()) {
                seatDiscountRate = Integer.parseInt(discountStr) / 100.0;
            }
            // 쿠폰 사용 처리 (DB에서 삭제)
            userRepository.useCoupon(userId);
        }
        if (fuelCoupon != null && !fuelCoupon.trim().isEmpty()) {
            String discountStr = fuelCoupon.replaceAll("[^0-9]", "");
            if (!discountStr.isEmpty()) {
                fuelDiscountRate = Integer.parseInt(discountStr) / 100.0;
            }
            // 쿠폰 사용 처리 (DB에서 삭제)
            userRepository.useCoupon(userId);
        }
        int passengerCount = seatNumbers.size();
        int seatDiscount = (int)Math.floor(seatOriginalPrice * seatDiscountRate);
        int fuelDiscount = (int)Math.floor(fuelOriginalPrice * fuelDiscountRate);
        int finalSeatPrice = seatOriginalPrice - seatDiscount;
        int finalFuelPrice = fuelOriginalPrice - fuelDiscount;
        int totalFinalPrice = (finalSeatPrice + finalFuelPrice) * passengerCount;
        // 4. 포인트 차감 처리 (실제 결제 금액만큼)
        if (usedPoints > totalFinalPrice) {
            throw new RuntimeException("결제 포인트가 실제 결제 금액을 초과합니다.");
        }
        if (usedPoints > 0) {
            int updatedRows = userRepository.deductPoints(userId, usedPoints);
            if (updatedRows == 0) {
                throw new RuntimeException("포인트가 부족합니다.");
            }
        }
        
        // 5. 좌석 예약 처리
        int reservedSeats = seatRepository.reserveSeats(flightId, seatNumbers);
        if (reservedSeats != seatNumbers.size()) {
            throw new RuntimeException("좌석 예약에 실패했습니다. 이미 예약된 좌석이 있습니다.");
        }
        
        // 6. 예약 레코드 생성
        List<Seat> seats = seatRepository.findSeatsByFlightIdAndNumbers(flightId, seatNumbers);
        return seats.stream().map(seat -> {
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
    }
    
    // 사용자별 예약 목록 조회
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdWithFlightAndSeat(userId);
    }
    
    // 예약 취소
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new RuntimeException("예약 취소 권한이 없습니다.");
        }
        
        // 좌석 예약 취소
        List<String> seatNumbers = List.of(reservation.getSeat().getSeatNumber());
        seatRepository.cancelReservation(reservation.getFlight().getId(), seatNumbers);
        
        // 예약 삭제
        reservationRepository.delete(reservation);
    }
}