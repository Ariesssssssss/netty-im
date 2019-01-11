package com.lautumn.im;

import com.lautumn.im.netty.WSServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @Author: Lautumn
 * @Describe: 通过监听 spring boot 启动完成后执行netty 启动
 * @Date: Create in 下午5:32 2019/1/11
 */
@Component
@Slf4j
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private WSServer wsServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止重复执行
        if (event.getApplicationContext().getParent() == null) {
            log.info("启动netty");
            try {
                wsServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
