package com.service.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatepwdDTO implements Serializable {
    private String id;
    private String oldPassword;
    private String newPassword;
}
