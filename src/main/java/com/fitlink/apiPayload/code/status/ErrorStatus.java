package com.fitlink.apiPayload.code.status;

import com.fitlink.apiPayload.code.BaseErrorCode;
import com.fitlink.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반 에러 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "권한이 없습니다."),
    _INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON4011", "잘못된 토큰입니다."),
    _INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "COMMON4012", "잘못된 비밀번호입니다."),

    // 로그인 및 회원 관련 에러
    _BAD_EMAIL_FORMAT(HttpStatus.CONFLICT, "USER4001", "올바른 이메일 형식이 아닙니다."),
    _BAD_PASSWORD_FORMAT(HttpStatus.CONFLICT, "USER4002", "올바른 비밀번호 형식이 아닙니다."),
    _DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER4031", "중복된 이메일입니다."),
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "사용자를 찾을 수 없습니다."),

    // 프로그램 관련 에러
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}

