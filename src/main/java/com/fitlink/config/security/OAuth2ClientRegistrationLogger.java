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
                // account_email scope 체크만 수행 (에러 발생 시 로깅)
                if (kakaoRegistration.getScopes().contains("account_email")) {
                    log.error("카카오 scopes에 account_email이 포함되어 있습니다: {}", kakaoRegistration.getScopes());
                }
            }
        } catch (Exception e) {
            log.error("ClientRegistration 조회 중 오류", e);
        }
    }
}
