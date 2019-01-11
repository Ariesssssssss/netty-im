package com.lautumn.im.netty.attribute;

import com.lautumn.im.netty.session.Session;
import io.netty.util.AttributeKey;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 上午11:23 2018/12/12
 */
public interface Attributes {

    AttributeKey<Session> SESSION = AttributeKey.newInstance("session");
}
