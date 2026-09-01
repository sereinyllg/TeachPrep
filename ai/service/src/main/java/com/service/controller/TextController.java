package com.service.controller;


import com.common.context.BaseContext;
import com.common.result.Result;
import com.service.domain.dto.LessonDTO;
import com.service.domain.dto.PageDTO;
import com.service.domain.entity.LessonPlan;
import com.service.service.ITextService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@Api(tags = "教案")
@RequestMapping("/text")
@Slf4j
@RequiredArgsConstructor
public class TextController {
    private final ITextService textService;

    /**
     * 教案生成
     */
    @PostMapping
    @ApiOperation(value = "教案生成")
    public Result text(@RequestBody String prompt) {
        log.info(" 用户输入的文本指令: {}", prompt);
        return textService.genPlan(prompt);
    }

    /**
     * 生成word
     */
    //TODO: 待优化，前端需要传一个参数，生成word
    @PostMapping("/genword")
    @ApiOperation(value = "生成word")
    public Result genword() {
        log.info(" 生成word");
        return Result.success();
    }

    /**
     * 保存教案
     */
    @PostMapping("/lesson")
    @ApiOperation(value = "保存教案")
    public Result lesson(@RequestBody LessonDTO lessonDTO) {
        lessonDTO.setUserId(String.valueOf(BaseContext.getCurrentId()));
        log.info(" 保存教案:{}",lessonDTO);
        textService.save(lessonDTO);
        return Result.success("保存成功");
    }

    /**
     * 删除教案
     */
    @DeleteMapping("/lesson")
    @ApiOperation(value = "删除教案")
    public Result<?> deleteLesson(@RequestBody LessonDTO lessonDTO) {
        Long id = Long.valueOf(lessonDTO.getId());
        log.info(" 删除教案 lessonId:{}",id);
        textService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询所有教案
     */
    @PostMapping("/lessonlist")
    @ApiOperation(value = "分页查询所有教案")
    public Result<?> lessonList(@RequestBody PageDTO page) {
        log.info(" 分页查询所有教案");
        page.setUserId(String.valueOf(BaseContext.getCurrentId()));
        return Result.success(textService.getLessonPage(page));
    }

    /**
     * 修改教案
     */
    @PutMapping("/lesson")
    @ApiOperation(value = "修改教案")
    public Result<?> updateLesson(@RequestBody LessonDTO lessonDTO) {
        lessonDTO.setUserId(String.valueOf(BaseContext.getCurrentId()));
        log.info(" 修改教案:{}",lessonDTO);
        textService.update(lessonDTO);
        return Result.success("修改成功");
    }

    /**
     * 根据id查询教案
     */
    @GetMapping("/getById")
    @ApiOperation(value = "根据id查询教案")
    public Result getById(@RequestParam String id) {
        log.info(" 根据id查询教案id:{}",id);
        LessonPlan lessonPlan = textService.getById(id);
        return Result.success(lessonPlan);
    }

    /**
     * 教案生成，流式响应
     */
    @PostMapping("/stream")
    @ApiOperation(value = "教案生成，流式响应")
    public void stream(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String prompt = body.get("prompt");
        log.info("用户输入的教案指令: {}", prompt);
        textService.generateLessonPlan(prompt, response);
    }
}
