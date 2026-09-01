package com.service.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private String id;
    private String phone;
    private String email;
    private String nickName;
    private String realName;
    private Integer sex;
}
