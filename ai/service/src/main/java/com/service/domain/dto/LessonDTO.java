package com.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LessonDTO implements Serializable {
    private String id;
    //用户id
    private String userId;
    //教案名称
    private String title;
    //教案内容
    private String content;
    @JsonProperty("text")
    public void setText(String text) {
        this.content = text;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }
}
