package com.WHS.whair.service;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    // 모든 예약 조회
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllWithUserAndFlightAndSeat();
    }

    // 사용 가능한 비즈니스 클래스 좌석 조회
    public List<Seat> getAvailableBusinessSeats(Long flightId) {
        return seatRepository.findAvailableBusinessSeats(flightId);
    }

    // 좌석 변경 (economy -> business만 가능)
    @Transactional
    public void changeSeat(Long reservationId, Long newSeatId) {
        Logger log = LoggerFactory.getLogger(ManagerService.class);
        log.info("changeSeat() called: reservationId={}, newSeatId={}", reservationId, newSeatId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        log.info("Reservation: id={}, passengerName={}, seatId={}", reservation.getId(), reservation.getPassengerName(), reservation.getSeat() != null ? reservation.getSeat().getId() : null);
        Seat newSeat = seatRepository.findById(newSeatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
        log.info("NewSeat: id={}, seatClass={}, isReserved={}", newSeat.getId(), newSeat.getSeatClass(), newSeat.isReserved());
        // economy 클래스 좌석만 business로 변경 가능
        Seat currentSeat = reservation.getSeat();
        log.info("CurrentSeat: id={}, seatClass={}, isReserved={}", currentSeat.getId(), currentSeat.getSeatClass(), currentSeat.isReserved());
        if (!"economy".equals(currentSeat.getSeatClass())) {
            log.warn("좌석 변경 실패: 현재 좌석 등급이 economy가 아님. currentSeatClass={}", currentSeat.getSeatClass());
            throw new RuntimeException("economy 클래스 좌석만 business로 변경할 수 있습니다.");
        }
        // 새 좌석이 business 클래스인지 확인
        if (!"business".equals(newSeat.getSeatClass())) {
            log.warn("좌석 변경 실패: 새 좌석 등급이 business가 아님. newSeatClass={}", newSeat.getSeatClass());
            throw new RuntimeException("business 클래스 좌석만 선택할 수 있습니다.");
        }
        // 새 좌석이 사용 가능한지 확인
        if (newSeat.isReserved()) {
            log.warn("좌석 변경 실패: 새 좌석이 이미 예약됨. newSeatId={}", newSeat.getId());
            throw new RuntimeException("이미 예약된 좌석입니다.");
        }
        // 기존 좌석 예약 해제
        currentSeat.setReserved(false);
        seatRepository.save(currentSeat);
        // 새 좌석 예약
        newSeat.setReserved(true);
        seatRepository.save(newSeat);
        // 예약 정보 업데이트
        reservation.setSeat(newSeat);
        reservationRepository.save(reservation);
        log.info("좌석 변경 성공: reservationId={}, oldSeatId={}, newSeatId={}", reservationId, currentSeat.getId(), newSeat.getId());
    }
} 