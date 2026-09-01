package com.service.domain.dto;

import lombok.Data;

@Data
public class PracticeDTO {
    private String id;
    private String lessonId;
    private String userId;
    private String title;
    private String content;
}
