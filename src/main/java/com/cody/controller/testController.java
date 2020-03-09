package com.cody.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public void test(HttpServletResponse response) throws IOException {
        response.setStatus(200);
        response.getWriter().append("success");
    }
}
