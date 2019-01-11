package com.lautumn.im.util;

import com.lautumn.im.netty.attribute.Attributes;
import com.lautumn.im.netty.session.Session;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Lautumn
 * @Describe: session 管理
 * @Date: Create in 下午9:01 2019/1/10
 */
public class SessionUtil {

    // 存储 用户id和连接对应关系
    private static final Map<String, Channel> userIdChannelMap = new HashMap<>();

    public static void bindSession(Session session, Channel channel) {
        userIdChannelMap.put(session.getUserId(), channel);
        channel.attr(Attributes.SESSION).set(session);
    }

    public static void unBindSession(Channel channel) {
        if (hasLogin(channel)) {
            Session session = getSession(channel);
            userIdChannelMap.remove(session.getUserId());
            channel.attr(Attributes.SESSION).set(null);
        }
    }

    public static Session getSession(Channel channel) {
        return channel.attr(Attributes.SESSION).get();
    }


    public static boolean hasLogin(Channel channel) {
        return getSession(channel) != null;
    }


}
