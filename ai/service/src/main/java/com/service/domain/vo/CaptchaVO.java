package com.service.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CaptchaVO implements Serializable {
    //验证码
    private String imageData;
    //会话ID
    private String sessionId;
}
