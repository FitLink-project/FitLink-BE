package com.fitlink.apiPayload.exception.handler;

import com.fitlink.apiPayload.code.BaseErrorCode;
import com.fitlink.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
