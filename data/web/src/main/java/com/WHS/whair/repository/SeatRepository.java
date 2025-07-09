package com.WHS.whair.repository;

import com.WHS.whair.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatClass = 'business' AND s.isReserved = false")
List<Seat> findAvailableBusinessSeats(@Param("flightId") Long flightId);
    
    List<Seat> findByFlightId(Long flightId);
} 