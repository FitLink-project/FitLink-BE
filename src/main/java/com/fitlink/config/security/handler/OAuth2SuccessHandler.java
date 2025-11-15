package com.fitlink.config.security.handler;

import com.fitlink.config.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String redirectUri;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider,
                                @Value("${oauth2.redirect.uri:http://localhost:3000/oauth2/redirect}") String redirectUri) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redirectUri = redirectUri;
        
        log.info("OAuth2SuccessHandler 초기화 - 프론트엔드 리다이렉트 URI: {}", this.redirectUri);
        
        // 기본 리다이렉트 동작 비활성화 (우리가 직접 제어)
        setAlwaysUseDefaultTargetUrl(false);
        
        // 리다이렉트 전략 명시적 설정
        setRedirectStrategy((request, response, url) -> {
            log.info("OAuth2 리다이렉트 실행: {} -> {}", request.getRequestURI(), url);
            response.sendRedirect(url);
        });
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        try {
            log.info("OAuth2 인증 성공 처리 시작: {}", request.getRequestURI());
            log.info("사용할 프론트엔드 리다이렉트 URI: {}", redirectUri);
            
            if (redirectUri == null || redirectUri.isBlank()) {
                log.error("프론트엔드 리다이렉트 URI가 설정되지 않았습니다!");
                throw new IllegalStateException("프론트엔드 리다이렉트 URI가 설정되지 않았습니다.");
            }
            
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getName(); // CustomOAuth2User에서 email 반환
            
            log.info("OAuth2 사용자 이메일: {}", email);
            
            // 카카오 이메일이 없는 경우 needsEmailUpdate 플래그 확인
            Boolean needsEmailUpdate = oAuth2User.getAttribute("needsEmailUpdate");
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            // JWT 토큰 생성
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );
            
            String accessToken = jwtTokenProvider.generateToken(authToken);
            log.info("JWT 토큰 생성 완료 (길이: {})", accessToken.length());
            
            // 리다이렉트 URL 생성 (프론트엔드로 토큰 전달)
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", accessToken);
            
            // 이메일 업데이트가 필요한 경우 플래그 추가
            if (Boolean.TRUE.equals(needsEmailUpdate)) {
                uriBuilder.queryParam("needsEmailUpdate", true);
                log.warn("OAuth2 로그인 성공 (임시 이메일): {}. 사용자가 이메일을 입력해야 합니다.", email);
            } else {
                log.info("OAuth2 로그인 성공: {}", email);
            }
            
            String targetUrl = uriBuilder.build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
            
            log.info("프론트엔드로 리다이렉트: {}", targetUrl);
            
            // 리다이렉트 실행
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", e);
            
            // 에러 발생 시에도 프론트엔드로 리다이렉트 (에러 정보 포함)
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "oauth2_processing_error")
                    .queryParam("message", "인증 처리 중 오류가 발생했습니다.")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
            
            response.sendRedirect(errorUrl);
        }
    }
}

