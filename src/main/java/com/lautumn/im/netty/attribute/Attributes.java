package com.lautumn.im.netty.attribute;

import io.netty.util.AttributeKey;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 上午11:23 2018/12/12
 */
public interface Attributes {

    // 保存用户id
    AttributeKey<String> USERID = AttributeKey.newInstance("user");
}
