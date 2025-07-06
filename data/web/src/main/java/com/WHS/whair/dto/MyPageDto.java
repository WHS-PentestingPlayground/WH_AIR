package com.WHS.whair.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class MyPageDto {
    // 사용자 정보
    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private Integer point;
    private String coupon;
    private LocalDateTime createdAt;
    
    // 예약 정보
    private Long reservationId;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String seatNumber;
    private String seatClass;
    private BigDecimal totalPrice;
    private String passengerName;
    private String status;
    private LocalDateTime bookedAt;
}