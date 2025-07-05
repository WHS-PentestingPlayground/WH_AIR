package com.WHS.whair.service;

import com.WHS.whair.entity.Flight;
import com.WHS.whair.dto.FlightSearchResultDTO;
import com.WHS.whair.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * =================================================================
 * FlightService - 항공편 및 좌석 관리 서비스
 * =================================================================
 * 
 * [주요 기능]
 * 1. 항공편 검색 및 조회
 * 2. 좌석 현황 관리 
 * 3. 예약 처리 및 검증
 * 4. 비즈니스 로직 처리
 * 
 * [Repository 연동]
 * - FlightRepository: 항공편 및 좌석 관련 모든 DB 작업 처리
 * - reservations 테이블: 별도 예약 관리 테이블 (향후 연동 예정)
 * 
 * [개발 단계]
 * - 현재: Repository 메서드 구현 중, 모의 데이터 사용
 * - 향후: 실제 DB 연동으로 전환 예정
 * 
 * [데이터베이스 구조]
 * - seats 테이블: seat_number, class, is_reserved, seat_price, fuel_price
 * - reservations 테이블: user_id, flight_id, seat_id, passenger_name, status 등
 * 
 * [보안 고려사항]
 * - 좌석 선택 시 동시성 제어 필요
 * - 예약 처리 시 트랜잭션 관리 필수
 * - 사용자 입력 검증 및 권한 확인
 */
@Service
public class FlightService {
    @Autowired
    private FlightRepository flightRepository;

    /**
     * 항공편 검색
     */
    public List<FlightSearchResultDTO> searchFlights(String departureAirport, String arrivalAirport, 
                                                     LocalDate departureDate, LocalDate arrivalDate, 
                                                     List<String> seatClasses) {
        return flightRepository.searchFlightsWithPrice(departureAirport, arrivalAirport, 
                                                       departureDate, arrivalDate, seatClasses);
    }

    /**
     * 항공편 상세 정보 조회
     */
    public FlightSearchResultDTO getFlightDetail(Long flightId, String seatClass) {
        return flightRepository.findFlightDetailByIdAndSeatClass(flightId, seatClass);
    }

    /**
     * 좌석 현황 조회 - FlightRepository 메서드 활용
     * 
     * [사용되는 Repository 메서드]
     * - findReservedSeatNumbersByClass(): 특정 클래스의 예약된 좌석 조회
     * - findAvailableSeatNumbersByClass(): 특정 클래스의 선택 가능한 좌석 조회
     */
    public Map<String, Object> getSeatMap(Long flightId, String seatClass) {
        Map<String, Object> seatData = new HashMap<>();
        
        try {
            List<String> reservedSeats = flightRepository.findReservedSeatNumbersByClass(flightId, seatClass);
            List<String> availableSeats = flightRepository.findAvailableSeatNumbersByClass(flightId, seatClass);
            
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", reservedSeats);
            seatData.put("availableSeats", availableSeats);
            
        } catch (Exception e) {
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", new ArrayList<>());
            seatData.put("availableSeats", new ArrayList<>());
        }
        
        return seatData;
    }

    /**
     * 예약 가능한 좌석 조회 - FlightRepository 메서드 활용
     * 
     * [사용되는 Repository 메서드]
     * - findAvailableSeatNumbersByClass(): 특정 클래스의 선택 가능한 좌석 조회
     */
    public List<String> getAvailableSeats(Long flightId, String seatClass) {
        try {
            return flightRepository.findAvailableSeatNumbersByClass(flightId, seatClass);
        } catch (Exception e) {
            // 에러 발생 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    /**
     * 좌석 예약 처리 - FlightRepository 메서드 활용
     * 
     * [사용되는 Repository 메서드]
     * - countExistingSeats(): 좌석 존재 여부 확인
     * - countAvailableSeats(): 선택한 좌석들이 모두 예약 가능한지 확인
     * - findSeatClassesBySeatNumbers(): 좌석 클래스 정보 조회
     * - reserveSeats(): 좌석 예약 상태 업데이트
     * 
     * [예약 처리 흐름]
     * 1. 좌석 유효성 검증
     * 2. 탑승자 정보 검증
     * 3. DB 좌석 예약 처리
     * 4. reservations 테이블에 예약 정보 저장 (향후 구현)
     */
    public Map<String, Object> bookSeats(Long flightId, List<String> selectedSeats, List<Map<String, String>> passengers) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 좌석 유효성 검증 (Repository 메서드 활용)
            if (!validateSeatSelection(flightId, selectedSeats)) {
                result.put("success", false);
                result.put("message", "선택한 좌석 중 예약 불가능한 좌석이 있습니다.");
                return result;
            }
            
            // 2. 탑승자 정보 검증
            if (selectedSeats.size() != passengers.size()) {
                result.put("success", false);
                result.put("message", "좌석 수와 탑승자 수가 일치하지 않습니다.");
                return result;
            }
            
            // 3. 실제 DB 예약 처리 (Repository 메서드 활용)
            int reservedCount = flightRepository.reserveSeats(flightId, selectedSeats);
            
            if (reservedCount != selectedSeats.size()) {
                result.put("success", false);
                result.put("message", "일부 좌석이 이미 예약되었습니다. 다시 시도해주세요.");
                return result;
            }
            
            // 4. reservations 테이블에 예약 정보 저장 (향후 구현)
            // for (int i = 0; i < selectedSeats.size(); i++) {
            //     String seatNumber = selectedSeats.get(i);
            //     String passengerName = passengers.get(i).get("name");
            //     // reservationRepository.save(new Reservation(...));
            // }
            
            // 5. 예약 완료 응답
            result.put("success", true);
            result.put("message", "예약이 완료되었습니다.");
            result.put("selectedSeats", selectedSeats);
            result.put("passengers", passengers);
            result.put("flightId", flightId);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 좌석 선택 유효성 검증 - FlightRepository 메서드 활용
     * 
     * [사용되는 Repository 메서드]
     * - countExistingSeats(): 입력한 좌석들이 실제 존재하는지 확인
     * - countAvailableSeats(): 선택한 좌석들이 모두 예약 가능한지 확인
     * - findSeatClassesBySeatNumbers(): 좌석 클래스 일치 여부 확인
     * 
     * [검증 단계]
     * 1. 좌석 존재 여부 확인: 잘못된 좌석 번호 입력 방지
     * 2. 예약 가능 여부 확인: 이미 예약된 좌석 선택 방지
     * 3. 클래스 일치 확인: 선택한 클래스와 좌석 클래스 일치 여부
     */
    private boolean validateSeatSelection(Long flightId, List<String> selectedSeats) {
        try {
            // 실제 DB 검증 로직 (Repository 메서드 활용)
            
            // 1. 좌석 존재 여부 확인
            long existingSeatsCount = flightRepository.countExistingSeats(flightId, selectedSeats);
            if (existingSeatsCount != selectedSeats.size()) {
                return false; // 존재하지 않는 좌석이 포함됨
            }
            
            // 2. 예약 가능 여부 확인
            long availableSeatsCount = flightRepository.countAvailableSeats(flightId, selectedSeats);
            if (availableSeatsCount != selectedSeats.size()) {
                return false; // 이미 예약된 좌석이 포함됨
            }
            
            // 3. 추가 검증: 좌석 클래스 일치 여부 확인
            List<String> seatClasses = flightRepository.findSeatClassesBySeatNumbers(flightId, selectedSeats);
            if (seatClasses.size() > 1) {
                return false; // 서로 다른 클래스의 좌석이 혼합됨
            }
            
            return true;
            
        } catch (Exception e) {
            // 검증 과정에서 오류 발생 시 안전을 위해 false 반환
            return false;
        }
    }




} 