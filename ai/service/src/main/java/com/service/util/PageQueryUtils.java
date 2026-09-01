package com.service.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.context.ApplicationContextHolder;
import com.service.domain.dto.PageDTO;
import com.service.domain.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

//@Slf4j
//public class PageQueryUtils {
//    private static final RedisTemplate<String, Object> redisTemplate =
//            ApplicationContextHolder.getBean(RedisTemplate.class);
//
//
//
//    // 从缓存中获取分页数据，如果没有则查询数据库并缓存结果
//    public static <T> PageVO<T> getPageFromCache(
//            PageDTO pageDTO,
//            String cachePrefix,
//            Function<Page<T>, IPage<T>> dbQueryFunction
//    ) {
//        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
//                .map(Long::parseLong)
//                .orElse(1L);
//        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
//                .map(Long::parseLong)
//                .orElse(10L);
//        String cacheKey = cachePrefix + ":" + pageDTO.getUserId()  + ":" + pageNum + ":" + pageSize;
//
//        Object cached = redisTemplate.opsForValue().get(cacheKey);
//        if (cached instanceof PageVO) {
//            log.debug(" 命中分页缓存: {}", cacheKey);
//            return (PageVO<T>) cached;
//        }
//
//        log.info(" 分页查询 - 用户ID: {}, 页码: {}, 每页大小: {}",
//                pageDTO.getUserId(),  pageNum, pageSize);
//
//        Page<T> page = new Page<>(pageNum, pageSize);
//        IPage<T> result = dbQueryFunction.apply(page);
//        PageVO<T> vo = new PageVO<>(result.getTotal(),  result.getRecords());
//
//        redisTemplate.opsForValue().set(cacheKey,  vo, 5, TimeUnit.MINUTES);
//        log.debug(" 设置分页缓存: {}", cacheKey);
//
//        return vo;
//    }
//
//    //  清除缓存
//    public static void clearPageCache(String prefix, Long userId) {
//        String pattern = String.format("%s:%d:*", prefix, userId);
//        Set<String> keys = redisTemplate.keys(pattern);
//        if (keys != null && !keys.isEmpty()) {
//            redisTemplate.delete(keys);
//            log.debug("清除缓存：prefix={} userId={} keys={}", prefix, userId, keys);
//        }
//    }
//
//}

@Slf4j
public class PageQueryUtils {

    private static RedisTemplate<String, Object> getRedisTemplate() {
        return ApplicationContextHolder.getBean(RedisTemplate.class);
    }

    public static <T> PageVO<T> getPageFromCache(
            PageDTO pageDTO,
            String cachePrefix,
            Function<Page<T>, IPage<T>> dbQueryFunction
    ) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(); // 每次调用时取

        long pageNum = Optional.ofNullable(pageDTO.getPageNum())
                .map(Long::parseLong).orElse(1L);
        long pageSize = Optional.ofNullable(pageDTO.getPageSize())
                .map(Long::parseLong).orElse(10L);
        String cacheKey = cachePrefix + ":" + pageDTO.getUserId()  + ":" + pageNum + ":" + pageSize;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageVO) {
            log.debug(" 命中分页缓存: {}", cacheKey);
            return (PageVO<T>) cached;
        }

        log.info(" 分页查询 - 用户ID: {}, 页码: {}, 每页大小: {}", pageDTO.getUserId(), pageNum, pageSize);

        Page<T> page = new Page<>(pageNum, pageSize);
        IPage<T> result = dbQueryFunction.apply(page);
        PageVO<T> vo = new PageVO<>(result.getTotal(), result.getRecords());

        redisTemplate.opsForValue().set(cacheKey, vo, 5, TimeUnit.MINUTES);
        log.debug(" 设置分页缓存: {}", cacheKey);

        return vo;
    }

    public static void clearPageCache(String prefix, Long userId) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate(); // 延迟获取
        String pattern = String.format("%s:%d:*", prefix, userId);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("清除缓存：prefix={} userId={} keys={}", prefix, userId, keys);
        }
    }
}
