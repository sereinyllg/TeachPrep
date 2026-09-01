package com.service;

import com.common.properties.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

//@SpringBootApplication
//@EnableConfigurationProperties(JwtProperties.class)
//@MapperScan("com.service.mapper")
//public class ServiceApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(ServiceApplication.class, args);
//    }
//
//}

@SpringBootApplication(scanBasePackages = {"com.service", "com.common"})
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan("com.service.mapper")
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
