package com.service.controller;

import com.common.context.BaseContext;
import com.common.result.Result;
import com.service.domain.dto.PageDTO;
import com.service.domain.dto.PracticeDTO;
import com.service.service.IPracticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "练习题")
@RequestMapping("/practice")
@Slf4j
@RequiredArgsConstructor
public class PracticeController {

    private final IPracticeService practiceService;

    /**
     * 练习题生成
     */
    @PostMapping
    @ApiOperation(value = "练习题生成")
    public Result<?> genPractice(@RequestBody PracticeDTO practiceDTO) {
        log.info("practiceRequestDTO: {}", practiceDTO);
        return practiceService.generatePractice(practiceDTO);
    }

    /**
     * 保存练习题
     */
    @PostMapping("/save")
    @ApiOperation(value = "保存练习题")
    public Result<?> savePractice(@RequestBody PracticeDTO practiceDTO) {
        log.info("practiceRequestDTO: {}", practiceDTO);
        practiceService.save(practiceDTO);
        return Result.success("保存成功");
    }

    /**
     * 修改练习题
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改练习题")
    public Result<?> updatePractice(@RequestBody PracticeDTO practiceDTO) {
        log.info("practiceRequestDTO: {}", practiceDTO);
        boolean result = practiceService.update(practiceDTO);
        if (result) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }

    /**
     * 删除练习题
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除练习题")
    public Result<?> deletePractice(@RequestBody PracticeDTO practiceDTO) {
        String id = practiceDTO.getId();
        log.info("id: {}", id);
        boolean result = practiceService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 分页查询练习题
     */
    @GetMapping
    @ApiOperation(value = "分页查询练习题")
    public Result<?> listPractice(@RequestParam String id, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        log.info(" 分页查询所有练习题");
        PageDTO pageDTO = new PageDTO();
        //封装参数
        pageDTO.setUserId(String.valueOf(BaseContext.getCurrentId()));
        pageDTO.setPageNum(String.valueOf(pageNum));
        pageDTO.setPageSize(String.valueOf(pageSize));
        return Result.success(practiceService.getPracticePage(pageDTO));

    }
}
