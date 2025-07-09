package com.WHS.whair.repository;

import com.WHS.whair.entity.User;
import com.WHS.whair.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyPageRepository extends JpaRepository<User, Long> {
    
    // 사용자 정보 조회
    Optional<User> findByName(String name);
    
    // 사용자 예약 목록 조회 (Flight와 Seat 정보 포함)
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.flight f " +
           "JOIN FETCH r.seat s " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.bookedAt DESC")
    List<Reservation> findReservationsByUserId(@Param("userId") Long userId);
}
