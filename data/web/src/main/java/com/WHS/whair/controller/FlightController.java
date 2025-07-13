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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
            // 검색 값 고정
            String fixedDepartureAirport = "ICN";
            String fixedArrivalAirport = "YVR";
            LocalDate fixedDate = LocalDate.parse("2025-08-02");
            List<String> fixedSeatClass = Arrays.asList("economy");

            List<FlightSearchResultDTO> searchResults = flightService.searchFlights(
                fixedDepartureAirport, 
                fixedArrivalAirport, 
                fixedDate, 
                fixedDate, 
                fixedSeatClass
            );
            
            // 결과를 모델에 추가
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("departure_airport", fixedDepartureAirport);
            model.addAttribute("arrival_airport", fixedArrivalAirport);
            model.addAttribute("departure_date", fixedDate.toString());
            model.addAttribute("arrival_date", fixedDate.toString());
            model.addAttribute("param_class", "economy");
            
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
    public String showBookingPage(@RequestParam("flightId") Long flightId, @RequestParam("seatClass") String seatClass, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

        if (!"economy".equalsIgnoreCase(seatClass)) {
            redirectAttributes.addFlashAttribute("error", "잘못된 접근입니다. 현재 이코노미 클래스만 예매 가능합니다.");
            return "redirect:/search";
        }
        
        FlightSearchResultDTO flight = flightService.getFlightDetail(flightId, seatClass);
        if (flight == null) {
            redirectAttributes.addFlashAttribute("error", "항공편을 찾을 수 없습니다.");
            return "redirect:/search";
        }
        
        checkUserAccess(request);

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
            if (user == null || user.getName() == null) {
                return ResponseEntity.status(401).build();
            }

            Map<String, Object> response = new HashMap<>();
            
            // DB에서 사용자 정보 조회
            User dbUser = userRepository.findByName(user.getName()).orElse(null);
            if (dbUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            // 실제 DB에서 조회한 사용자의 쿠폰 정보 및 포인트 조회
            response.put("userCoupon", dbUser.getCoupon());
            response.put("points", dbUser.getPoint());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 관리자 페이지 접근 권한 체크
    private void checkUserAccess(HttpServletRequest request) {

        // JWT 인증 필터가 request에 "user"라는 이름으로 저장해둔 User 객체를 가져온다.
        User user = (User) request.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
    }
} 
