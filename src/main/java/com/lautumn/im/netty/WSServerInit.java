package com.lautumn.im.netty;

import com.lautumn.im.netty.handler.ChatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午5:25 2019/1/11
 */
public class WSServerInit extends ChannelInitializer<SocketChannel> {

    private String path = "/ws";

    public WSServerInit(String path) {
        this.path = path;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        // websocket 基于 http 协议，所以要有 http 的编解码助手类
        ch.pipeline().addLast(new HttpServerCodec());

        // 对写大数据流的支持
        ch.pipeline().addLast(new ChunkedWriteHandler());

        // 对 httpMessage 进行聚合，聚合成 FullHttpRequest 或 FullHttpResponse
        // 几乎在 netty 的编程中都会使用到此handler
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 64));

        // websocket 服务器处理协议，指定给客户端访问连接的路由, 访问 ws://localhost:8088/ws
        ch.pipeline().addLast(new WebSocketServerProtocolHandler(path));

        // 处理发来的数据 handler
        ch.pipeline().addLast(new ChatHandler());


    }
}
