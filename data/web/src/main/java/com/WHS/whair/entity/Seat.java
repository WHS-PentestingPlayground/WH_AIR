package com.WHS.whair.entity;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

/**
 * =================================================================
 * Seat Entity - 좌석 정보 관리
 * =================================================================
 * 
 * [데이터베이스 구조와 일치]
 * - seats 테이블: seat_number, class, is_reserved, seat_price, fuel_price
 * - 예약 관리는 별도 reservations 테이블에서 처리
 * 
 * [주요 필드]
 * - flight: 항공편 연관 관계 (ManyToOne)
 * - seatNumber: 좌석 번호 (예: 12A, 5F)
 * - class: 좌석 등급 (first, business, economy)
 * - isReserved: 예약 여부 (예약됨/선택가능)
 * - seatPrice: 기본 운임
 * - fuelPrice: 유류할증료
 * 
 * [예약 관리 구조]
 * - 좌석 예약 상태: seats.is_reserved (true/false)
 * - 예약자 정보: reservations 테이블에서 관리
 * - 예약 세부사항: user_id, passenger_name, status, booked_at 등
 */
@Entity
@Table(name = "seats")
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    /**
     * 좌석 등급 필드
     * - 데이터베이스 필드명: class
     * - 가능한 값: first, business, economy
     * - CHECK 제약 조건으로 값 제한
     */
    @Column(name = "class", nullable = false)
    private String seatClass;

    /**
     * 예약 상태 필드
     * - 데이터베이스 필드명: is_reserved
     * - true: 예약됨 (선택 불가)
     * - false: 선택 가능
     * - FlightRepository의 모든 쿼리에서 사용
     */
    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved = false;

    @Column(name = "seat_price", nullable = false)
    private BigDecimal seatPrice;

    @Column(name = "fuel_price", nullable = false) 
    private BigDecimal fuelPrice;

    /**
     * 예약 상태 관리 메서드
     * - 좌석 예약 처리 시 사용
     * - Repository의 reserveSeats() 메서드와 연동
     */
    public void reserve() {
        this.isReserved = true;
    }

    /**
     * 예약 취소 메서드
     * - 좌석 예약 취소 시 사용
     * - Repository의 cancelReservation() 메서드와 연동
     */
    public void cancelReservation() {
        this.isReserved = false;
    }

    /**
     * 예약 가능 여부 확인 메서드
     * - 좌석 선택 UI에서 사용
     * - 예약 가능 좌석 필터링에 활용
     */
    public boolean isAvailable() {
        return !this.isReserved;
    }
} 
