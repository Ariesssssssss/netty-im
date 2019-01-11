package com.lautumn.im.netty;

import com.lautumn.im.netty.config.WebSocketConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Lautumn
 * @Describe: websocket 服务配置
 * @Date: Create in 下午5:20 2019/1/11
 */
@Slf4j
@Data
public class WSServer {


    private Integer port = 8088;
    private String path = "/ws";

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap bootstrap;
    private ChannelFuture future;

    public WSServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInit(path));
    }

    public void start() {
        future = this.bootstrap.bind(port);
        log.info("netty websocket server 启动完毕, 访问路径: ws://localhost:{}{}", port, path);
    }
}
