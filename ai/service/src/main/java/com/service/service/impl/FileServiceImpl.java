package com.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.service.domain.dto.PageDTO;
import com.service.domain.entity.File;
import com.service.domain.vo.FileVO;
import com.service.mapper.FileMapper;
import com.service.service.IFileService;
import com.service.util.PageQueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {
    @Autowired
    private ExecutorService fileExecutor;
    private final FileMapper fileMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String uploadPath = "D:/upload/";

    //  分页查询文件
    /*@Override
    public Object getFilePage(PageDTO pageDTO) {
        log.info("分页参数: pageNum={}, pageSize={}, userId={}", pageDTO.getPageNum(), pageDTO.getPageSize(), pageDTO.getUserId());

        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong)
                .orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong)
                .orElse(10L);

        Page<File> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUserid, Long.valueOf(pageDTO.getUserId()))
                .eq(File::getDeleted, 1);

        IPage<File> result = fileMapper.selectPage(page, queryWrapper);

        return new PageVO<>(
                result.getTotal(),
                result.getRecords()
        );
    }*/
    /*@Override
    public Object getFilePage(PageDTO pageDTO) {
        // 1. 构建缓存key（包含用户ID、页码、每页大小）
        String cacheKey = "filePage:" + pageDTO.getUserId()  + ":" + pageDTO.getPageNum()  + ":" + pageDTO.getPageSize();

        // 2. 尝试从缓存获取
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug(" 命中文件分页缓存: {}", cacheKey);
            return cached;
        }

        // 3. 缓存未命中，执行数据库查询
        log.info(" 分页参数: pageNum={}, pageSize={}, userId={}", pageDTO.getPageNum(),  pageDTO.getPageSize(),  pageDTO.getUserId());

        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong)
                .orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong)
                .orElse(10L);

        Page<File> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUserid,  Long.valueOf(pageDTO.getUserId()))
                .eq(File::getDeleted, 1)
                .orderByDesc(File::getCreateTime); // 增加默认排序

        IPage<File> result = fileMapper.selectPage(page,  queryWrapper);
        PageVO<File> vo = new PageVO<>(result.getTotal(),  result.getRecords());

        // 4. 将结果存入缓存（设置5分钟过期时间）
        redisTemplate.opsForValue().set(
                cacheKey,
                vo,
                5,
                TimeUnit.MINUTES
        );

        // 5. 记录缓存设置日志
        log.debug(" 设置文件分页缓存: {}", cacheKey);

        return vo;
    }*/
    @Override
    public Object getFilePage(PageDTO pageDTO) {
        return PageQueryUtils.getPageFromCache(
                pageDTO,
                "filePage",
                (Page<File> page) -> {
                    LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(File::getUserid, Long.valueOf(pageDTO.getUserId()))
                            .eq(File::getDeleted, 1)
                            .orderByDesc(File::getCreateTime);
                    return fileMapper.selectPage(page, wrapper);
                }
        );

    }

    // 上传文件
   /* @Override
    public FileVO uploadFile(String userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        try {
            //  生成新的文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = "test" + System.currentTimeMillis() + suffix;
            // 保存文件到服务器
            java.io.File dest = new java.io.File(uploadPath + newFileName);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            // 构建实体类用于插入数据库
            File fileEntity = new File();
            fileEntity.setId(IdUtil.getSnowflakeNextId());
            fileEntity.setUserid(Long.parseLong(userId));
            fileEntity.setTitle(originalFilename);
            fileEntity.setUrl("http://localhost:8080/uploads/" + newFileName);
            fileEntity.setDeleted(1);

            fileMapper.insert(fileEntity);

            // 构建返回的 VO 对象
            FileVO fileInfo = new FileVO();
            // 可以使用 BeanUtils.copyProperties 快速复制相同字段
            org.springframework.beans.BeanUtils.copyProperties(fileInfo, fileEntity);
            fileInfo.setCreateTime(System.currentTimeMillis());
            return fileInfo;

        } catch (IOException e) {
            throw new RuntimeException("上传失败：" + e.getMessage());
        }
    }*/
    //使用异步方式处理文件上传
    @Override
    public FileVO uploadFile(String userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = "file_" + IdUtil.getSnowflakeNextId() + suffix;
        String fileUrl = "http://localhost:8080/uploads/" + newFileName;
        String fullPath = uploadPath + newFileName;

        Long id = IdUtil.getSnowflakeNextId();

        fileExecutor.submit(() -> {
            try {
                java.io.File dest = new java.io.File(fullPath);
                dest.getParentFile().mkdirs();
                file.transferTo(dest);

                File fileEntity = new File();
                fileEntity.setId(id);
                fileEntity.setUserid(Long.parseLong(userId));
                fileEntity.setTitle(originalFilename);
                fileEntity.setUrl(fileUrl);
                fileEntity.setDeleted(1);
                fileMapper.insert(fileEntity);
            } catch (IOException e) {
                log.error("异步上传失败", e);
            }
        });

        FileVO vo = new FileVO();
        vo.setId(id);
        vo.setUserid(Long.parseLong(userId));
        vo.setTitle(originalFilename);
        vo.setUrl(fileUrl);
        vo.setDeleted(1);
        vo.setCreateTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis()),
                ZoneId.systemDefault()
        ));
        return vo;
    }

}
