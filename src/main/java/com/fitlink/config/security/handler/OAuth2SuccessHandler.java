package com.fitlink.config.security.handler;

import com.fitlink.config.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${oauth2.redirect.uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getName(); // CustomOAuth2User에서 email 반환
        
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
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

