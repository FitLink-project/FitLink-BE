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
        
        log.error("OAuth2 로그인 실패: {}", exception.getMessage());
        
        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth2Exception = 
                    (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception;
            org.springframework.security.oauth2.core.OAuth2Error oauth2Error = oauth2Exception.getError();
            if (oauth2Error != null) {
                log.error("OAuth2 에러 코드: {}", oauth2Error.getErrorCode());
            }
        }
        
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "oauth2_authentication_failed")
                .queryParam("message", exception.getMessage())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

