package com.fitlink.validation.validator;

import com.fitlink.apiPayload.code.status.ErrorStatus;
import com.fitlink.apiPayload.exception.GeneralException;
import com.fitlink.domain.Users;
import com.fitlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 관련 검증 로직을 제공하는 클래스
 * 비즈니스 규칙에 따른 사용자 검증을 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    /**
     * 이메일 중복 여부를 검증합니다.
     * 이미 존재하는 이메일이면 예외를 발생시킵니다.
     *
     * @param email 검증할 이메일
     * @throws GeneralException 중복된 이메일인 경우
     */
    public void validateEmailNotDuplicate(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
                });
    }

    /**
     * 사용자 활성화 여부를 검증합니다.
     * 비활성화된 사용자이면 예외를 발생시킵니다.
     *
     * @param user 검증할 사용자
     * @throws GeneralException 비활성화된 사용자인 경우
     */
    public void validateUserActive(Users user) {
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new GeneralException(ErrorStatus._USER_INACTIVE);
        }
    }

    /**
     * 비밀번호 일치 여부를 검증합니다.
     * 비밀번호가 일치하지 않으면 예외를 발생시킵니다.
     *
     * @param rawPassword 원본 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @throws GeneralException 비밀번호가 일치하지 않는 경우
     */
    public void validatePasswordMatch(String rawPassword, String encodedPassword, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new GeneralException(ErrorStatus._LOGIN_FAILED);
        }
    }
}

