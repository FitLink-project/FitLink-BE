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
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    log.info("요청 파라미터 - {}: {}", key, Arrays.toString(values));
                }
            });
        }
        
        filterChain.doFilter(request, response);
    }
}
