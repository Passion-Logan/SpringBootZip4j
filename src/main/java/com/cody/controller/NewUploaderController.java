package com.cody.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cody.utils.FileUtils;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.cody.request.FileUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ClassName: newUploaderController
 *
 * @author WQL
 * @Description: 用于vue-simple-upload 组件断点续传
 * @date: 2020/3/13 22:52
 * @since JDK 1.8
 */
@RestController
@RequestMapping("new/upload")
@Slf4j
public class NewUploaderController {

    // @Value("${upload.local.path}")
    private String uploadFolder = "/home/user";

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

        // 必须返回一个错误码，不然会造成第一个分片丢失
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
    public void chunk(@ModelAttribute FileUploadRequest chunk) {
        MultipartFile file = chunk.getFile();
        log.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(FileUtils.generatePath(uploadFolder, chunk));
            // 文件写入指定路径
            Files.write(path, bytes);
            log.debug("文件 {} 写入成功, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
