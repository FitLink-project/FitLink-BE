package com.fitlink.domain;

import com.fitlink.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "agreements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Agreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, unique = true)
    private Users user;

    @Column(name = "privacy", nullable = false)
    @Builder.Default
    private Boolean privacy = false;

    @Column(name = "service", nullable = false)
    @Builder.Default
    private Boolean service = false;

    @Column(name = "over14", nullable = false)
    @Builder.Default
    private Boolean over14 = false;

    @Column(name = "location", nullable = false)
    @Builder.Default
    private Boolean location = false;
}

