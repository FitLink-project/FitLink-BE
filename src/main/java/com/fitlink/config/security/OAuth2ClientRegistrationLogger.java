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
        log.info("=== OAuth2 ClientRegistration 설정 확인 ===");
        
        try {
            ClientRegistration kakaoRegistration = clientRegistrationRepository.findByRegistrationId("kakao");
            if (kakaoRegistration != null) {
                log.info("📋 카카오 ClientRegistration:");
                log.info("  - RegistrationId: {}", kakaoRegistration.getRegistrationId());
                log.info("  - ClientId: {}", kakaoRegistration.getClientId());
                log.info("  - Scopes: {}", kakaoRegistration.getScopes());
                log.info("  - AuthorizationUri: {}", kakaoRegistration.getProviderDetails().getAuthorizationUri());
                log.info("  - TokenUri: {}", kakaoRegistration.getProviderDetails().getTokenUri());
                log.info("  - RedirectUri: {}", kakaoRegistration.getRedirectUri());
                
                if (kakaoRegistration.getScopes().contains("account_email")) {
                    log.error("❌ 문제 발견: 카카오 ClientRegistration의 scopes에 account_email이 포함되어 있습니다!");
                    log.error("  현재 scopes: {}", kakaoRegistration.getScopes());
                    log.error("  이것은 application.properties에서 제거했지만, 어딘가에서 여전히 추가되고 있습니다.");
                    log.error("  해결 방법:");
                    log.error("    1. Docker 컨테이너를 완전히 재빌드");
                    log.error("    2. application.properties 파일이 JAR에 제대로 포함되었는지 확인");
                    log.error("    3. 카카오 개발자 콘솔에서 account_email을 '선택' 동의항목으로 설정하고 백엔드에서도 포함");
                } else {
                    log.info("✅ 카카오 ClientRegistration의 scopes가 올바릅니다: {}", kakaoRegistration.getScopes());
                    log.info("  이제 account_email 없이 로그인이 가능해야 합니다.");
                }
            } else {
                log.warn("⚠️ 카카오 ClientRegistration을 찾을 수 없습니다!");
            }
            
            ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
            if (googleRegistration != null) {
                log.info("📋 구글 ClientRegistration:");
                log.info("  - RegistrationId: {}", googleRegistration.getRegistrationId());
                log.info("  - ClientId: {}", googleRegistration.getClientId());
                log.info("  - Scopes: {}", googleRegistration.getScopes());
            }
            
        } catch (Exception e) {
            log.error("ClientRegistration 조회 중 오류 발생", e);
        }
        
        log.info("=== OAuth2 ClientRegistration 설정 확인 완료 ===");
    }
}
