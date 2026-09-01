package com.common.exception;

//业务逻辑非法操作
public class BizIllegalException extends CommonException{

    public BizIllegalException(String message) {
        super(message, 500);
    }

    public BizIllegalException(String message, Throwable cause) {
        super(message, cause, 500);
    }

    public BizIllegalException(Throwable cause) {
        super(cause, 500);
    }
}
