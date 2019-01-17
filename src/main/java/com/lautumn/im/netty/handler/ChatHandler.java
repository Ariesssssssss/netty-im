package com.lautumn.im.netty.handler;

import ch.qos.logback.core.util.SystemInfo;
import com.lautumn.im.netty.attribute.Attributes;
import com.lautumn.im.netty.entity.ChatMsg;
import com.lautumn.im.netty.entity.DataContent;
import com.lautumn.im.netty.entity.ServerInfo;
import com.lautumn.im.netty.entity.UserChannelRel;
import com.lautumn.im.netty.enums.MsgActionEnum;
import com.lautumn.im.netty.session.Session;
import com.lautumn.im.util.IDUtil;
import com.lautumn.im.util.JsonUtils;
import com.lautumn.im.util.SessionUtil;
import com.lautumn.im.util.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * @Author: Lautumn
 * @Describe: TextWebSocketFrame:在netty中为websocket专门处理文本的对象， frame是消息的载体
 * @Date: Create in 下午5:30 2019/1/11
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private static final String USER_PREFIX = "user:";

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

            ServerInfo serverInfo = getServerInfo();

            // 保存用户和本机关系
            StringRedisTemplate stringRedisTemplate = SpringUtil.getBean("stringRedisTemplate", StringRedisTemplate.class);
            String serverInfoStr = JsonUtils.objectToJson(serverInfo);
            stringRedisTemplate.opsForValue().set(USER_PREFIX + senderId, serverInfoStr);

            writerAndFlush(currentChannel, response);

            log.info("用户连接: [{}]", senderId);

        } else if (action == MsgActionEnum.CHAT.type) {
            // 2.2 聊天类型的消息
            ChatMsg chatMsg = dataContent.getChatMsg();

            // 响应数据
            DataContent response = new DataContent();
            response.setAction(MsgActionEnum.SIGNED.type);
            response.setChatMsg(chatMsg);

            // 找到接收者,由于是集群的部署方式，先在本节点查找对应接收者的连接是否在本节点，
            // 如果不在，那么查询redis找到接收者所在节点的ip，将响应数据通过http的方式发送
            String receiverId = chatMsg.getReceiverId();

            Channel receiverChannel = UserChannelRel.get(receiverId);

            if (receiverChannel != null) {

                // 从 users里获取真正的连接
                Channel realChannel = users.find(receiverChannel.id());

                writerAndFlush(realChannel, response);
                log.info("[{}] 向 [{}] 发送消息", chatMsg.getSenderId(), chatMsg.getReceiverId());

            } else {

                // 去redis查找
                StringRedisTemplate stringRedisTemplate = SpringUtil.getBean("stringRedisTemplate", StringRedisTemplate.class);
                String serverInfoJson = stringRedisTemplate.opsForValue().get(receiverId);
                if (serverInfoJson != null && serverInfoJson.length() > 0) {
                    ServerInfo serverInfo = JsonUtils.jsonToPojo(serverInfoJson, ServerInfo.class);
                    // 发送http请求，将响应数据发到对应存有接收用户的channel连接的服务器上

                }

            }

        } else if (action == MsgActionEnum.PULL_FRIEND.type) {

            // 从redis拉取所有用户
            StringRedisTemplate stringRedisTemplate = SpringUtil.getBean("stringRedisTemplate", StringRedisTemplate.class);

            Set<String> userIdSet = stringRedisTemplate.keys(USER_PREFIX + "*");

            // 过滤自己
            String senderId = dataContent.getChatMsg().getSenderId();
            userIdSet.remove(USER_PREFIX + senderId);

            DataContent response = new DataContent();
            response.setAction(MsgActionEnum.PULL_FRIEND.type);
            ChatMsg chatMsg = new ChatMsg();
            StringBuilder userIds = new StringBuilder();
            userIdSet.forEach(userId -> {

                userIds.append(userId.substring(USER_PREFIX.length()));
                userIds.append(",");
            });
            if (userIds.length() > 0){
                String userIdStr = userIds.substring(0, userIds.length() - 1);
                chatMsg.setMsg(userIdStr);
            }
            response.setChatMsg(chatMsg);
            writerAndFlush(currentChannel, response);

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
        String userId = ctx.channel().attr(Attributes.USERID).get();
        if (userId != null) {
            StringRedisTemplate stringRedisTemplate = SpringUtil.getBean("stringRedisTemplate", StringRedisTemplate.class);
            stringRedisTemplate.delete(USER_PREFIX + userId);
            UserChannelRel.remove(userId);
        }

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

    private ServerInfo getServerInfo() throws UnknownHostException {
        Environment env = SpringUtil.getBean(Environment.class);
        String port = env.getProperty("server.port");
        String ip = Inet4Address.getLocalHost().getHostAddress();
        ServerInfo serverInfo = new ServerInfo(ip, Integer.valueOf(port));
        return serverInfo;
    }

    private void writerAndFlush(Channel channel, DataContent dataContent) {
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
    }

}
