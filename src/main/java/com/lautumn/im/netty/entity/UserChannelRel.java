package com.lautumn.im.netty.entity;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * @Author: Lautumn
 * @Describe: 用户和channel关联关系
 * @Date: Create in 上午11:09 2018/9/5
 */
public class UserChannelRel {
    private static HashMap<String, Channel> manager = new HashMap<>();

    public static void put(String userId, Channel channel){
        manager.put(userId, channel);
    }

    public static Channel get(String userId){
        return manager.get(userId);
    }
}
