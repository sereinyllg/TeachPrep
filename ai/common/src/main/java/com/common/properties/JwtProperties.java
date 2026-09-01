package com.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.jwt")
public class JwtProperties {
    private String userSecretKey;
    private Long userTTL;
    private String userTokenName;
}
