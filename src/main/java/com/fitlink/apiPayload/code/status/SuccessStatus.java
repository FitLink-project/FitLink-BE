package com.fitlink.apiPayload.code.status;

import com.fitlink.apiPayload.code.BaseCode;
import com.fitlink.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // ?쇰컲?곸씤 ?묐떟
    _OK(HttpStatus.OK, "COMMON200", "?깃났?낅땲??"),
    // ?앹꽦 愿???묐떟
    _CREATED(HttpStatus.CREATED, "COMMON201", "?깃났?곸쑝濡??앹꽦(????섏뿀?듬땲??"),
    // ?뚯썝媛??愿???묐떟
    _NICKNAME_AVAILABLE(HttpStatus.ACCEPTED, "NICKNAME202", "?됰꽕??以묐났 ?뺤씤 ?깃났.")
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

