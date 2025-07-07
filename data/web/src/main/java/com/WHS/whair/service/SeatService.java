package com.WHS.whair.service;

import com.WHS.whair.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


@Service
public class SeatService {
    
    @Autowired
    private SeatRepository seatRepository;

    // =================================================================
    // 좌석 현황 조회 관련
    // =================================================================
    
    // 좌석 현황 조회 (좌석 맵 표시용)
    public Map<String, Object> getSeatMap(Long flightId, String seatClass) {
        Map<String, Object> seatData = new HashMap<>();
        
        try {
            System.out.println("좌석 맵 조회 - flightId: " + flightId + ", seatClass: " + seatClass);
            
            List<String> reservedSeats = seatRepository.findReservedSeatNumbersByClass(flightId, seatClass);
            List<String> availableSeats = seatRepository.findAvailableSeatNumbersByClass(flightId, seatClass);
            
            System.out.println("예약된 좌석: " + reservedSeats);
            System.out.println("예약 가능 좌석: " + availableSeats);
            
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", reservedSeats);
            seatData.put("availableSeats", availableSeats);
            
        } catch (Exception e) {
            System.err.println("좌석 맵 조회 실패: " + e.getMessage());
            e.printStackTrace();
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", new ArrayList<>());
            seatData.put("availableSeats", new ArrayList<>());
        }
        
        return seatData;
    }

    // 예약 가능한 좌석 목록 조회
    public List<String> getAvailableSeats(Long flightId, String seatClass) {
        try {
            return seatRepository.findAvailableSeatNumbersByClass(flightId, seatClass);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // =================================================================
    // 좌석 선택 검증 관련
    // =================================================================
    
    // 좌석 선택 유효성 검증
    public boolean validateSeatSelection(Long flightId, List<String> selectedSeats) {
        try {
            // 1. 좌석 존재 여부 확인
            long existingSeatsCount = seatRepository.countExistingSeats(flightId, selectedSeats);
            if (existingSeatsCount != selectedSeats.size()) {
                return false; // 존재하지 않는 좌석이 포함됨
            }
            
            // 2. 예약 가능 여부 확인
            long availableSeatsCount = seatRepository.countAvailableSeats(flightId, selectedSeats);
            if (availableSeatsCount != selectedSeats.size()) {
                return false; // 이미 예약된 좌석이 포함됨
            }
            
            // 3. 좌석 클래스 일치 여부 확인
            List<String> seatClasses = seatRepository.findSeatClassesBySeatNumbers(flightId, selectedSeats);
            if (seatClasses.size() > 1) {
                return false; // 서로 다른 클래스의 좌석이 혼합됨
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    // =================================================================
    // 좌석 선택 확정 관련
    // =================================================================
    
    // 좌석 선택 확정 (STEP 1 완료)
    public Map<String, Object> confirmSeatSelection(Long flightId, List<String> selectedSeats) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 좌석 선택 유효성 재검증
            if (!validateSeatSelection(flightId, selectedSeats)) {
                result.put("success", false);
                result.put("message", "선택한 좌석 중 예약 불가능한 좌석이 있습니다.");
                return result;
            }
            
            // 기본 가격 정보 조회
            BigDecimal basePrice = seatRepository.calculateTotalPrice(flightId, selectedSeats);
            
            result.put("success", true);
            result.put("flightId", flightId);
            result.put("selectedSeats", selectedSeats);
            result.put("basePrice", basePrice);
            result.put("message", "좌석 선택이 확정되었습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "좌석 선택 확정 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
} 