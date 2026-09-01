package com.common.exception;

//专门用于处理数据库操作相关异常的自定义异常类
public class DbException extends CommonException{

    public DbException(String message) {
        super(message, 500);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause, 500);
    }

    public DbException(Throwable cause) {
        super(cause, 500);
    }
}
