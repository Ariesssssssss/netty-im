package com.lautumn.im.netty.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: Lautumn
 * @Describe: 会话信息
 * @Date: Create in 下午9:00 2019/1/10
 */
@Data
@AllArgsConstructor
@ToString
public class Session {
    private String userId;

    private String username;
}
