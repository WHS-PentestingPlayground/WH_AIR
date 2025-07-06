package com.WHS.whair.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class FlightSearchResultDTO {
    private Long flightId;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String airline;
    private String aircraftModel;
    private String seatClass;
    private BigDecimal seatPrice;
    private BigDecimal fuelPrice;

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // JPA 쿼리에서 사용할 생성자
    public FlightSearchResultDTO(Long flightId, String flightNumber, String departureAirport, String arrivalAirport,
                                LocalDateTime departureTime, LocalDateTime arrivalTime, String airline, String aircraftModel,
                                String seatClass, BigDecimal seatPrice, BigDecimal fuelPrice) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.airline = airline;
        this.aircraftModel = aircraftModel;
        this.seatClass = seatClass;
        this.seatPrice = seatPrice;
        this.fuelPrice = fuelPrice;
    }

    // 포맷팅된 날짜 문자열 반환 메서드
    public String getFormattedDepartureTime() {
        return departureTime != null ? departureTime.format(DATE_FORMATTER) : "";
    }

    public String getFormattedArrivalTime() {
        return arrivalTime != null ? arrivalTime.format(DATE_FORMATTER) : "";
    }
} 