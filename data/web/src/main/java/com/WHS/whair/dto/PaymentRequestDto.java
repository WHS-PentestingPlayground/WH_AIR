package com.WHS.whair.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PaymentRequestDto {
    private Long flightId;
    private List<String> selectedSeats;
    private List<Map<String, String>> passengers;
    private String couponCode;
    private BigDecimal usedPoints;
    private BigDecimal totalAmount;
    private String paymentMethod;

    public PaymentRequestDto(Long flightId, List<String> selectedSeats, List<Map<String, String>> passengers, 
                           String couponCode, BigDecimal usedPoints, BigDecimal totalAmount, String paymentMethod) {
        this.flightId = flightId;
        this.selectedSeats = selectedSeats;
        this.passengers = passengers;
        this.couponCode = couponCode;
        this.usedPoints = usedPoints;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }
} 