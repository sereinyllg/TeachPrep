package com.service.domain.vo;

import com.service.domain.entity.LessonPlan;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class PageVO<LessonPlan> implements Serializable {
    private Long total;
    private List<LessonPlan> records;
}
