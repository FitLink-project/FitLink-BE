package com.fitlink.util;

import com.fitlink.apiPayload.code.status.ErrorStatus;
import com.fitlink.apiPayload.exception.GeneralException;
import com.fitlink.domain.Users;
import com.fitlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 관련 유틸리티 클래스
 * 사용자 조회 및 확인 로직을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;

    /**
     * ID로 사용자를 조회합니다.
     * 사용자가 없으면 예외를 발생시킵니다.
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자
     * @throws GeneralException 사용자를 찾을 수 없는 경우
     */
    public Users findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자를 조회합니다.
     * 사용자가 없으면 예외를 발생시킵니다.
     *
     * @param email 이메일
     * @return 조회된 사용자
     * @throws GeneralException 사용자를 찾을 수 없는 경우
     */
    public Users findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자를 조회합니다.
     * 사용자가 없으면 null을 반환합니다.
     *
     * @param email 이메일
     * @return 조회된 사용자 또는 null
     */
    public Users findByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}

