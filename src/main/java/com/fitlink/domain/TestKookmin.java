package com.fitlink.domain;

import com.fitlink.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_kookmin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestKookmin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id2", nullable = false)
    private Users user;

    @Column(name = "grip_strength", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal gripStrength;

    @Column(name = "sit_up", nullable = false)
    private Integer sitUp;

    @Column(name = "sit_and_reach", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal sitAndReach;

    @Column(name = "shuttle_run", nullable = false)
    private Integer shuttleRun;

    @Column(name = "sprint", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal sprint;

    @Column(name = "standing_long_jump", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal standingLongJump;
}

