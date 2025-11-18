package com.fitlink.config.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${oauth2.redirect.uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        
        log.error("❌ OAuth2 로그인 실패 발생!");
        log.error("요청 URI: {}", request.getRequestURI());
        log.error("전체 요청 URL: {}?{}", request.getRequestURL(), request.getQueryString());
        log.error("에러 타입: {}", exception.getClass().getSimpleName());
        log.error("에러 메시지: {}", exception.getMessage());
        log.error("에러 원인: {}", exception.getCause() != null ? exception.getCause().getMessage() : "원인 없음");
        
        // OAuth2Error 정보 확인
        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth2Exception = 
                    (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception;
            org.springframework.security.oauth2.core.OAuth2Error oauth2Error = oauth2Exception.getError();
            if (oauth2Error != null) {
                log.error("OAuth2 에러 코드: {}", oauth2Error.getErrorCode());
                log.error("OAuth2 에러 설명: {}", oauth2Error.getDescription());
                log.error("OAuth2 에러 URI: {}", oauth2Error.getUri());
            }
        }
        
        // 전체 스택 트레이스 로깅 (디버깅용)
        log.error("전체 스택 트레이스:", exception);
        
        // 쿼리 파라미터 확인 (authorization code 등)
        log.info("📋 콜백 요청 쿼리 파라미터:");
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                String value = "code".equals(key) || "state".equals(key) 
                        ? values[0].substring(0, Math.min(30, values[0].length())) + "..." 
                        : values[0];
                log.info("  - {}: {}", key, value);
            }
        });
        
        // 쿼리 파라미터가 없거나 code가 없는 경우
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");
        
        // 모든 파라미터 확인
        log.info("📋 파라미터 확인 - code: {}, state: {}, error: {}, error_description: {}", 
                code != null ? "있음" : "없음",
                state != null ? "있음" : "없음",
                error != null ? error : "없음",
                errorDescription != null ? errorDescription : "없음");
        
        if (error != null) {
            log.error("⚠️ 카카오에서 에러 반환: error={}, error_description={}", error, errorDescription);
            log.error("카카오 OAuth2 에러 해결 방법:");
            log.error("  1. 카카오 개발자 콘솔에서 Redirect URI 확인: https://www.fitlink1207.store/login/oauth2/code/kakao");
            log.error("  2. 클라이언트 시크릿 확인");
            log.error("  3. 동의항목 설정 확인");
        }
        
        if (code == null || code.isBlank()) {
            log.error("❌ authorization code가 없습니다!");
            log.error("가능한 원인:");
            log.error("  1. 카카오가 Authorization Request를 거부했을 수 있음");
            log.error("  2. 브라우저가 직접 콜백 URL로 접근했을 수 있음 (정상적인 OAuth2 플로우가 아님)");
            log.error("  3. 카카오 개발자 콘솔에서 Redirect URI가 정확히 등록되지 않았을 수 있음");
            log.error("해결 방법: 카카오 로그인 버튼을 통해 정상적인 OAuth2 플로우를 시작하세요.");
        }
        
        if (state == null || state.isBlank()) {
            log.error("❌ state 파라미터가 없습니다! 이것이 invalid_request의 원인일 수 있습니다.");
            log.error("state 파라미터는 CSRF 보호를 위해 필요합니다.");
        }
        
        // 쿼리 스트링이 아예 없는 경우
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank() || "null".equals(queryString)) {
            log.error("❌ 쿼리 스트링이 없습니다! 이것은 브라우저가 직접 콜백 URL로 접근했거나,");
            log.error("   카카오가 파라미터 없이 리다이렉트했을 수 있습니다.");
            log.error("정상적인 OAuth2 플로우:");
            log.error("  1. 사용자가 /oauth2/authorization/kakao 접근");
            log.error("  2. 카카오 인증 페이지로 리다이렉트");
            log.error("  3. 사용자 인증 후 /login/oauth2/code/kakao?code=xxx&state=xxx 로 리다이렉트");
            log.error("현재 상황: 파라미터 없이 /login/oauth2/code/kakao 접근");
        }
        
        // 에러와 함께 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "oauth2_authentication_failed")
                .queryParam("message", exception.getMessage())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        
        log.info("프론트엔드로 에러 리다이렉트: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

