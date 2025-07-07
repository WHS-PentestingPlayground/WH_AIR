package com.WHS.whair.repository;

import com.WHS.whair.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // =================================================================
    // 예약 정보 관련
    // =================================================================
    
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.flight f " +
           "JOIN FETCH r.seat s " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.bookedAt DESC")
    List<Reservation> findByUserIdWithFlightAndSeat(@Param("userId") Long userId);
    
    List<Reservation> findByUserId(Long userId);

    // =================================================================
    // 결제 관련 - 사용자 포인트 관리
    // =================================================================
    
    // 사용자 포인트 조회
    @Query("SELECT u.point FROM User u WHERE u.id = :userId")
    Integer findUserPoints(@Param("userId") Long userId);
    
    // 사용자 포인트 차감
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.point = u.point - :usedPoints WHERE u.id = :userId AND u.point >= :usedPoints")
    int deductUserPoints(@Param("userId") Long userId, @Param("usedPoints") int usedPoints);

    // =================================================================
    // 결제 관련 - 사용자 쿠폰 관리
    // =================================================================
    
    // 사용자 쿠폰 조회
    @Query("SELECT u.coupon FROM User u WHERE u.id = :userId")
    String findUserCoupon(@Param("userId") Long userId);
    
    // 사용자 쿠폰 사용 처리 (Race Condition 방지)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.coupon = NULL WHERE u.id = :userId AND u.coupon = :couponCode")
    int useUserCoupon(@Param("userId") Long userId, @Param("couponCode") String couponCode);
}