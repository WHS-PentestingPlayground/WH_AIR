package com.WHS.whair.entity;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "flights")
@Data

public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number")
    private String flightNumber;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    private String airline;

    @Column(name = "aircraft_model")
    private String aircraftModel;

    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Seat> seats;
} 
