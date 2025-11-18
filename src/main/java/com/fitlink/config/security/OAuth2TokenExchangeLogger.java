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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 토큰 교환 요청을 로깅하는 클라이언트
 * 카카오 토큰 엔드포인트로 전송되는 파라미터를 상세히 로깅합니다.
 */
@Slf4j
@Component
public class OAuth2TokenExchangeLogger implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate;
    private final RestTemplate restTemplate;
    
    // 사용된 authorization code 추적 (재사용 방지)
    // Key: authorization code, Value: 사용 시간
    private final ConcurrentHashMap<String, LocalDateTime> usedCodes = new ConcurrentHashMap<>();
    
    // 오래된 code 정리를 위한 스케줄러 (10분마다 실행)
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

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
        
        // 오래된 code 정리 스케줄러 시작 (10분마다 실행, 30분 이상 된 code 제거)
        cleanupScheduler.scheduleAtFixedRate(this::cleanupOldCodes, 10, 10, TimeUnit.MINUTES);
    }
    
    /**
     * 30분 이상 된 code를 메모리에서 제거
     */
    private void cleanupOldCodes() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);
            int removedCount = 0;
            
            // 제거할 code 목록 수집
            var codesToRemove = usedCodes.entrySet().stream()
                    .filter(entry -> entry.getValue().isBefore(cutoffTime))
                    .map(entry -> entry.getKey())
                    .toList();
            
            // 수집된 code 제거
            for (String code : codesToRemove) {
                if (usedCodes.remove(code) != null) {
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                log.debug("오래된 authorization code {}개 정리 완료", removedCount);
            }
        } catch (Exception e) {
            log.warn("authorization code 정리 중 오류: {}", e.getMessage());
        }
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
        var authorizationResponse = authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse();
        String code = authorizationResponse.getCode();
        
        // 카카오인 경우에만 code 재사용 체크 및 상세 로깅
        if ("kakao".equalsIgnoreCase(registrationId)) {
            // Code 재사용 체크
            checkCodeReuse(code);
            
            logTokenExchangeRequest(authorizationCodeGrantRequest);
            logActualHttpRequest(authorizationCodeGrantRequest);
        }
        
        try {
            OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationCodeGrantRequest);
            
            // 성공 시 code를 사용된 목록에 추가
            if ("kakao".equalsIgnoreCase(registrationId)) {
                markCodeAsUsed(code);
                logTokenExchangeResponse(response);
            }
            
            return response;
        } catch (RestClientException e) {
            // 실패 시에도 code를 사용된 것으로 표시 (카카오는 code를 무효화할 수 있음)
            if ("kakao".equalsIgnoreCase(registrationId) && code != null) {
                markCodeAsUsed(code);
            }
            
            log.error("=== 카카오 토큰 교환 실패 ===");
            log.error("에러 메시지: {}", e.getMessage());
            
            // HTTP 에러 응답 본문이 있으면 로깅
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpEx = 
                        (org.springframework.web.client.HttpClientErrorException) e;
                log.error("HTTP 상태 코드: {}", httpEx.getStatusCode());
                log.error("HTTP 상태 텍스트: {}", httpEx.getStatusText());
                
                try {
                    String responseBody = httpEx.getResponseBodyAsString();
                    if (responseBody != null && !responseBody.isEmpty()) {
                        log.error("HTTP 응답 본문: {}", responseBody);
                    } else {
                        log.error("HTTP 응답 본문: 없음 (카카오가 에러 본문을 반환하지 않음)");
                    }
                } catch (Exception ex) {
                    log.error("HTTP 응답 본문 읽기 실패: {}", ex.getMessage());
                }
                
                // HTTP 응답 헤더 확인
                try {
                    org.springframework.http.HttpHeaders headers = httpEx.getResponseHeaders();
                    if (headers != null) {
                        log.error("HTTP 응답 헤더:");
                        headers.forEach((key, values) -> 
                            log.error("  {}: {}", key, values)
                        );
                    }
                } catch (Exception ex) {
                    log.warn("HTTP 응답 헤더 읽기 실패: {}", ex.getMessage());
                }
            }
            
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
            }
            
            // 401 에러의 가능한 원인 안내
            log.error("=== 401 에러 가능한 원인 ===");
            log.error("1. Client Secret이 잘못되었을 수 있음");
            log.error("   - 카카오 개발자 콘솔의 Client Secret과 application.properties의 client-secret이 일치하는지 확인");
            log.error("2. Redirect URI가 카카오 개발자 콘솔에 등록된 것과 정확히 일치하지 않을 수 있음");
            log.error("   - 등록된 URI: {}", authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getRedirectUri());
            log.error("   - 카카오 개발자 콘솔의 Redirect URI와 정확히 일치해야 함 (공백, 대소문자, 슬래시 포함)");
            log.error("3. Authorization Request의 redirect_uri와 Token Exchange의 redirect_uri가 다를 수 있음");
            log.error("4. Code가 이미 사용되었거나 만료되었을 수 있음 (카카오는 code 1회성 사용)");
            log.error("5. 카카오 개발자 콘솔에서 앱 상태가 비활성화되었을 수 있음");
            
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
            var authorizationRequest = authorizationExchange.getAuthorizationRequest();
            String authRequestRedirectUri = authorizationRequest.getRedirectUri();
            
            // Authorization Request와 Token Exchange의 redirect_uri 비교
            log.info("=== Redirect URI 비교 ===");
            log.info("Authorization Request redirect_uri: {}", authRequestRedirectUri);
            log.info("Token Exchange redirect_uri: {}", redirectUri);
            if (!redirectUri.equals(authRequestRedirectUri)) {
                log.error("❌ Redirect URI 불일치! Authorization Request와 Token Exchange의 redirect_uri가 다릅니다!");
                log.error("   이것이 401 에러의 원인일 수 있습니다.");
            } else {
                log.info("✅ Redirect URI 일치 확인");
            }
            
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
            log.info("   - 대소문자, 슬래시, 공백 모두 정확히 일치해야 함");
            log.info("2. Client Secret이 올바른지 확인:");
            log.info("   Client ID: {}", clientRegistration.getClientId());
            log.info("   Client Secret (앞 5자리): {}...", 
                    clientRegistration.getClientSecret() != null 
                            ? clientRegistration.getClientSecret().substring(0, Math.min(5, clientRegistration.getClientSecret().length())) 
                            : "null");
            log.info("3. Code가 이미 사용되었는지 확인 (카카오는 code 1회성 사용)");
            log.info("4. CORS 설정 확인:");
            log.info("   - /login/oauth2/** 경로는 .permitAll()로 허용됨 ✅");
            log.info("   - CORS는 모든 경로(/**)에 대해 허용됨 ✅");
            
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
    
    /**
     * Code 재사용 체크 및 경고
     */
    private void checkCodeReuse(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        
        LocalDateTime previousUseTime = usedCodes.get(code);
        if (previousUseTime != null) {
            long minutesSinceLastUse = ChronoUnit.MINUTES.between(previousUseTime, LocalDateTime.now());
            log.error("⚠️⚠️⚠️ CRITICAL: Authorization Code 재사용 감지! ⚠️⚠️⚠️");
            log.error("Code: {}...", code.substring(0, Math.min(20, code.length())));
            log.error("이전 사용 시간: {} ({}분 전)", previousUseTime, minutesSinceLastUse);
            log.error("현재 시간: {}", LocalDateTime.now());
            log.error("카카오는 authorization code를 1회성으로만 사용할 수 있습니다!");
            log.error("같은 code를 재사용하면 401 Unauthorized 에러가 발생합니다.");
            log.error("가능한 원인:");
            log.error("  1. 네트워크 오류로 인한 자동 재시도");
            log.error("  2. 브라우저 새로고침 또는 뒤로가기로 인한 중복 요청");
            log.error("  3. 프론트엔드에서 같은 code로 여러 번 요청");
            log.error("이 요청은 카카오에서 거부될 가능성이 높습니다.");
        }
    }
    
    /**
     * Code를 사용된 것으로 표시
     */
    private void markCodeAsUsed(String code) {
        if (code != null && !code.isBlank()) {
            LocalDateTime previousUseTime = usedCodes.put(code, LocalDateTime.now());
            if (previousUseTime != null) {
                log.warn("Code가 이미 사용된 적이 있습니다. 재사용 가능성이 있습니다.");
            } else {
                log.debug("Code를 사용된 것으로 표시: {}...", code.substring(0, Math.min(20, code.length())));
            }
        }
    }
}

