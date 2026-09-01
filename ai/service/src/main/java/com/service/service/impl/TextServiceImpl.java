package com.service.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.result.Result;
import com.service.domain.dto.LessonDTO;
import com.service.domain.dto.PageDTO;
import com.service.domain.entity.LessonPlan;
import com.service.domain.vo.PageVO;
import com.service.mapper.TextMapper;
import com.service.service.ITextService;
import com.service.util.PageQueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextServiceImpl extends ServiceImpl<TextMapper, LessonPlan> implements ITextService {

    private final TextMapper textMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    //分页查询
    /*@Override
    public Object getLessonPage(PageDTO pageDTO) {
        log.info("分页参数: pageNum={}, pageSize={}, userId={}", pageDTO.getPageNum(), pageDTO.getPageSize(), pageDTO.getUserId());

        //创建分页对象
        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong)
                .orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong)
                .orElse(10L);

        //构造查询条件并查询
        Page<LessonPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LessonPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LessonPlan::getUserid, Long.valueOf(pageDTO.getUserId()))
                .eq(LessonPlan::getDeleted, 1);

        IPage<LessonPlan> result = textMapper.selectPage(page, queryWrapper);

        //返回分页结果
        return new PageVO<>(
                result.getTotal(),
                result.getRecords()
        );
    }*/
    //教案分页结果缓存，提高读取效率。
    /*@Override
    public Object getLessonPage(PageDTO pageDTO) {
        String cacheKey = "lessonPage:" + pageDTO.getUserId() + ":" + pageDTO.getPageNum() + ":" + pageDTO.getPageSize();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("命中缓存: {}", cacheKey);
            return cached;
        }

        long pageNum = Optional.ofNullable(pageDTO.getPageNum()).map(Long::parseLong).orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize()).map(Long::parseLong).orElse(10L);
        Page<LessonPlan> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LessonPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LessonPlan::getUserid, Long.valueOf(pageDTO.getUserId()))
                .eq(LessonPlan::getDeleted, 1);

        IPage<LessonPlan> result = textMapper.selectPage(page, queryWrapper);
        PageVO<LessonPlan> vo = new PageVO<>(result.getTotal(), result.getRecords());

        redisTemplate.opsForValue().set(cacheKey, vo, 5, TimeUnit.MINUTES); // 缓存5分钟
        return vo;
    }*/
    @Override
    public PageVO<LessonPlan> getLessonPage(PageDTO pageDTO) {
        return PageQueryUtils.getPageFromCache(
                pageDTO,
                "lessonPage",
                page -> {
                    LambdaQueryWrapper<LessonPlan> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(LessonPlan::getUserid, Long.valueOf(pageDTO.getUserId()))
                            .eq(LessonPlan::getDeleted, 1)
                            .orderByDesc(LessonPlan::getCreateTime);
                    return textMapper.selectPage(page, wrapper);
                }
        );
    }


    //流式生成教案
    @Override
    public void generateLessonPlan(String prompt, HttpServletResponse response) {
        //设置响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        //创建生成器
        try (PrintWriter out = response.getWriter()) {
            String apiKey = System.getenv("DASHSCOPE_API_KEY");
            if (apiKey == null) {
                out.write("data: " + wrapJsonError("系统未配置 API 密钥") + "\n\n");
                out.flush();
                return;
            }

            //生成课程目标、教学内容、课堂活动
            String[] sections = {"课程目标", "教学内容", "课堂活动"};
            Generation generation = new Generation();

            for (String section : sections) {
                GenerationParam param = GenerationParam.builder()
                        .apiKey(apiKey)
                        .model("qwen-plus")
                        .prompt(prompt + "\n请生成教案部分：" + section)
                        .build();

                GenerationResult result = generation.call(param);

                //处理输出
                if (result != null && result.getOutput() != null) {
                    String[] chunks = result.getOutput().getText().split("(?<=，|。|！|；)");
                    for (String chunk : chunks) {
                        out.write("data: " + wrapJson(chunk) + "\n\n");
                        out.flush();
                        Thread.sleep(100);
                    }
                } else {
                    out.write("data: " + wrapJson("教案生成失败：输出为空") + "\n\n");
                    out.flush();
                }
            }

        } catch (Exception e) {
            try {
                response.getWriter().write("data: " + wrapJsonError("系统异常：" + e.getMessage()) + "\n\n");
                response.getWriter().flush();
            } catch (IOException ex) {
                log.error("写入流式响应失败", ex);
            }
        }
    }

    //保存教案
    @Override
    public void save(LessonDTO lessonDTO) {
        //将DTO转换成Entity
        LessonPlan lessonPlan = new LessonPlan();
        BeanUtils.copyProperties(lessonDTO, lessonPlan);
        // 手动类型转换：String → Long
        lessonPlan.setUserid(Long.valueOf(lessonDTO.getUserId()));
        lessonPlan.setContent(lessonDTO.getContent()); // ⬅️ 手动映射 text → content
        PageQueryUtils.clearPageCache("lessonPage",Long.valueOf(lessonDTO.getUserId()));
        save(lessonPlan);
    }

    //修改教案
    @Override
    public void update(LessonDTO lessonDTO) {
        //将DTO转换成Entity
        LessonPlan lessonPlan = new LessonPlan();
        BeanUtils.copyProperties(lessonDTO, lessonPlan);
        // 手动类型转换：String → Long
        lessonPlan.setUserid(Long.valueOf(lessonDTO.getUserId()));
        lessonPlan.setContent(lessonDTO.getContent());
        lessonPlan.setId(Long.valueOf(lessonDTO.getId()));
        PageQueryUtils.clearPageCache("lessonPage",Long.valueOf(lessonDTO.getUserId()));
        updateById(lessonPlan);
    }

    //生成教案
    @Override
    public Result genPlan(String content) {
        // 调用API生成练习题
        String prompt = String.format("请根据要求提示生成教案，提示要求如下：\n%s", content);

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

    // 正常内容封装
    private String wrapJson(String content) {
        return String.format("{\"code\":1,\"msg\":null,\"data\":\"%s\"}", content.replace("\"", "\\\""));
    }

    // 错误内容封装
    private String wrapJsonError(String errorMsg) {
        return String.format("{\"code\":0,\"msg\":\"%s\",\"data\":null}", errorMsg.replace("\"", "\\\""));
    }
}
