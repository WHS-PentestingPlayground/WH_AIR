package com.WHS.whair.controller;

import com.WHS.whair.entity.Reservation;
import com.WHS.whair.entity.Seat;
import com.WHS.whair.entity.User;
import com.WHS.whair.repository.ReservationRepository;
import com.WHS.whair.repository.SeatRepository;
import com.WHS.whair.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
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

    // 관리자 페이지 접근 권한 체크
    private void checkManagerAccess(HttpServletRequest request) {

        // JWT 인증 필터가 request에 "user"라는 이름으로 저장해둔 User 객체를 가져온다.
        User user = (User) request.getAttribute("user");

        log.info("checkManagerAccess() - user: {}", user);
        if (user == null) {
            log.warn("checkManagerAccess() - user is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        log.info("checkManagerAccess() - user.getName(): {}", user.getName());
        if (!"wh_manager".equals(user.getName())) {
            log.warn("checkManagerAccess() - forbidden for user: {}", user.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
    }

    // HttpServletRequest 사용
    @GetMapping
    public String managerPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        // X-Powered-By 헤더 설정 (CVE 테스트용)
        response.setHeader("X-Powered-By", "Spring Boot 2.6.5");

        checkManagerAccess(request);

        try {
            List<Reservation> reservations = managerService.getAllReservations();
            log.info("checkManagerAccess() - reservations.size(): {}", reservations.size());
            model.addAttribute("reservations", reservations);
        } catch (Exception e) {
            log.warn("checkManagerAccess() - 예약 목록 조회 실패: {}", e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
        }
        return "manager";
    }

    // 좌석 변경 페이지
    @GetMapping("/change-seat/{reservationId}")
    public String changeSeatPage(@PathVariable Long reservationId, Model model, HttpServletRequest request) {
        checkManagerAccess(request);

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
    public String changeSeat(@PathVariable Long reservationId, @RequestParam Long newSeatId, HttpServletRequest request) {
        checkManagerAccess(request);

        try {
            managerService.changeSeat(reservationId, newSeatId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // POST 요청 처리 (테스트용)
    @PostMapping
    public String managerPagePost(Model model, HttpServletResponse response, Dummy dummy, HttpServletRequest request) {
        // X-Powered-By 헤더 설정 (CVE 테스트용)
        response.setHeader("X-Powered-By", "Spring Boot 2.6.5");

        log.info("checkManagerAccess() - dummy.test: {}", dummy != null ? dummy.getTest() : "null");

        // wh_manager만 접근 가능
        checkManagerAccess(request);

        try {
            List<Reservation> reservations = managerService.getAllReservations();
            model.addAttribute("reservations", reservations);
        } catch (Exception e) {
            log.warn("checkManagerAccess() - 예약 목록 조회 실패: {}", e.getMessage());
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