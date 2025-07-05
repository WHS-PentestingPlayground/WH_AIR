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
import java.util.Random;

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
            // 실제 DB에서 좌석 정보 조회
            System.out.println("=== DB 좌석 정보 조회 시작 ===");
            System.out.println("flightId: " + flightId + ", seatClass: " + seatClass);
            
            List<String> reservedSeats = flightRepository.findReservedSeatNumbersByClass(flightId, seatClass);
            List<String> availableSeats = flightRepository.findAvailableSeatNumbersByClass(flightId, seatClass);
            
            System.out.println("예약된 좌석 (DB): " + reservedSeats);
            System.out.println("선택 가능한 좌석 (DB): " + availableSeats.size() + "개");
            
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", reservedSeats);
            seatData.put("availableSeats", availableSeats);
            seatData.put("totalSeats", reservedSeats.size() + availableSeats.size());
            seatData.put("dataSource", "database"); // 실제 DB 데이터임을 표시
            
            System.out.println("=== DB 좌석 정보 조회 성공 ===");
            
        } catch (Exception e) {
            // 데이터베이스 조회 실패 시 실제 예약 정보를 반영한 모의 데이터로 대체
            System.err.println("=== DB 좌석 정보 조회 실패 ===");
            System.err.println("에러: " + e.getMessage());
            e.printStackTrace();
            
            List<String> reservedSeats = generateReservedSeatsWithRealData(seatClass);
            List<String> availableSeats = generateAvailableSeats(seatClass, reservedSeats);
            
            seatData.put("flightId", flightId);
            seatData.put("seatClass", seatClass);
            seatData.put("reservedSeats", reservedSeats);
            seatData.put("availableSeats", availableSeats);
            seatData.put("totalSeats", reservedSeats.size() + availableSeats.size());
            seatData.put("dataSource", "mock_with_real_data"); // 실제 예약 정보 반영된 모의 데이터 사용
            seatData.put("error", e.getMessage()); // 에러 메시지 포함
            
            System.out.println("예약된 좌석 (모의데이터): " + reservedSeats);
        }
        
        return seatData;
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
            
            // 3. 실제 DB 예약 처리 (Repository 메서드 활용 예정)
            /*
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
            */
            
            // 현재는 Repository 컴파일 에러로 인해 임시 처리
            String bookingReference = generateBookingReference();
            
            // 5. 예약 완료 응답
            result.put("success", true);
            result.put("message", "예약이 완료되었습니다.");
            result.put("bookingReference", bookingReference);
            result.put("selectedSeats", selectedSeats);
            result.put("passengers", passengers);
            result.put("flightId", flightId);
            result.put("processingMode", "mock"); // 현재 모의 처리 모드임을 표시
            
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
            // 실제 DB 검증 로직 (Repository 메서드 활용 예정)
            /*
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
            
            // 3. 추가 검증: 좌석 클래스 일치 여부 확인 (필요시)
            // List<String> seatClasses = flightRepository.findSeatClassesBySeatNumbers(flightId, selectedSeats);
            // if (seatClasses.size() > 1) {
            //     return false; // 서로 다른 클래스의 좌석이 혼합됨
            // }
            */
            
            // 현재는 Repository 컴파일 에러로 인해 모의 검증 사용
            Map<String, Object> seatMap = getSeatMap(flightId, "economy"); // 기본값으로 economy 사용
            List<String> reservedSeats = (List<String>) seatMap.get("reservedSeats");
            
            // 선택한 좌석이 이미 예약된 좌석인지 확인
            for (String seat : selectedSeats) {
                if (reservedSeats.contains(seat)) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            // 검증 과정에서 오류 발생 시 안전을 위해 false 반환
            return false;
        }
    }

    // =================================================================
    // 유틸리티 메서드 (Utility Methods)
    // =================================================================
    
    /**
     * 예약 번호 생성
     * 
     * [생성 규칙]
     * - 접두사: "WH" (WH Air 식별자)
     * - 형식: WH + 6자리 영숫자 조합
     * - 예시: WH7A9K2L, WHBX5M8P
     * 
     * [실제 운영환경에서는]
     * - UUID 또는 Sequence 기반 생성 권장
     * - 중복 방지를 위한 DB 유니크 제약 필요
     * - 예약 시간, 항공편 정보 등을 포함한 의미있는 코드 생성 고려
     */
    private String generateBookingReference() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return "WH" + sb.toString();
    }

    // =================================================================
    // 모의 데이터 생성 메서드 (Mock Data Generation)
    // - Repository 구현 완료 후 제거 예정
    // =================================================================
    
    /**
     * 실제 예약 정보를 반영한 좌석 목록 생성 (모의 데이터)
     * 
     * [실제 예약 정보]
     * - 김철수: 11A (이코노미)
     * - 이영희: 12F (이코노미)
     * 
     * [실제 대체될 Repository 메서드]
     * - flightRepository.findReservedSeatNumbersByClass(flightId, seatClass)
     */
    private List<String> generateReservedSeatsWithRealData(String seatClass) {
        List<String> reservedSeats = new ArrayList<>();
        
        // 실제 DB에 있는 예약 정보를 반영
        if ("economy".equalsIgnoreCase(seatClass)) {
            // 김철수: 11A, 이영희: 12F
            reservedSeats.add("11A");
            reservedSeats.add("12F");
        }
        // 다른 클래스(first, business)는 예약된 좌석이 없음
        
        System.out.println("실제 예약 정보 반영된 좌석: " + reservedSeats);
        return reservedSeats;
    }

    /**
     * 이미 예약된 좌석 목록 생성 (기존 랜덤 모의 데이터)
     * 
     * [실제 대체될 Repository 메서드]
     * - flightRepository.findReservedSeatNumbersByClass(flightId, seatClass)
     * 
     * [좌석 배치 규칙]
     * - First Class: 1-3열, A-B (2석/열, 총 6석)
     * - Business Class: 4-10열, A-D (4석/열, 총 28석)  
     * - Economy Class: 11-30열, A-F (6석/열, 총 120석)
     * 
     * [모의 예약률]
     * - 25% 확률로 랜덤 예약 설정
     * - 실제로는 DB에서 is_reserved = true인 좌석 조회
     */
    private List<String> generateReservedSeats(String seatClass) {
        List<String> reservedSeats = new ArrayList<>();
        Random random = new Random();
        
        // 좌석 클래스별 범위 설정
        int startRow, endRow;
        char[] letters;
        
        switch (seatClass.toLowerCase()) {
            case "first":
                startRow = 1; endRow = 3;
                letters = new char[]{'A', 'B'};
                break;
            case "business":
                startRow = 4; endRow = 10;
                letters = new char[]{'A', 'B', 'C', 'D'};
                break;
            default: // economy
                startRow = 11; endRow = 30;
                letters = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};
                break;
        }
        
        // 랜덤하게 25% 정도의 좌석을 예약된 상태로 설정
        for (int row = startRow; row <= endRow; row++) {
            for (char letter : letters) {
                if (random.nextDouble() < 0.25) { // 25% 확률로 예약됨
                    reservedSeats.add(row + String.valueOf(letter));
                }
            }
        }
        
        return reservedSeats;
    }

    /**
     * 선택 가능한 좌석 목록 생성 (모의 데이터)
     * 
     * [실제 대체될 Repository 메서드]
     * - flightRepository.findAvailableSeatNumbersByClass(flightId, seatClass)
     * 
     * [생성 로직]
     * - 전체 좌석에서 예약된 좌석을 제외한 나머지
     * - 실제로는 DB에서 is_reserved = false인 좌석 조회
     * 
     * [사용 목적]
     * - 좌석 선택 UI에서 선택 가능한 옵션 제공
     * - 예약 가능 좌석 수 계산
     */
    private List<String> generateAvailableSeats(String seatClass, List<String> reservedSeats) {
        List<String> availableSeats = new ArrayList<>();
        
        // 좌석 클래스별 범위 설정 (위와 동일)
        int startRow, endRow;
        char[] letters;
        
        switch (seatClass.toLowerCase()) {
            case "first":
                startRow = 1; endRow = 3;
                letters = new char[]{'A', 'B'};
                break;
            case "business":
                startRow = 4; endRow = 10;
                letters = new char[]{'A', 'B', 'C', 'D'};
                break;
            default: // economy
                startRow = 11; endRow = 30;
                letters = new char[]{'A', 'B', 'C', 'D', 'E', 'F'};
                break;
        }
        
        // 모든 좌석에서 예약된 좌석 제외
        for (int row = startRow; row <= endRow; row++) {
            for (char letter : letters) {
                String seat = row + String.valueOf(letter);
                if (!reservedSeats.contains(seat)) {
                    availableSeats.add(seat);
                }
            }
        }
        
        return availableSeats;
    }
} 