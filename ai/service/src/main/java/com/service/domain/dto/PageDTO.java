package com.service.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageDTO implements Serializable {
    private String userId;
    private String pageNum;
    private String pageSize;
}
