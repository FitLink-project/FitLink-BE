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
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        try {
            if (redirectUri == null || redirectUri.isBlank()) {
                throw new IllegalStateException("프론트엔드 리다이렉트 URI가 설정되지 않았습니다.");
            }
            
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getName();
            Boolean needsEmailUpdate = oAuth2User.getAttribute("needsEmailUpdate");
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            email, null, authorities
                    );
            
            String accessToken = jwtTokenProvider.generateToken(authToken);
            
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", accessToken);
            
            if (Boolean.TRUE.equals(needsEmailUpdate)) {
                uriBuilder.queryParam("needsEmailUpdate", true);
                log.warn("OAuth2 로그인 성공 (임시 이메일): {}", email);
            } else {
                log.info("OAuth2 로그인 성공: {}", email);
            }
            
            String targetUrl = uriBuilder.build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류", e);
            
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

