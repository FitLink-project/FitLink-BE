package com.fitlink.apiPayload.code.status;

import com.fitlink.apiPayload.code.BaseErrorCode;
import com.fitlink.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 媛???쇰컲?곸씤 ?묐떟
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "?쒕쾭 ?먮윭, 愿由ъ옄?먭쾶 臾몄쓽 諛붾엻?덈떎."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","?섎せ???붿껌?낅땲??"),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","?몄쬆???꾩슂?⑸땲??"),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "湲덉????붿껌?낅땲??"),
    _INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON4011", "?섎せ???좏겙?낅땲??"),
    _INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "COMMON4012", "?섎せ??鍮꾨?踰덊샇?낅땲??"),

    // 濡쒓렇???뚯썝媛???먮윭
    _DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "COMMON403", "以묐났???됰꽕?꾩엯?덈떎."),
    _DUPLICATE_JOIN_REQUEST(HttpStatus.CONFLICT, "COMMON403", "以묐났???대찓?쇱엯?덈떎."),
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "?ъ슜?먮? 李얠쓣 ???놁뒿?덈떎."),
    _PURPOSE_NOT_PROVIDED(HttpStatus.NOT_FOUND, "COMMON404", "紐⑹쟻???좏깮?댁빞?⑸땲??"),
    _INTEREST_NOT_PROVIDED(HttpStatus.NOT_FOUND, "COMMON404", "愿??遺꾩빞瑜??좏깮?댁빞?⑸땲??")

    //留곹걧 愿???먮윭
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

