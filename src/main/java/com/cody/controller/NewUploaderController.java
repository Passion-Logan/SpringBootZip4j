package com.cody.controller;

import com.cody.request.FileUploadRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: newUploaderController
 *
 * @author WQL
 * @Description: 用于vue-simple-upload 组件断点续传
 * @date: 2020/3/13 22:52
 * @since JDK 1.8
 */
public class NewUploaderController {

    /**
     * @Description: 检查分片是否上传
     * @Date: 2020/3/13
     * @Param response:
     * @return: void
     */
    @GetMapping("chunk")
    @ResponseBody
    public Object chunk(@ModelAttribute FileUploadRequest chunk, HttpServletResponse response) {
        if (chunk.getChunkNumber() == 1) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }

        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

        return "校验通过";
    }

    /**
     * @Description: 保存分片文件
     * @Date: 2020/3/14
     * @Param request:
     * @Param response:
     * @return: void
     */
    @PostMapping("chunk")
    public void chunk(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * @Description: 合并分片
     * @Date: 2020/3/14
     * @Param request:
     * @Param response:
     * @return: void
     */
    @PostMapping("mergeFile")
    public void mergeFile(HttpServletRequest request, HttpServletResponse response) {

    }
}
