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

import java.util.List;

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
    public String managerPage(Model model) {
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
} 