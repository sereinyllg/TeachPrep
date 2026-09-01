package com.service.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.result.Result;
import com.service.domain.dto.PageDTO;
import com.service.domain.dto.PracticeDTO;
import com.service.domain.entity.LessonPlan;
import com.service.domain.entity.PracticeQuestion;
import com.service.domain.vo.PageVO;
import com.service.mapper.PracticeMapper;
import com.service.service.IPracticeService;
import com.service.service.ITextService;
import com.service.util.PageQueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeServiceImpl extends ServiceImpl<PracticeMapper, PracticeQuestion> implements IPracticeService {
    private final ITextService textService; // ✅ 注入教案服务
    private final PracticeMapper practiceMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    //  生成练习题
    @Override
    public Result<?> generatePractice(PracticeDTO dto) {
        // 检查教案是否存在和用户ID是否匹配
        LessonPlan lessonPlan = textService.getById(dto.getLessonId());
        if (lessonPlan == null || !dto.getUserId().equals(String.valueOf(lessonPlan.getUserid()))) {
            return Result.error("教案不存在或用户ID不匹配");
        }

        String content = lessonPlan.getContent();
        if (content == null || content.trim().isEmpty()) {
            return Result.error("教案内容为空");
        }

        // 调用API生成练习题
        String prompt = String.format("请根据以下教案内容设计5道语文练习题，题型包括选择题、填空题、简答题，格式为 markdown。\n\n教案内容如下：\n%s", content);

        try {
            Generation generation = new Generation();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen-plus")
                    .prompt(prompt)
                    .build();

            GenerationResult result = generation.call(param);
            if (result != null && result.getOutput() != null) {
                return Result.success(result.getOutput().getText());
            } else {
                return Result.error("生成失败：无输出结果");
            }
        } catch (Exception e) {
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    // 保存练习题
    @Override
    public void save(PracticeDTO practiceDTO) {
        //将DTO转换成Entity
        PracticeQuestion practiceQuestion = new PracticeQuestion();
        BeanUtils.copyProperties(practiceDTO, practiceQuestion);
        // 手动类型转换：String → Long
        practiceQuestion.setUserid(Long.valueOf(practiceDTO.getUserId()));
        practiceQuestion.setLessonid(Long.valueOf(practiceDTO.getLessonId()));
        PageQueryUtils.clearPageCache("practice:page",Long.valueOf(practiceDTO.getUserId()));
        save(practiceQuestion);
    }

    // 更新练习题
    @Override
    public boolean update(PracticeDTO practiceDTO) {
        //将DTO转换成Entity
        PracticeQuestion practiceQuestion = new PracticeQuestion();
        BeanUtils.copyProperties(practiceDTO, practiceQuestion);
        // 手动类型转换：String → Long
        practiceQuestion.setId(Long.valueOf(practiceDTO.getId()));
        practiceQuestion.setUserid(Long.valueOf(practiceDTO.getUserId()));
        practiceQuestion.setLessonid(Long.valueOf(practiceDTO.getLessonId()));
        PageQueryUtils.clearPageCache("practice:page",Long.valueOf(practiceDTO.getUserId()));
        return updateById(practiceQuestion);
    }

    // 获取练习题分页
    /*@Override
    public Object getPracticePage(PageDTO pageDTO) {
        log.info("分页参数: pageNum={}, pageSize={}, userId={}", pageDTO.getPageNum(), pageDTO.getPageSize(), pageDTO.getUserId());

        // 分页参数处理
        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong)
                .orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong)
                .orElse(10L);

        //  构建查询条件并查询
        Page<PracticeQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PracticeQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PracticeQuestion::getUserid, Long.valueOf(pageDTO.getUserId()))
                .eq(PracticeQuestion::getDeleted, 1);

        IPage<PracticeQuestion> result = practiceMapper.selectPage(page, queryWrapper);

        // 返回分页结果
        return new PageVO<>(
                result.getTotal(),
                result.getRecords()
        );
    }*/
    /*@Override
    public Object getPracticePage(PageDTO pageDTO) {
        // 1. 构建缓存Key（用户ID+分页参数）
        String cacheKey = String.format("practice:page:%s:%s:%s",
                pageDTO.getUserId(),
                pageDTO.getPageNum(),
                pageDTO.getPageSize());

        // 2. 尝试从缓存获取
        Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            log.debug(" 命中练习题分页缓存，key: {}", cacheKey);
            return cachedResult;
        }

        // 3. 记录查询日志（原始info日志保留）
        log.info(" 练习题分页查询 - 用户ID: {}, 页码: {}, 每页大小: {}",
                pageDTO.getUserId(),
                pageDTO.getPageNum(),
                pageDTO.getPageSize());

        // 4. 处理分页参数（使用Optional避免NPE）
        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong)
                .orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong)
                .orElse(10L);

        // 5. 构建查询条件（增加默认排序）
        Page<PracticeQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PracticeQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PracticeQuestion::getUserid,  Long.valueOf(pageDTO.getUserId()))
                .eq(PracticeQuestion::getDeleted, 1)
                .orderByDesc(PracticeQuestion::getCreateTime); // 新增默认排序

        // 6. 执行分页查询
        IPage<PracticeQuestion> result = practiceMapper.selectPage(page,  queryWrapper);

        // 7. 构建返回VO
        PageVO<PracticeQuestion> pageVO = new PageVO<>(result.getTotal(),  result.getRecords());

        // 8. 设置缓存（5分钟过期）
        redisTemplate.opsForValue().set(
                cacheKey,
                pageVO,
                5,
                TimeUnit.MINUTES
        );
        log.debug(" 练习题分页结果已缓存，key: {}", cacheKey);

        return pageVO;
    }*/
    @Override
    public PageVO<PracticeQuestion> getPracticePage(PageDTO pageDTO) {
        return PageQueryUtils.getPageFromCache(
                pageDTO,
                "practicePage",
                page -> {
                    LambdaQueryWrapper<PracticeQuestion> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(PracticeQuestion::getUserid, Long.valueOf(pageDTO.getUserId()))
                            .eq(PracticeQuestion::getDeleted, 1)
                            .orderByDesc(PracticeQuestion::getCreateTime);
                    return practiceMapper.selectPage(page, wrapper);
                }
        );
    }
}
