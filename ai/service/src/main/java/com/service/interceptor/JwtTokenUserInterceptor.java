package com.service.interceptor;

import com.common.context.BaseContext;
import com.common.properties.JwtProperties;
import com.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final RedisTemplate  redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Request URI: {}", request.getRequestURI());
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        /*String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.trim().isEmpty()) {
            log.warn("请求头中缺少 token，URI: {}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }
        try {
            log.info("jwt校验token: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("当前用户ID：{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.error("JWT 校验失败，Token: {}, 错误: {}", token, ex.getMessage(), ex);
            response.setStatus(401);
            return false;
        }*/
        //JWT 校验前使用 Redis 缓存 userId
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(401);
            return false;
        }

// 从 Redis 中获取 userId
        String redisKey = "jwt:user:" + token;
        String userIdStr = (String) redisTemplate.opsForValue().get(redisKey);

        if (userIdStr != null) {
            log.debug("JWT缓存命中: {}", redisKey);
            BaseContext.setCurrentId(Long.valueOf(userIdStr));
            return true;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("userId").toString());

            BaseContext.setCurrentId(userId);
            redisTemplate.opsForValue().set(redisKey, String.valueOf(userId), 2, TimeUnit.HOURS); // 缓存有效期等于 token TTL
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }

    }
}
