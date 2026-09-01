package com.service.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {
    // 用户id
    private String id;
    // 用户名
    private String userName;
    // 真实姓名
    private String realName;
    // 昵称
    private String nickName;
    // 手机号
    private String phone;
    // 邮箱
    private String email;
    // 性别
    private Integer sex;
    // token
    private String token;
}
