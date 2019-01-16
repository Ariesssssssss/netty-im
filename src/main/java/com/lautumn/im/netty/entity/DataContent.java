package com.lautumn.im.netty.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午4:08 2019/1/16
 */
@Data
public class DataContent implements Serializable{

    private Integer action;

    private ChatMsg chatMsg;

}
