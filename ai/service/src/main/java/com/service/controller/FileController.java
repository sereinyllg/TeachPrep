package com.service.controller;

import com.common.result.Result;
import com.service.domain.dto.PageDTO;
import com.service.domain.vo.FileVO;
import com.service.service.IFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Api(tags = "文件")
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<FileVO> uploadFile(
            @RequestParam("id") String userId,
            @RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadFile(userId,file));
    }

    /**
     * 分页查询文件
     */
    @GetMapping
    @ApiOperation(value = "分页查询文件")
    public Result<?> list(@RequestParam String id,@RequestParam Integer pageNum,@RequestParam Integer pageSize) {
        log.info(" 分页查询所有教案");
        PageDTO page = new PageDTO();
        page.setUserId(id);
        page.setPageNum(String.valueOf(pageNum));
        page.setPageSize(String.valueOf(pageSize));
        return Result.success(fileService.getFilePage(page));
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    @ApiOperation(value = "删除文件")
    public Result<?> delete(@RequestParam String id) {
        log.info(" 删除文件");
        return Result.success(fileService.removeById(id));
    }
}
