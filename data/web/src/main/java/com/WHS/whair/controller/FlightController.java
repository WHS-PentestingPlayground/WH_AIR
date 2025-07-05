package com.WHS.whair.controller;

import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/flights")
@Controller
public class FlightController {
    @Autowired
    private FlightService flightService;

    // 항공권 검색 폼
    @GetMapping("/search")
    public String search(
        @RequestParam(required = false, defaultValue = "ICN") String departure_airport,
        @RequestParam(required = false, defaultValue = "YVR") String arrival_airport,
        @RequestParam(required = false, defaultValue = "2025-08-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure_date,
        @RequestParam(required = false, defaultValue = "2025-08-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival_date,
        @RequestParam(name = "class", required = false, defaultValue = "economy") String seatClass,
        Model model
    ) {
        // 입력값 검증 및 보안 처리
        departure_airport = validateAndSanitizeInput(departure_airport);
        arrival_airport = validateAndSanitizeInput(arrival_airport);
        seatClass = validateSeatClass(seatClass);
        
        // 선택한 좌석 등급만 검색
        List<String> seatClasses = List.of(seatClass);
        List<FlightSearchResultDTO> flights = flightService.searchFlightsWithPrice(departure_airport, arrival_airport, departure_date, arrival_date, seatClasses);
        model.addAttribute("flights", flights);
        model.addAttribute("searchParams", new String[]{departure_airport, arrival_airport, departure_date.toString(), arrival_date.toString(), seatClass});
        return "flightSearch";
    }
    
    // 입력값 검증 및 보안 처리
    private String validateAndSanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // 알파벳과 숫자만 허용 (공항 코드 형식)
        return input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
    
    // 좌석 등급 검증
    private String validateSeatClass(String seatClass) {
        if (seatClass == null) {
            return "economy";
        }
        String normalized = seatClass.toLowerCase().trim();
        if ("economy".equals(normalized) || "business".equals(normalized) || "first".equals(normalized)) {
            return normalized;
        }
        return "economy"; // 기본값
    }
} 