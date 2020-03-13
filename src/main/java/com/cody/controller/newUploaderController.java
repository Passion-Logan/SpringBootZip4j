package com.cody.controller;

import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: newUploaderController
 *
 * @author WQL
 * @Description: 用于vue-simple-upload 组件断点续传
 * @date: 2020/3/13 22:52
 * @since JDK 1.8
 */
public class newUploaderController {

    /**
     * @Description: 减产分片是否上传
     * @Date: 2020/3/13
     * @Param response:
     * @return: void
     */
    @GetMapping("chunk")
    public void chunk(HttpServletResponse response) {

    }
}
