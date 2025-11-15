package com.fitlink.domain;

import com.fitlink.domain.common.BaseEntity;
import com.fitlink.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "del_date")
    private LocalDateTime deleteDate;

    @Column(name = "profile_url", columnDefinition = "text")
    private String profileUrl;
}

