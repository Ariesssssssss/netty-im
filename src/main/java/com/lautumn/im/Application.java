package com.lautumn.im;

import com.lautumn.im.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @Author: Lautumn
 * @Describe:
 * @Date: Create in 下午5:21 2019/1/11
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

}
