package com.fitlink.domain;

import com.fitlink.domain.enums.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Provider provider;

    @Column(name = "social_token", length = 255)
    private String socialToken;
    
    @Column(name = "ext_id", length = 255)
    private String externalId;
}

