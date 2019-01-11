package com.lautumn.im.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午5:49 2019/1/11
 */
@Configuration
@ConfigurationProperties(prefix = "netty.websocket")
@Data
public class WebSocketProperties {

    private String path;

    private Integer port;


}
