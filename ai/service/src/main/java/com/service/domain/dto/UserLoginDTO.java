package com.service.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {
    // 用户名
    private String username;
    // 密码
    private String password;
    // 验证码
    private String code;
    // 验证码sessionId
    private Long sessionId;
}
