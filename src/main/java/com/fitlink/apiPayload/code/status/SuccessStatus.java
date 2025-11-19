package com.fitlink.apiPayload.code.status;

import com.fitlink.apiPayload.code.BaseCode;
import com.fitlink.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 일반 응답
    _OK(HttpStatus.OK, "COMMON200", "성공했습니다."),
    // 생성 관련 응답
    _CREATED(HttpStatus.CREATED, "COMMON201", "성공적으로 생성(등록)되었습니다."),
    // 회원 관련 응답
    _NICKNAME_AVAILABLE(HttpStatus.ACCEPTED, "NICKNAME202", "닉네임이 중복되지 않아 사용 가능합니다.")
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}

