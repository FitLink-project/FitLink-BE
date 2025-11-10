package com.fitlink.domain;

import com.fitlink.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "del_date")
    private LocalDateTime deleteDate;

    @Column(name = "profile_url", columnDefinition = "text")
    private String profileUrl;
}

