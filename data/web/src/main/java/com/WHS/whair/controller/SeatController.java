package com.WHS.whair.controller;

import com.WHS.whair.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

// =================================================================
// SeatController - STEP 1: 좌석 선택 관련 API
// =================================================================
// 
// [주요 기능]
// - 좌석 현황 조회 API
// - 좌석 선택 유효성 검증 API
// - 좌석 선택 확정 API
@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    // =================================================================
    // 좌석 현황 조회 API
    // =================================================================

    // 좌석 현황 조회 API (좌석 맵 표시용)
    @GetMapping("/{flightId}/map")
    public ResponseEntity<Map<String, Object>> getSeatMap(@PathVariable Long flightId,
                                                         @RequestParam String seatClass) {
        try {
            Map<String, Object> seatData = seatService.getSeatMap(flightId, seatClass);
            return ResponseEntity.ok(seatData);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "좌석 현황을 가져오는데 실패했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 예약 가능한 좌석 조회 API
    @GetMapping("/{flightId}/available")
    public ResponseEntity<Map<String, Object>> getAvailableSeats(@PathVariable Long flightId,
                                                                @RequestParam String seatClass) {
        try {
            List<String> availableSeats = seatService.getAvailableSeats(flightId, seatClass);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
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

    // =================================================================
    // 좌석 선택 검증 및 확정 API
    // =================================================================

    // 좌석 선택 유효성 검증 API
    @PostMapping("/{flightId}/validate")
    public ResponseEntity<Map<String, Object>> validateSeatSelection(@PathVariable Long flightId,
                                                                    @RequestBody Map<String, Object> requestData) {
        try {
            List<String> selectedSeats = (List<String>) requestData.get("selectedSeats");
            
            if (selectedSeats == null || selectedSeats.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "좌석을 선택해주세요.");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean isValid = seatService.validateSeatSelection(flightId, selectedSeats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", isValid);
            if (isValid) {
                response.put("message", "선택한 좌석이 유효합니다.");
            } else {
                response.put("message", "선택한 좌석 중 예약 불가능한 좌석이 있습니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "좌석 검증 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 좌석 선택 확정 API (STEP 1 완료)
    @PostMapping("/{flightId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmSeatSelection(@PathVariable Long flightId,
                                                                   @RequestBody Map<String, Object> requestData) {
        try {
            List<String> selectedSeats = (List<String>) requestData.get("selectedSeats");
            
            if (selectedSeats == null || selectedSeats.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "좌석을 선택해주세요.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = seatService.confirmSeatSelection(flightId, selectedSeats);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "좌석 선택 확정 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 