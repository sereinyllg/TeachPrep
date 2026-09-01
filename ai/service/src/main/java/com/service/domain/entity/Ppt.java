package com.service.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
public class Ppt implements Serializable {
    // 主键
    private Long id;
    // 用户id
    private Long userId;
    // 封面图地址
    private String coverImgSrc;
    // 标题
    private String title;
    // 副标题
    private String subTitle;
    //PPT查询id
    private String sid;
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
