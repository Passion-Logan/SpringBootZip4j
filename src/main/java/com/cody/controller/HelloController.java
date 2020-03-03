package com.cody.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应用模块名称
 * <p>
 * 代码描述
 * <p>
 *
 * @author WQL
 * @since 2020年3月3日 0003 10:47
 */
@Controller
public class HelloController {

    @RequestMapping("/")
    public String hello() {
        return "hello";
    }

    @PostMapping("uploadFile")
    public @ResponseBody String uploadFile(@RequestParam(value = "file", required = true) MultipartFile file) {

        try {

            String fileName = file.getOriginalFilename();
            String path = "E:\\download";
            File files = new File(path + "\\" + fileName);
            if (!files.getParentFile().exists()) {
                files.getParentFile().mkdirs();
            }

            file.transferTo(files);
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败";
        }

        return "上传成功";
    }

}
