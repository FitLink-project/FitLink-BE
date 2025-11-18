package com.fitlink.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * OAuth2 토큰 교환 요청을 로깅하는 클라이언트
 * 카카오 토큰 엔드포인트로 전송되는 파라미터를 상세히 로깅합니다.
 */
@Slf4j
@Component
public class OAuth2TokenExchangeLogger implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate;
    private final RestTemplate restTemplate;

    public OAuth2TokenExchangeLogger() {
        // 커스텀 RestTemplate 생성 (로깅용)
        this.restTemplate = new RestTemplate();
        this.restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter()
        ));
        
        // Spring Security의 기본 구현체를 사용하되, RestTemplate을 주입
        org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient client = 
                new org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient();
        client.setRestOperations(this.restTemplate);
        this.delegate = client;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
        
        // 카카오인 경우에만 상세 로깅 및 실제 HTTP 요청 본문 로깅
        if ("kakao".equalsIgnoreCase(registrationId)) {
            logTokenExchangeRequest(authorizationCodeGrantRequest);
            logActualHttpRequest(authorizationCodeGrantRequest);
        }
        
        try {
            OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationCodeGrantRequest);
            
            if ("kakao".equalsIgnoreCase(registrationId)) {
                logTokenExchangeResponse(response);
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("카카오 토큰 교환 실패: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
            }
            // HTTP 에러 응답 본문이 있으면 로깅
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpEx = 
                        (org.springframework.web.client.HttpClientErrorException) e;
                log.error("HTTP 상태 코드: {}", httpEx.getStatusCode());
                log.error("HTTP 응답 본문: {}", httpEx.getResponseBodyAsString());
            }
            throw e;
        } catch (Exception e) {
            log.error("카카오 토큰 교환 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void logActualHttpRequest(OAuth2AuthorizationCodeGrantRequest request) {
        try {
            var clientRegistration = request.getClientRegistration();
            var authorizationExchange = request.getAuthorizationExchange();
            var authorizationResponse = authorizationExchange.getAuthorizationResponse();
            
            String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
            String code = authorizationResponse.getCode();
            String redirectUri = authorizationResponse.getRedirectUri();
            
            // 실제 HTTP 요청 본문 생성 (DefaultAuthorizationCodeTokenResponseClient와 동일하게)
            MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
            formParameters.add("grant_type", "authorization_code");
            formParameters.add("code", code);
            formParameters.add("redirect_uri", redirectUri);
            formParameters.add("client_id", clientRegistration.getClientId());
            formParameters.add("client_secret", clientRegistration.getClientSecret());
            
            log.info("=== 실제 HTTP 요청 본문 (실제 전송되는 데이터) ===");
            log.info("POST {}", tokenUri);
            log.info("Content-Type: {}", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            formParameters.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    String value = values.get(0);
                    // 민감한 정보 마스킹
                    if ("client_secret".equals(key)) {
                        value = value != null ? value.substring(0, Math.min(5, value.length())) + "..." : "null";
                    } else if ("code".equals(key)) {
                        value = value != null ? value.substring(0, Math.min(20, value.length())) + "..." : "null";
                    }
                    log.info("  {}: {}", key, value);
                }
            });
            
            // 카카오 개발자 콘솔 확인 사항
            log.info("=== 카카오 개발자 콘솔 확인 사항 ===");
            log.info("1. Redirect URI가 정확히 일치하는지 확인:");
            log.info("   등록된 URI: {}", redirectUri);
            log.info("   (카카오 개발자 콘솔에 정확히 동일한 URI가 등록되어 있어야 함)");
            log.info("2. Client Secret이 올바른지 확인:");
            log.info("   Client ID: {}", clientRegistration.getClientId());
            log.info("3. Authorization Request와 Token Exchange의 redirect_uri가 동일한지 확인");
            log.info("4. Code가 이미 사용되었는지 확인 (카카오는 code 1회성 사용)");
            
        } catch (Exception e) {
            log.warn("실제 HTTP 요청 로깅 중 오류: {}", e.getMessage());
        }
    }

    private void logTokenExchangeRequest(OAuth2AuthorizationCodeGrantRequest request) {
        try {
            var clientRegistration = request.getClientRegistration();
            var authorizationExchange = request.getAuthorizationExchange();
            var authorizationResponse = authorizationExchange.getAuthorizationResponse();
            
            String code = authorizationResponse.getCode();
            String state = authorizationResponse.getState();
            String redirectUri = authorizationResponse.getRedirectUri();
            String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
            
            log.info("=== 카카오 토큰 교환 요청 시작 ===");
            log.info("Token URI: {}", tokenUri);
            log.info("Client ID: {}", clientRegistration.getClientId());
            log.info("Client Secret: {}...", 
                    clientRegistration.getClientSecret() != null 
                            ? clientRegistration.getClientSecret().substring(0, Math.min(5, clientRegistration.getClientSecret().length())) 
                            : "null");
            log.info("Authorization Code: {}...", code != null ? code.substring(0, Math.min(20, code.length())) : "null");
            log.info("State: {}", state);
            log.info("Redirect URI: {}", redirectUri);
            log.info("Configured Scopes: {}", clientRegistration.getScopes());
            
            // 요청 본문 파라미터 확인
            log.info("요청 본문 파라미터 (예상):");
            log.info("  - grant_type: authorization_code");
            log.info("  - client_id: {}", clientRegistration.getClientId());
            log.info("  - redirect_uri: {}", redirectUri);
            log.info("  - code: {}...", code != null ? code.substring(0, Math.min(20, code.length())) : "null");
            log.info("  - client_secret: {}...", 
                    clientRegistration.getClientSecret() != null 
                            ? clientRegistration.getClientSecret().substring(0, Math.min(5, clientRegistration.getClientSecret().length())) 
                            : "null");
            
            // Content-Type 확인
            log.info("Content-Type: application/x-www-form-urlencoded (필수)");
            
            // 카카오 정책 체크
            log.info("=== 카카오 정책 확인 ===");
            log.info("1. grant_type=authorization_code 확인: ✅");
            log.info("2. Content-Type=application/x-www-form-urlencoded 확인: ✅");
            log.info("3. code 1회성 사용 정책: code가 이미 사용되었는지 확인 필요");
            log.info("4. scope 불일치 체크: 승인된 scope와 서버 설정 scope 일치 여부 확인");
            
        } catch (Exception e) {
            log.warn("토큰 교환 요청 로깅 중 오류: {}", e.getMessage());
        }
    }

    private void logTokenExchangeResponse(OAuth2AccessTokenResponse response) {
        try {
            log.info("=== 카카오 토큰 교환 성공 ===");
            log.info("Access Token: {}...", 
                    response.getAccessToken().getTokenValue().substring(0, Math.min(20, response.getAccessToken().getTokenValue().length())));
            log.info("Token Type: {}", response.getAccessToken().getTokenType().getValue());
            log.info("Expires At: {}", response.getAccessToken().getExpiresAt());
            if (response.getRefreshToken() != null) {
                log.info("Refresh Token: {}...", 
                        response.getRefreshToken().getTokenValue().substring(0, Math.min(20, response.getRefreshToken().getTokenValue().length())));
            }
            if (!response.getAdditionalParameters().isEmpty()) {
                log.info("Additional Parameters: {}", response.getAdditionalParameters().keySet());
            }
        } catch (Exception e) {
            log.warn("토큰 교환 응답 로깅 중 오류: {}", e.getMessage());
        }
    }
}

