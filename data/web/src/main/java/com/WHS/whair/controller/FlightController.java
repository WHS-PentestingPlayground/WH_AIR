package com.WHS.whair.controller;

import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.service.FlightService;
import com.WHS.whair.repository.UserRepository;
import com.WHS.whair.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.time.LocalDate;

@Controller
public class FlightController {

    @Autowired
    private FlightService flightService;
    
    @Autowired
    private UserRepository userRepository;

    // 항공권 검색 폼
    @GetMapping("/search")
    public String searchFlights(Model model) {
        return "flightSearch";
    }

    @PostMapping("/search")
    public String searchFlights(@RequestParam String departure_airport, @RequestParam String arrival_airport, @RequestParam String departure_date, @RequestParam String arrival_date, @RequestParam("class") String seatClass, Model model) {
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

    // 항공권 예매 폼
    @GetMapping("/booking")
    public String showBookingPage(@RequestParam("flightId") Long flightId, @RequestParam("seatClass") String seatClass, 
                                  HttpServletRequest request, Model model) {
        
        FlightSearchResultDTO flight = flightService.getFlightDetail(flightId, seatClass);
        if (flight == null) {
            model.addAttribute("error", "항공편을 찾을 수 없습니다.");
            return "redirect:/search";
        }
        
        // JWT 토큰에서 사용자 정보 가져오기
        User user = (User) request.getAttribute("user");
        if (user == null) {
            model.addAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        
        model.addAttribute("flight", flight);
        model.addAttribute("flightId", flightId);
        model.addAttribute("selectedSeatClass", seatClass);
        model.addAttribute("user", user);
        
        return "flightBooking";
    }

    /* REST API 엔드포인트들 */
    
    // 항공편 가격 정보 조회 API
    @GetMapping("/api/flights/{flightId}/pricing")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFlightPricing(@PathVariable Long flightId, @RequestParam String seatClass) {
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
  
    // 사용자 쿠폰 조회 API
    @GetMapping("/api/user/coupons")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserCoupons(HttpServletRequest request) {
        try {
            // JWT 토큰에서 사용자 정보 가져오기
            User user = (User) request.getAttribute("user");
            if (user == null || user.getId() == null) {
                return ResponseEntity.status(401).build();
            }
            
            // 실제 DB에서 사용자 정보 조회
            User dbUser = userRepository.findById(user.getId()).orElse(null);
            if (dbUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            Map<String, Object> response = new HashMap<>();
            
            // 실제 DB에서 조회한 사용자의 쿠폰 정보 및 포인트 조회
            response.put("userCoupon", dbUser.getCoupon());
            response.put("points", dbUser.getPoint());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 
