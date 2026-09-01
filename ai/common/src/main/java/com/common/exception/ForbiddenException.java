package com.common.exception;

//专门用于处理 权限拒绝（HTTP 403 Forbidden）场景 的自定义异常类
public class ForbiddenException extends CommonException{

    public ForbiddenException(String message) {
        super(message, 403);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, 403);
    }

    public ForbiddenException(Throwable cause) {
        super(cause, 403);
    }
}
