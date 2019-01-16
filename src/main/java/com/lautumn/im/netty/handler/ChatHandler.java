package com.lautumn.im.netty.handler;

import com.lautumn.im.netty.entity.ChatMsg;
import com.lautumn.im.netty.entity.DataContent;
import com.lautumn.im.netty.entity.UserChannelRel;
import com.lautumn.im.netty.enums.MsgActionEnum;
import com.lautumn.im.netty.session.Session;
import com.lautumn.im.util.IDUtil;
import com.lautumn.im.util.JsonUtils;
import com.lautumn.im.util.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Lautumn
 * @Describe: TextWebSocketFrame:在netty中为websocket专门处理文本的对象， frame是消息的载体
 * @Date: Create in 下午5:30 2019/1/11
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 用于记录和管理所有客户端的channel
     */
    private static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        // 获取客户端传来的消息
        String content = msg.text();

        Channel currentChannel = ctx.channel();

        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();

        // 判断消息类型，做不同的处理
        if (action == MsgActionEnum.CONNECT.type) {

            // 第一次连接，保存用户和channel的关系,并且在redis保存用户和登录节点的映射
            String senderId = IDUtil.randomId();
            DataContent response = new DataContent();
            response.setAction(MsgActionEnum.CONNECT.type);
            ChatMsg responseChatMsg = new ChatMsg();
            response.setChatMsg(responseChatMsg);
            responseChatMsg.setSenderId(senderId);

            UserChannelRel.put(senderId, currentChannel);

            currentChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(response)));

            log.info("用户连接: [{}]", senderId);

        } else if (action == MsgActionEnum.CHAT.type) {
            // 2.2 聊天类型的消息
            ChatMsg chatMsg = dataContent.getChatMsg();

            // 响应数据
            DataContent response = new DataContent();
            response.setAction(MsgActionEnum.SIGNED.type);
            response.setChatMsg(chatMsg);


            // 找到接收者,由于是集群的部署方式，现在本节点查找对应接收者的连接是否在本节点，
            // 如果不在，那么查询redis找到接收者所在节点的ip，将响应数据通过http的方式发送
            String receiverId = chatMsg.getReceiverId();

            Channel receiverChannel = UserChannelRel.get(receiverId);

            if (receiverChannel != null) {

                // 从 users里获取真正的连接
                Channel realChannel = users.find(receiverChannel.id());
                // 用户在应用里
                realChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(response)));
                log.info("[{}] 向 [{}] 发送消息", chatMsg.getSenderId(), chatMsg.getReceiverId());

            } else {

                // 去redis查找

            }

        }


    }

    /**
     * 当客户端连接服务端之后执行的生命周期,获取客户端的channel并且添加到全局管理channel中，
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 添加到
        users.add(ctx.channel());
    }


    /**
     * 浏览器关闭后执行的生命周期,
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        clients.remove(ctx.channel()); ChannelGroup会自动移除，所以这行代码不需要写
        users.remove(ctx.channel());
        log.info("用户下线");
    }

    /**
     * 发生异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常后关闭连接，随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }

}
