package com.lautumn.im.util;

import java.util.UUID;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 上午8:59 2018/12/17
 */
public class IDUtil {

    public static String randomId() {
        return UUID.randomUUID().toString().split("-")[0];
    }
}
