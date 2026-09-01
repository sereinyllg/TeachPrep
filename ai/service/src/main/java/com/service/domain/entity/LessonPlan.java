package com.service.domain.entity;


import com.baomidou.mybatisplus.annotation.*;
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
@TableName("lesson_plan")
public class LessonPlan implements Serializable {
    //教案id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    //用户id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userid;
    //教案名称
    private String title;
    //教案内容
    private String content;
    //是否删除（0-已删除，1-未删除）
    @TableLogic
    private Integer deleted;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    //更新时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
