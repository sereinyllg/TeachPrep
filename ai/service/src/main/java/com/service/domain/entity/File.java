package com.service.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
public class File implements Serializable {
    // 主键
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    // 用户id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userid;
    // 文件名
    private String title;
    // 文件地址
    private String url;
    //是否删除（0-已删除，1-未删除）
    @TableLogic
    private Integer deleted;
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
