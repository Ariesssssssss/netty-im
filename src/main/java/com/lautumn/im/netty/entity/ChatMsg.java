package com.lautumn.im.netty.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Lautumn
 * @Describe: 消息模型
 * @Date: Create in 下午4:05 2019/1/16
 */
@Data
public class ChatMsg implements Serializable {

    private String msg; // 消息内容

    private String senderId; // 发送者用户id

    private String receiverId; // 接受者的用户id

    private String msgId; // 用于消息的签收

}
