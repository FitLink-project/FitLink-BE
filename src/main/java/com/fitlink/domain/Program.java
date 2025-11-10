package com.fitlink.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(nullable = false)
    private String name;

    @Column(name = "target", length = 50)
    private String target;

    @Column(name = "days", length = 100)
    private String days;

    @Column(name = "time", length = 30)
    private String time;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "price")
    private Integer price;
}

