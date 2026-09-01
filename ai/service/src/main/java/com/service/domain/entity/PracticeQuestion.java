package com.service.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("practice_question")
public class PracticeQuestion {
    @JsonSerialize(using = ToStringSerializer.class)//Long转String,防止前端溢出
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userid;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lessonid;
    private String title;
    private String content;
    @TableLogic//逻辑删除
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)//插入时填充字段
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
