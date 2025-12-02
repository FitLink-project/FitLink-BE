package com.fitlink.domain;

import com.fitlink.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "fitness_result")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class FitnessResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fitness_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="kookmin_result_id")
    private TestKookmin kookminResultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="general_result_id")
    private TestGeneral generalResultId;

    @Column(name = "strength")
    private Float strength;

    @Column(name = "muscular")
    private Float muscular;

    @Column(name = "flexibility")
    private Float flexibility;

    @Column(name = "cardiopulmonary")
    private Float cardiopulmonary;

    @Column(name = "agility")
    private Float agility;

    @Column(name = "quickness")
    private Float quickness;

}

