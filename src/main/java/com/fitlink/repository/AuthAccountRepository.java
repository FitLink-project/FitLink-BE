package com.fitlink.repository;

import com.fitlink.domain.AuthAccount;
import com.fitlink.domain.Users;
import com.fitlink.domain.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {
    
    // Provider와 ExternalId로 조회 (소셜 로그인용)
    Optional<AuthAccount> findByProviderAndExternalId(Provider provider, String externalId);
    
    // User와 Provider로 조회 (사용자의 특정 Provider 계정 조회)
    Optional<AuthAccount> findByUserAndProvider(Users user, Provider provider);
    
    // User로 모든 AuthAccount 조회
    java.util.List<AuthAccount> findByUser(Users user);
}

