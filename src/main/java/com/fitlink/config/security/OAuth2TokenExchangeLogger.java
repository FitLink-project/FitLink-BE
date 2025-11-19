package com.fitlink.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegateForGoogle;
    private final RestTemplate restTemplate;
    
    // 사용된 authorization code 추적 (재사용 방지)
    // Key: authorization code, Value: 사용 시간
    private final ConcurrentHashMap<String, LocalDateTime> usedCodes = new ConcurrentHashMap<>();
    
    // 오래된 code 정리를 위한 스케줄러 (10분마다 실행)
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    public OAuth2TokenExchangeLogger() {
        // 카카오용 커스텀 RestTemplate 생성 (로깅용)
        this.restTemplate = new RestTemplate();
        this.restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(),
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter()
        ));
        
        // 실제 HTTP 요청 본문 로깅을 위한 인터셉터 추가 (에러 감지용)
        this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                // 카카오 토큰 요청인 경우에만 client_secret 포함 여부 확인
                if (request.getURI().toString().contains("kauth.kakao.com/oauth/token")) {
                    if (body != null && body.length > 0) {
                        String requestBody = new String(body, StandardCharsets.UTF_8);
                        // client_secret이 없으면 에러 로깅
                        if (!requestBody.contains("client_secret")) {
                            log.error("CRITICAL: client_secret 파라미터가 실제 HTTP 요청에 포함되지 않습니다. 이것이 KOE010 오류의 원인일 수 있습니다.");
                        }
                    }
                }
                
                return execution.execute(request, body);
            }
        });
        
        // 구글 등 다른 Provider는 기본 Spring Security 구현체 사용 (RestTemplate 주입하지 않음)
        // 기본 구현은 자체 RestTemplate을 사용하며, 모든 표준 MessageConverter가 포함되어 있음
        this.delegateForGoogle = new org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient();
        
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
        
        // 카카오인 경우 직접 토큰 요청을 수행하여 client_secret을 명시적으로 포함
        if ("kakao".equalsIgnoreCase(registrationId)) {
            // Code 재사용 체크
            checkCodeReuse(code);
            
            try {
                OAuth2AccessTokenResponse response = performKakaoTokenRequest(authorizationCodeGrantRequest);
                
                // 성공 시 code를 사용된 목록에 추가
                markCodeAsUsed(code);
                
                return response;
            } catch (RestClientException e) {
                // 실패 시에도 code를 사용된 것으로 표시 (카카오는 code를 무효화할 수 있음)
                if (code != null) {
                    markCodeAsUsed(code);
                }
                
                log.error("카카오 토큰 교환 실패: {}", e.getMessage());
                
                // HTTP 에러 응답 본문이 있으면 로깅
                if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                    org.springframework.web.client.HttpClientErrorException httpEx = 
                            (org.springframework.web.client.HttpClientErrorException) e;
                    log.error("HTTP 상태 코드: {}", httpEx.getStatusCode());
                    
                    try {
                        String responseBody = httpEx.getResponseBodyAsString();
                        if (responseBody != null && !responseBody.isEmpty()) {
                            log.error("HTTP 응답 본문: {}", responseBody);
                        }
                    } catch (Exception ex) {
                        log.error("HTTP 응답 본문 읽기 실패: {}", ex.getMessage());
                    }
                }
                
                throw e;
            } catch (Exception e) {
                log.error("카카오 토큰 교환 중 예외 발생: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        // 구글 등 다른 Provider는 기본 Spring Security 구현체 사용 (표준 MessageConverter 포함)
        try {
            OAuth2AccessTokenResponse response = delegateForGoogle.getTokenResponse(authorizationCodeGrantRequest);
            return response;
        } catch (Exception e) {
            log.error("OAuth2 토큰 교환 중 예외 발생 (Provider: {}): {}", registrationId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 카카오 토큰 요청을 직접 수행하여 client_secret을 명시적으로 포함
     * 카카오는 client_secret을 POST 본문에 포함해야 합니다.
     */
    private OAuth2AccessTokenResponse performKakaoTokenRequest(OAuth2AuthorizationCodeGrantRequest request) {
        var clientRegistration = request.getClientRegistration();
        var authorizationExchange = request.getAuthorizationExchange();
        var authorizationResponse = authorizationExchange.getAuthorizationResponse();
        
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        String clientId = clientRegistration.getClientId();
        String clientSecret = clientRegistration.getClientSecret();
        String code = authorizationResponse.getCode();
        String redirectUri = authorizationResponse.getRedirectUri();
        
        // 카카오 문서에 따른 토큰 요청 파라미터 (client_secret 포함)
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("grant_type", "authorization_code");
        formParameters.add("client_id", clientId);
        formParameters.add("redirect_uri", redirectUri);
        formParameters.add("code", code);
        
        // client_secret을 명시적으로 포함 (카카오 필수)
        if (clientSecret != null && !clientSecret.isBlank()) {
            formParameters.add("client_secret", clientSecret);
        } else {
            log.error("CRITICAL: client_secret이 null이거나 비어있습니다. 카카오 토큰 요청에 client_secret이 필요합니다.");
        }
        
        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        // RequestEntity 생성
        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(
                formParameters,
                headers,
                org.springframework.http.HttpMethod.POST,
                java.net.URI.create(tokenUri)
        );
        
        try {
            // RestTemplate을 사용하여 토큰 요청 수행 (Map으로 응답 받기)
            ResponseEntity<java.util.Map<String, Object>> responseEntity = 
                    restTemplate.exchange(
                            requestEntity,
                            new ParameterizedTypeReference<java.util.Map<String, Object>>() {}
                    );
            
            // 응답 Map을 OAuth2AccessTokenResponse로 변환
            java.util.Map<String, Object> responseMap = responseEntity.getBody();
            if (responseMap == null || !responseMap.containsKey("access_token")) {
                throw new IllegalStateException("카카오 토큰 응답이 유효하지 않습니다: " + responseMap);
            }
            
            // OAuth2AccessTokenResponse 생성
            OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse
                    .withToken((String) responseMap.get("access_token"))
                    .tokenType(OAuth2AccessToken.TokenType.BEARER);
            
            // expires_in 처리
            Object expiresInObj = responseMap.get("expires_in");
            if (expiresInObj != null) {
                if (expiresInObj instanceof Integer) {
                    builder.expiresIn((Integer) expiresInObj);
                } else if (expiresInObj instanceof String) {
                    builder.expiresIn(Integer.parseInt((String) expiresInObj));
                } else if (expiresInObj instanceof Number) {
                    builder.expiresIn(((Number) expiresInObj).intValue());
                }
            }
            
            // refresh_token 처리
            if (responseMap.containsKey("refresh_token")) {
                builder.refreshToken((String) responseMap.get("refresh_token"));
            }
            
            // scope 처리
            if (responseMap.containsKey("scope")) {
                Object scopeObj = responseMap.get("scope");
                if (scopeObj instanceof String) {
                    String scopeString = (String) scopeObj;
                    java.util.Set<String> scopes = new java.util.HashSet<>(java.util.Arrays.asList(scopeString.split(" ")));
                    builder.scopes(scopes);
                } else if (scopeObj instanceof java.util.Collection) {
                    java.util.Set<String> scopes = new java.util.HashSet<>((java.util.Collection<String>) scopeObj);
                    builder.scopes(scopes);
                }
            }
            
            // 추가 파라미터 처리 (기본 필드 제외)
            java.util.Map<String, Object> additionalParams = new java.util.HashMap<>();
            responseMap.forEach((key, value) -> {
                if (!key.equals("access_token") && 
                    !key.equals("token_type") && 
                    !key.equals("expires_in") && 
                    !key.equals("refresh_token") && 
                    !key.equals("scope")) {
                    additionalParams.put(key, value);
                }
            });
            
            if (!additionalParams.isEmpty()) {
                builder.additionalParameters(additionalParams);
            }
            
            OAuth2AccessTokenResponse response = builder.build();
            return response;
            
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("카카오 토큰 요청 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카카오 토큰 응답 파싱 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("카카오 토큰 응답 파싱 실패", e);
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
            usedCodes.put(code, LocalDateTime.now());
        }
    }
}

