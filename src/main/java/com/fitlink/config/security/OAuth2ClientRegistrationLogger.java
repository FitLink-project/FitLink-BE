package com.fitlink.config.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

/**
 * OAuth2 ClientRegistration 설정을 로깅하여 디버깅에 도움을 주는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ClientRegistrationLogger {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @PostConstruct
    public void logClientRegistrations() {
        try {
            // 카카오 설정 확인
            ClientRegistration kakaoRegistration = clientRegistrationRepository.findByRegistrationId("kakao");
            if (kakaoRegistration == null) {
                log.error("카카오 ClientRegistration을 찾을 수 없습니다.");
            } else {
                log.info("=== 카카오 ClientRegistration 설정 확인 ===");
                log.info("Client ID: {}", kakaoRegistration.getClientId());
                log.info("Redirect URI: {}", kakaoRegistration.getRedirectUri());
                log.info("Token URI: {}", kakaoRegistration.getProviderDetails().getTokenUri());
                log.info("Scopes: {}", kakaoRegistration.getScopes());
                if (kakaoRegistration.getScopes().contains("account_email")) {
                    log.error("⚠️ 카카오 scopes에 account_email이 포함되어 있습니다: {}", kakaoRegistration.getScopes());
                }
                log.info("=== 카카오 개발자 콘솔 확인 필요 ===");
                log.info("1. Redirect URI가 정확히 등록되어 있는지 확인:");
                log.info("   https://www.fitlink1207.store/login/oauth2/code/kakao");
                log.info("2. Client Secret이 카카오 개발자 콘솔과 일치하는지 확인");
                log.info("3. Client ID가 카카오 개발자 콘솔과 일치하는지 확인");
            }
            
            // 구글 설정 확인 (비교용)
            ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
            if (googleRegistration != null) {
                log.info("=== 구글 ClientRegistration 설정 (성공 사례) ===");
                log.info("Client ID: {}", googleRegistration.getClientId());
                log.info("Redirect URI: {}", googleRegistration.getRedirectUri());
                log.info("Token URI: {}", googleRegistration.getProviderDetails().getTokenUri());
                log.info("Scopes: {}", googleRegistration.getScopes());
            }
            
        } catch (Exception e) {
            log.error("ClientRegistration 조회 중 오류", e);
        }
    }
}
