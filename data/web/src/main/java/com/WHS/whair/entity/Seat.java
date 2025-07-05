package com.WHS.whair.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "class")
    private String seatClass;

    @Column(name = "is_reserved")
    private boolean isReserved;

    @Column(name = "seat_price")
    private BigDecimal seatPrice;

    @Column(name = "fuel_price")
    private BigDecimal fuelPrice;
}
