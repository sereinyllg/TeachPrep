package com.service.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.common.enums.UserStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    //用户id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    //用户名
    private String username;
    //密码
    private String password;
    //手机号
    private String phone;
    //真实名称
    private String realName;
    //昵称
    private String nickName;
    //邮箱
    private String email;
    //账户状态（1:正常 2:锁定）
    private UserStatus status;
    //性别（1:男 2:女）
    private Integer sex;
    //是否删除（0-已删除，1-未删除）
    @TableLogic
    private Integer deleted;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    //修改时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
