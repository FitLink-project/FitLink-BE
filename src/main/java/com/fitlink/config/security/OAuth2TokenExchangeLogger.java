package com.fitlink.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OAuth2 토큰 교환 요청을 로깅하는 필터
 */
@Slf4j
@Component
public class OAuth2TokenExchangeLogger extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // OAuth2 콜백 엔드포인트인지 확인
        if (requestURI != null && requestURI.startsWith("/login/oauth2/code/")) {
            String provider = requestURI.substring("/login/oauth2/code/".length());
            log.info("🔄 OAuth2 토큰 교환 요청 감지 - Provider: {}, URI: {}", provider, requestURI);
            
            // 모든 쿼리 파라미터 로깅
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.isEmpty()) {
                log.info("📋 토큰 교환 요청 파라미터:");
                parameterMap.forEach((key, values) -> {
                    if (values.length > 0) {
                        String value = "code".equals(key) || "state".equals(key) 
                                ? values[0].substring(0, Math.min(20, values[0].length())) + "..." 
                                : values[0];
                        log.info("  - {}: {}", key, value);
                    }
                });
            }
            
            // 요청 헤더 확인
            String authorization = request.getHeader("Authorization");
            if (authorization != null) {
                log.info("📋 Authorization 헤더: {}", authorization.substring(0, Math.min(20, authorization.length())) + "...");
            }
            
            // Content-Type 확인
            String contentType = request.getContentType();
            if (contentType != null) {
                log.info("📋 Content-Type: {}", contentType);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
