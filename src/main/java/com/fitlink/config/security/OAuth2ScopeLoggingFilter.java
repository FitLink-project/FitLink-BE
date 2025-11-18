package com.fitlink.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * OAuth2 Authorization Request에서 요청되는 scope를 로깅하고 검증하는 필터
 */
@Slf4j
@Component
public class OAuth2ScopeLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // OAuth2 authorization endpoint 요청인지 확인
        if (requestURI != null && requestURI.startsWith("/oauth2/authorization/")) {
            String provider = requestURI.substring("/oauth2/authorization/".length());
            log.info("🔍 OAuth2 Authorization 요청 감지 - Provider: {}, URI: {}", provider, requestURI);
            log.info("📍 전체 요청 URL: {}?{}", request.getRequestURL(), 
                    request.getQueryString() != null ? request.getQueryString() : "(쿼리 없음)");
            
            // 쿼리 파라미터에서 scope 확인
            String scopeParam = request.getParameter("scope");
            if (scopeParam != null) {
                log.info("📋 요청된 scope 파라미터: {}", scopeParam);
                
                // account_email이 포함되어 있으면 경고
                if (scopeParam.contains("account_email")) {
                    log.error("❌ 오류 발견: account_email이 요청 scope에 포함되어 있습니다!");
                    log.error("요청 URI: {}", requestURI);
                    log.error("요청된 scope: {}", scopeParam);
                    log.error("카카오의 경우 account_email을 제거하고 profile_nickname만 사용해야 합니다!");
                }
            }
            
            // 모든 쿼리 파라미터 로깅
            if (request.getParameterMap().isEmpty()) {
                log.warn("⚠️ 쿼리 파라미터가 없습니다. 이는 정상적이지 않을 수 있습니다.");
            } else {
                request.getParameterMap().forEach((key, values) -> {
                    if (values.length > 0) {
                        log.info("요청 파라미터 - {}: {}", key, Arrays.toString(values));
                    }
                });
            }
            
            // Spring Security가 생성할 리다이렉트 URL 예상
            log.info("💡 Spring Security가 카카오로 리다이렉트할 예정입니다.");
            log.info("   카카오 인증 후 아래 URL로 콜백이 와야 합니다:");
            log.info("   {}/login/oauth2/code/{}", 
                    request.getScheme() + "://" + request.getServerName() + 
                    (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : ""),
                    provider);
        }
        
        // OAuth2 콜백 요청인지 확인
        if (requestURI != null && requestURI.startsWith("/login/oauth2/code/")) {
            String provider = requestURI.substring("/login/oauth2/code/".length());
            log.info("🔄 OAuth2 콜백 요청 감지 - Provider: {}", provider);
            log.info("📍 전체 콜백 URL: {}?{}", request.getRequestURL(),
                    request.getQueryString() != null ? request.getQueryString() : "(쿼리 없음)");
            
            String code = request.getParameter("code");
            String state = request.getParameter("state");
            String error = request.getParameter("error");
            
            if (code != null) {
                log.info("✅ authorization code 수신: {}", code.substring(0, Math.min(20, code.length())) + "...");
            } else {
                log.error("❌ authorization code가 없습니다!");
            }
            
            if (state != null) {
                log.info("✅ state 파라미터 수신: {}", state);
            } else {
                log.error("❌ state 파라미터가 없습니다!");
            }
            
            if (error != null) {
                log.error("❌ 카카오에서 에러 반환: error={}, error_description={}", 
                        error, request.getParameter("error_description"));
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
