package com.WHS.whair.controller;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.SeatRepository;
import com.WHS.whair.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private static final Logger log = LoggerFactory.getLogger(ManagerController.class);
    private final ManagerService managerService;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    // Manager 메인 페이지
    @GetMapping
    public String managerPage(Model model, HttpServletResponse response) {
        // X-Powered-By 헤더 설정 (CVE 테스트용)
        response.setHeader("X-Powered-By", "Spring Boot 2.6.5");
        
        // 인증 체크 임시 비활성화 (테스트용)
        /*
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("managerPage() - auth: {}", auth);
        if (auth == null) {
            log.warn("managerPage() - auth is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        log.info("managerPage() - auth.getName(): {}", auth.getName());
        if (!"manager".equals(auth.getName())) {
            log.warn("managerPage() - forbidden for user: {}", auth.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        */
        
        try {
            List<Reservation> reservations = managerService.getAllReservations();
            log.info("managerPage() - reservations.size(): {}", reservations.size());
            for (Reservation r : reservations) {
                log.info("Reservation id={}, passengerName={}, flightId={}, seatId={}",
                    r.getId(),
                    r.getPassengerName(),
                    r.getFlight() != null ? r.getFlight().getId() : null,
                    r.getSeat() != null ? r.getSeat().getId() : null
                );
            }
            model.addAttribute("reservations", reservations);
        } catch (Exception e) {
            log.warn("managerPage() - 예약 목록 조회 실패: {}", e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
        }
        return "manager";
    }

    // 좌석 변경 페이지
    @GetMapping("/change-seat/{reservationId}")
    public String changeSeatPage(@PathVariable Long reservationId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("changeSeatPage() - auth: {}", auth);
        if (auth == null) {
            log.warn("changeSeatPage() - auth is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        log.info("changeSeatPage() - auth.getName(): {}", auth.getName());
        if (!"manager".equals(auth.getName())) {
            log.warn("changeSeatPage() - forbidden for user: {}", auth.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        
        // economy 클래스 좌석만 business로 변경 가능
        List<Seat> availableBusinessSeats = managerService.getAvailableBusinessSeats(reservation.getFlight().getId());
        
        model.addAttribute("reservation", reservation);
        model.addAttribute("availableSeats", availableBusinessSeats);
        return "changeSeat";
    }

    // 좌석 변경 처리
    @PostMapping("/change-seat/{reservationId}")
    @ResponseBody
    public String changeSeat(@PathVariable Long reservationId, @RequestParam Long newSeatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("changeSeat() - auth: {}", auth);
        if (auth == null) {
            log.warn("changeSeat() - auth is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        log.info("changeSeat() - auth.getName(): {}", auth.getName());
        if (!"manager".equals(auth.getName())) {
            log.warn("changeSeat() - forbidden for user: {}", auth.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        try {
            managerService.changeSeat(reservationId, newSeatId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // POST 요청 처리 (테스트용)
    @PostMapping
    public String managerPagePost(Model model, HttpServletResponse response, Dummy dummy) {
        // X-Powered-By 헤더 설정 (CVE 테스트용)
        response.setHeader("X-Powered-By", "Spring Boot 2.6.5");
        
        log.info("managerPagePost() - dummy.test: {}", dummy != null ? dummy.getTest() : "null");
        
        // 인증 체크 임시 비활성화 (테스트용)
        /*
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("managerPagePost() - auth: {}", auth);
        if (auth == null) {
            log.warn("managerPagePost() - auth is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        log.info("managerPagePost() - auth.getName(): {}", auth.getName());
        if (!"manager".equals(auth.getName())) {
            log.warn("managerPagePost() - forbidden for user: {}", auth.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        */
        
        try {
            List<Reservation> reservations = managerService.getAllReservations();
            model.addAttribute("reservations", reservations);
        } catch (Exception e) {
            log.warn("managerPagePost() - 예약 목록 조회 실패: {}", e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
        }
        return "manager";
    }
    
    // 임시 객체 (POST 요청 바인딩용)
    public static class Dummy {
        private String test;
        public void setTest(String test) { this.test = test; }
        public String getTest() { return test; }
    }
} 