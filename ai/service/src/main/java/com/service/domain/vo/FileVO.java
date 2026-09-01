package com.service.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FileVO implements Serializable {
    private Long id;
    private Long userid;
    private String title;
    private String url;
    private Integer deleted;
    private LocalDateTime createTime;
}
