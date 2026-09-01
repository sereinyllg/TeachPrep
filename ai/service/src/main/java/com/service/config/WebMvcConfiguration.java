package com.service.config;

import com.service.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册一个 JWT 拦截器 JwtTokenUserInterceptor
 * 实现对指定接口的请求进行拦截处理。
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {  // 改为实现接口，而非继承类

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**", "/api/**","/text/**","/practice/**")      // 扩展拦截路径
                .excludePathPatterns("/user/login", "/user/captcha",  "/user/register"); // 排除登录和验证码接口
    }
}