package com.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.result.Result;
import com.service.domain.dto.PageDTO;
import com.service.domain.dto.PracticeDTO;
import com.service.domain.entity.PracticeQuestion;

public interface IPracticeService extends IService<PracticeQuestion> {
    Result<?> generatePractice(PracticeDTO practiceDTO);

    void save(PracticeDTO practiceDTO);

    boolean update(PracticeDTO practiceDTO);

    Object getPracticePage(PageDTO page);
}
