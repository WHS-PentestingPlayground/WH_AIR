package com.WHS.whair.controller;

import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.FlightService;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// =================================================================
// FlightController - 항공편 검색 및 예약 페이지 전용
// =================================================================
// 
// [주요 기능]
// - 항공편 검색 폼 제공
// - 항공편 검색 결과 표시
// - 항공편 예약 페이지 이동
// - 항공편 가격 정보 조회 API
// 
// [분리된 기능들]
// - 좌석 관련 API: SeatController
// - 예약 처리 API: ReservationController
@Controller
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // =================================================================
    // 항공편 검색 관련 페이지
    // =================================================================

    // 항공권 검색 폼
    @GetMapping("/search")
    public String searchFlights(Model model) {
        return "flightSearch";
    }

    // 항공편 검색 처리
    @PostMapping("/search")
    public String searchFlights(@RequestParam String departure_airport, @RequestParam String arrival_airport,
                               @RequestParam String departure_date, @RequestParam String arrival_date,
                               @RequestParam("class") String seatClass, Model model) {
        try {
            // 입력 데이터 검증
            if (departure_airport == null || arrival_airport == null || departure_date == null || seatClass == null ||
                departure_airport.trim().isEmpty() || arrival_airport.trim().isEmpty() || departure_date.trim().isEmpty() || seatClass.trim().isEmpty()) {
                model.addAttribute("error", "모든 검색 조건을 입력해주세요.");
                return "flightSearch";
            }

            // 날짜 파싱
            LocalDate depDate = LocalDate.parse(departure_date);
            LocalDate arrDate = arrival_date != null && !arrival_date.trim().isEmpty() ? 
                               LocalDate.parse(arrival_date) : depDate; // 도착날짜 미입력시 출발날짜와 동일
            
            // 좌석 클래스 리스트 생성
            List<String> seatClasses = Arrays.asList(seatClass);
            
            // 항공편 검색 실행
            List<FlightSearchResultDTO> searchResults = flightService.searchFlights(
                departure_airport.trim().toUpperCase(), 
                arrival_airport.trim().toUpperCase(), 
                depDate, 
                arrDate, 
                seatClasses
            );
            
            // 결과를 모델에 추가
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("departure_airport", departure_airport.toUpperCase());
            model.addAttribute("arrival_airport", arrival_airport.toUpperCase());
            model.addAttribute("departure_date", departure_date);
            model.addAttribute("arrival_date", arrival_date);
            model.addAttribute("param_class", seatClass);
            
            if (searchResults.isEmpty()) {
                model.addAttribute("noResults", true);
                model.addAttribute("message", "검색 조건에 맞는 항공편이 없습니다.");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "flightSearch";
    }

    // 항공권 예매 페이지 이동
    @GetMapping("/booking")
    public String showBookingPage(@RequestParam("flightId") Long flightId, 
                                 @RequestParam("seatClass") String seatClass, 
                                 HttpServletRequest request,
                                 Model model) {
        
        FlightSearchResultDTO flight = flightService.getFlightDetail(flightId, seatClass);
        if (flight == null) {
            model.addAttribute("error", "항공편을 찾을 수 없습니다.");
            return "redirect:/flights/search";
        }
        
        // JWT 토큰에서 사용자 정보 추출
        String token = extractTokenFromCookie(request);
        if (token != null) {
            String name = jwtUtil.validateAndExtractUsername(token);
            if (name != null) {
                try {
                    User currentUser = userService.findByName(name);
                    model.addAttribute("user", currentUser);
                } catch (Exception ignored) {
                    // 사용자 정보를 찾을 수 없는 경우 무시
                }
            }
        }
        
        model.addAttribute("flight", flight);
        model.addAttribute("flightId", flightId);
        model.addAttribute("selectedSeatClass", seatClass);
        
        return "flightBooking";
    }

    // JWT 토큰 추출 헬퍼 메서드
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

// =================================================================
// FlightApiController - 항공편 관련 REST API 전용
// =================================================================
@RestController
@RequestMapping("/api/flights")
class FlightApiController {

    @Autowired
    private FlightService flightService;

    // 항공편 가격 정보 조회 API
    @GetMapping("/{flightId}/pricing")
    public ResponseEntity<Map<String, Object>> getFlightPricing(@PathVariable Long flightId, 
                                                               @RequestParam String seatClass) {
        try {
            FlightSearchResultDTO flight = flightService.getFlightDetail(flightId, seatClass);
            if (flight == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> pricing = new HashMap<>();
            pricing.put("seatPrice", flight.getSeatPrice());
            pricing.put("fuelPrice", flight.getFuelPrice());
            pricing.put("totalPrice", flight.getSeatPrice().add(flight.getFuelPrice()));
            pricing.put("flightNumber", flight.getFlightNumber());
            pricing.put("route", flight.getDepartureAirport() + " → " + flight.getArrivalAirport());
            
            return ResponseEntity.ok(pricing);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
