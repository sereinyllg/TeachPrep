package com.common.context;

/**
 * 使用 ThreadLocal 实现了线程隔离的用户身份信息存储，
 * 适用于在请求处理过程中传递当前用户 ID
 */
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
