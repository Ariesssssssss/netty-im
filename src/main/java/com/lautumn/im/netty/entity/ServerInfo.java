package com.lautumn.im.netty.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午7:09 2019/1/16
 */

@Data
@AllArgsConstructor
public class ServerInfo implements Serializable {

    private String ip;

    private Integer port;

}
