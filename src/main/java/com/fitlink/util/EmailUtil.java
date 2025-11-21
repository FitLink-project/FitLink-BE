package com.fitlink.util;

import org.springframework.stereotype.Component;

/**
 * 이메일 관련 유틸리티 클래스
 * 이메일 형식 검증 및 처리 로직을 제공합니다.
 */
@Component
public class EmailUtil {

    /**
     * 임시 카카오 이메일인지 확인합니다.
     * 형식: kakao_{externalId}@kakao.fitlink
     *
     * @param email 확인할 이메일
     * @return 임시 카카오 이메일이면 true, 아니면 false
     */
    public boolean isTemporaryKakaoEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches("kakao_\\d+@kakao\\.fitlink");
    }
}

