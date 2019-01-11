package com.lautumn.im.netty.config;

import com.lautumn.im.netty.WSServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午5:56 2019/1/11
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public WSServer wsServer(WebSocketProperties webSocketProperties) {

        WSServer wsServer = new WSServer();
        wsServer.setPath(webSocketProperties.getPath());
        wsServer.setPort(webSocketProperties.getPort());
        return wsServer;
    }
}
