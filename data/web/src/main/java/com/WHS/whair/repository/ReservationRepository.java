package com.WHS.whair.repository;

import com.WHS.whair.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.flight f " +
           "JOIN FETCH r.seat s " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.bookedAt DESC")
    List<Reservation> findByUserIdWithFlightAndSeat(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.user u " +
           "JOIN FETCH r.flight f " +
           "JOIN FETCH r.seat s " +
           "ORDER BY r.bookedAt DESC")
    List<Reservation> findAllWithUserAndFlightAndSeat();
    
    List<Reservation> findByUserId(Long userId);
}