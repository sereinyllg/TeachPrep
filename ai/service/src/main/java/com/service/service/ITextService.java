package com.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.result.Result;
import com.service.domain.dto.LessonDTO;
import com.service.domain.dto.PageDTO;
import com.service.domain.entity.LessonPlan;

import javax.servlet.http.HttpServletResponse;

public interface ITextService extends IService<LessonPlan> {
    Object getLessonPage( PageDTO page);

    void generateLessonPlan(String prompt, HttpServletResponse response);

    void save(LessonDTO lessonDTO);

    void update(LessonDTO lessonDTO);

    Result genPlan(String prompt);
}
