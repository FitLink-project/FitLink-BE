package com.fitlink.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fitlink.apiPayload.code.BaseCode;
import com.fitlink.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;


    // ?깃났??寃쎌슦 ?묐떟 ?앹꽦
    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus._OK.getCode() , SuccessStatus._OK.getMessage(), result);
    }

    public static <T> ApiResponse<T> of(BaseCode code, T result){
        return new ApiResponse<>(true, code.getReasonHttpStatus().getCode() , code.getReasonHttpStatus().getMessage(), result);
    }
    public static <T> ApiResponse<T> onSuccess(String message, T result){
        return new ApiResponse<>(true, SuccessStatus._OK.getCode(), message, result);
    } //?깃났??寃쎌슦??"?깃났?낅땲?? 留먭퀬 ?ㅻⅨ 硫붿떆吏 ?ｋ뒗 硫붿꽌??


    // ?ㅽ뙣??寃쎌슦 ?묐떟 ?앹꽦
    public static <T> ApiResponse<T> onFailure(String code, String message, T data){
        return new ApiResponse<>(false, code, message, data);
    }
}

