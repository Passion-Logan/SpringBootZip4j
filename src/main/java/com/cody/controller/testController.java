package com.cody.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: testController
 *
 * @author WQL
 * @Description:
 * @date: 2020年3月9日 0009 16:05
 * @since JDK 1.8
 */
@RestController
@RequestMapping("test")
public class testController {


    @GetMapping("hello")
    public String test() {
        return "测试连接";
    }
}
