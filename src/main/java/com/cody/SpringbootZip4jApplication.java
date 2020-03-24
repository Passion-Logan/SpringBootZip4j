package com.cody;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * idea静态资源访问路径
 * 1 classpath:/META-INF/resources/
 * 2 classpath:/resources/
 * 3 classpath:/static/
 * 4 classpath:/public/
 * 5 /:当前项目的根路径
 */
@SpringBootApplication
public class SpringbootZip4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootZip4jApplication.class, args);
    }

}
