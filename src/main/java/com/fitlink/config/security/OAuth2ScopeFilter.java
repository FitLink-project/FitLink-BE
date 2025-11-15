package com.fitlink.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * OAuth2 Authorization Request의 scope를 필터링하는 Resolver
 * 카카오의 경우 account_email을 강제로 제거합니다.
 */
@Slf4j
public class OAuth2ScopeFilter implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";
    private final AntPathRequestMatcher authorizationRequestMatcher;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public OAuth2ScopeFilter(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(
                AUTHORIZATION_REQUEST_BASE_URI + "/{registrationId}");
        this.defaultResolver = new org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return resolve(request, null);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        String registrationId = clientRegistrationId;
        if (registrationId == null) {
            registrationId = this.authorizationRequestMatcher
                    .matcher(request)
                    .getVariables()
                    .get("registrationId");
        }

        if (registrationId == null) {
            return null;
        }

        log.info("🔧 OAuth2AuthorizationRequestResolver - RegistrationId: {}", registrationId);

        // 기본 resolver로 요청 생성
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, registrationId);
        if (originalRequest == null) {
            return null;
        }

        // 카카오인 경우 scope 필터링
        if ("kakao".equalsIgnoreCase(registrationId)) {
            Set<String> originalScopes = originalRequest.getScopes();
            log.info("📋 카카오 원본 scopes: {}", originalScopes);

            // account_email 제거
            Set<String> filteredScopes = new LinkedHashSet<>();
            for (String scope : originalScopes) {
                if (!"account_email".equalsIgnoreCase(scope)) {
                    filteredScopes.add(scope);
                } else {
                    log.warn("⚠️ account_email scope가 제거되었습니다!");
                }
            }

            log.info("✅ 필터링된 scopes: {}", filteredScopes);

            // scope가 변경되었으면 새 요청 생성
            if (!filteredScopes.equals(originalScopes)) {
                OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest
                        .from(originalRequest)
                        .scopes(filteredScopes);

                Consumer<OAuth2AuthorizationRequest.Builder> additionalParametersConsumer = originalRequest
                        .getAttribute("org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver.additionalParameters.consumer");
                if (additionalParametersConsumer != null) {
                    additionalParametersConsumer.accept(builder);
                }

                OAuth2AuthorizationRequest filteredRequest = builder.build();
                log.info("🔧 OAuth2AuthorizationRequest 재생성 완료 - Filtered Scopes: {}", filteredRequest.getScopes());
                return filteredRequest;
            }
        }

        log.info("📋 최종 사용할 scopes: {}", originalRequest.getScopes());
        return originalRequest;
    }
}
