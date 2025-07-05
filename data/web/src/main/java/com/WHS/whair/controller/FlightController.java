package com.WHS.whair.controller;

import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    // 항공권 검색 폼
    @GetMapping("/search")
    public String searchFlights(Model model) {
        return "flightSearch";
    }

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

    // 항공권 예매 폼
    @GetMapping("/booking")
    public String showBookingPage(@RequestParam("flightId") Long flightId, 
                                 @RequestParam("seatClass") String seatClass, 
                                 Model model) {
        
        FlightSearchResultDTO flight = flightService.getFlightDetail(flightId, seatClass);
        if (flight == null) {
            model.addAttribute("error", "항공편을 찾을 수 없습니다.");
            return "redirect:/flights/search";
        }
        
        model.addAttribute("flight", flight);
        model.addAttribute("flightId", flightId);
        model.addAttribute("selectedSeatClass", seatClass);
        
        return "flightBooking";
    }

    // REST API 엔드포인트들
    
    /**
     * 항공편 가격 정보 조회 API
     */
    @GetMapping("/api/{flightId}/pricing")
    @ResponseBody
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

    /**
     * 좌석 현황 조회 API
     */
    @GetMapping("/api/{flightId}/seats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSeatMap(@PathVariable Long flightId,
                                                         @RequestParam String seatClass) {
        try {
            Map<String, Object> seatData = flightService.getSeatMap(flightId, seatClass);
            return ResponseEntity.ok(seatData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 예약 가능한 좌석 조회 API
     */
    @GetMapping("/api/{flightId}/available-seats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvailableSeats(@PathVariable Long flightId,
                                                                @RequestParam String seatClass) {
        try {
            List<String> availableSeats = flightService.getAvailableSeats(flightId, seatClass);
            
            Map<String, Object> response = new HashMap<>();
            response.put("availableSeats", availableSeats);
            response.put("count", availableSeats.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "좌석 정보를 가져오는데 실패했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 좌석 예약 처리 API
     */
    @PostMapping("/api/{flightId}/book")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bookSeats(@PathVariable Long flightId,
                                                        @RequestBody Map<String, Object> bookingData) {
        try {
            // 예약 데이터 검증
            List<String> selectedSeats = (List<String>) bookingData.get("selectedSeats");
            List<Map<String, String>> passengers = (List<Map<String, String>>) bookingData.get("passengers");
            
            if (selectedSeats == null || selectedSeats.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "좌석을 선택해주세요.");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 예약 처리
            Map<String, Object> result = flightService.bookSeats(flightId, selectedSeats, passengers);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "예약 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 