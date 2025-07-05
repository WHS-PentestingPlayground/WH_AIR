//package com.WHS.whair.controller;
//
//import com.WHS.whair.dto.FlightSearchResultDTO;
//import com.WHS.whair.service.FlightService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RequestMapping("/flights")
//@Controller
//public class FlightController {
//    @Autowired
//    private FlightService flightService;
//
//    // 항공권 검색 폼
//    @GetMapping("/search")
//    public String search(
//        @RequestParam(required = false, defaultValue = "ICN") String departure_airport,
//        @RequestParam(required = false, defaultValue = "YVR") String arrival_airport,
//        @RequestParam(required = false, defaultValue = "2025-08-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure_date,
//        @RequestParam(required = false, defaultValue = "2025-08-02") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival_date,
//        @RequestParam(name = "class", required = false, defaultValue = "economy") String seatClass,
//        Model model
//    ) {
//        List<FlightSearchResultDTO> flights = flightService.searchFlightsWithPrice(departure_airport, arrival_airport, departure_date, arrival_date, seatClass);
//        model.addAttribute("flights", flights);
//        model.addAttribute("searchParams", new String[]{departure_airport, arrival_airport, departure_date.toString(), arrival_date.toString(), seatClass});
//        return "flightSearch";
//    }
//}