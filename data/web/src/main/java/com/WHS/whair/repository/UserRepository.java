package com.WHS.whair.repository;

import com.WHS.whair.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    
    /* 포인트 및 쿠폰 관련 쿼리 */
    
    // 사용자 현재 포인트 조회
    @Query("SELECT u.point FROM User u WHERE u.id = :userId")
    Optional<Integer> findPointsByUserId(@Param("userId") Long userId);
    
    // 사용자 현재 쿠폰 조회
    @Query("SELECT u.coupon FROM User u WHERE u.id = :userId")
    Optional<String> findCouponByUserId(@Param("userId") Long userId);

    // 사용자 포인트 차감
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.point = u.point - :pointsToDeduct " +
           "WHERE u.id = :userId AND u.point >= :pointsToDeduct")
    int deductPoints(@Param("userId") Long userId, @Param("pointsToDeduct") Integer pointsToDeduct);
    
    // 사용자 쿠폰 사용 처리
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.coupon = null WHERE u.id = :userId AND u.coupon = :couponCode")
    int useCouponByCode(@Param("userId") Long userId, @Param("couponCode") String couponCode);
}
