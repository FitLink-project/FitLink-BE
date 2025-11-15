package com.fitlink.domain;

import com.fitlink.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "test_general")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class TestGeneral extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Column(name = "slider_strength")
    private Integer sliderStrength;

    @Column(name = "sit_up", nullable = false)
    private Integer sitUp;

    @Column(name = "sit_and_reach", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal sitAndReach;

    @Column(name = "ymca_step_test", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal ymcaStepTest;

    @Column(name = "slider_agility")
    private Integer sliderAgility;

    @Column(name = "slider_power")
    private Integer sliderPower;
}

