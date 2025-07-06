package com.WHS.whair.service;

import com.WHS.whair.dto.MyPageDto;
import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.User;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public User getUserInfo(String userName) {
        return userRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public List<MyPageDto> getUserReservations(String userName) {
        User user = getUserInfo(userName);
        List<Reservation> reservations = reservationRepository.findByUserIdWithFlightAndSeat(user.getId());
        
        return reservations.stream()
                .map(this::convertToMyPageDto)
                .collect(Collectors.toList());
    }

    private MyPageDto convertToMyPageDto(Reservation reservation) {
        MyPageDto dto = new MyPageDto();
        
        // 사용자 정보
        User user = reservation.getUser();
        dto.setUserId(user.getId());
        dto.setUserName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPoint(user.getPoint());
        dto.setCoupon(user.getCoupon());
        dto.setCreatedAt(user.getCreatedAt());
        
        // 예약 정보
        dto.setReservationId(reservation.getId());
        dto.setFlightNumber(reservation.getFlight().getFlightNumber());
        dto.setDepartureAirport(reservation.getFlight().getDepartureAirport());
        dto.setArrivalAirport(reservation.getFlight().getArrivalAirport());
        dto.setDepartureTime(reservation.getFlight().getDepartureTime());
        dto.setArrivalTime(reservation.getFlight().getArrivalTime());
        dto.setSeatNumber(reservation.getSeat().getSeatNumber());
        dto.setSeatClass(reservation.getSeat().getSeatClass());
        dto.setPassengerName(reservation.getPassengerName());
        dto.setStatus(reservation.getStatus());
        dto.setBookedAt(reservation.getBookedAt());
        
        // 총 가격 계산 (좌석 가격 + 연료 가격)
        BigDecimal seatPrice = reservation.getSeat().getSeatPrice();
        BigDecimal fuelPrice = reservation.getSeat().getFuelPrice();
        dto.setTotalPrice(seatPrice.add(fuelPrice));
        
        return dto;
    }
}