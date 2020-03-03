package com.cody.controller;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;

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

    /**
     * 文件类型： MP3 MP4 pdf png 文件夹 1
     *
     * @param file
     * @return
     */
    @PostMapping("uploadFile")
    public @ResponseBody String uploadFile(@RequestParam(value = "file", required = true) MultipartFile file) {
        File files = null;
        try {
            String fileName = file.getOriginalFilename();
            String path = "E:\\download";
            files = new File(path + "\\" + fileName);
            if (!files.getParentFile().exists()) {
                files.getParentFile().mkdirs();
            }

            file.transferTo(files);

            // 指向zip文件
            ZipFile zipFile = new ZipFile(files);

            String[] types = {"mp3", "mp4"};
            // 校验文件内容
            checkContent(zipFile, types);

            // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            if (!zipFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法,可能被损坏.");
            }

            // 防止乱码
            zipFile.setCharset(Charset.forName("utf-8"));

            UUID uuid=UUID.randomUUID();
            String dest = "E:\\download\\" + uuid.toString();
            File destDir = new File(dest);

            zipFile.getFileHeaders().forEach(v -> {
                String extractedFile = getFileNameFromExtraData(v);
                try {
                    zipFile.extractFile(v, dest, extractedFile);
                } catch (ZipException e) {
                    e.printStackTrace();
                    return;
                }
            });

            // 执行上传操作与数据添加 单独封装
        } catch (ZipException e) {
            e.printStackTrace();
            return "上传失败," + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败," + e.getMessage();
        } catch (RuntimeException e) {
            return "上传失败, " + e.getMessage();
        } finally {
            if (files != null) {
                files.delete();
            }
        }

        return "上传成功";
    }

    /**
     * 校验文件内容
     *
     * @param zipFile
     * @param types
     */
    public void checkContent(ZipFile zipFile, String[] types) {
        AtomicBoolean is = new AtomicBoolean(true);
        try {
            zipFile.getFileHeaders().forEach(v -> {
                String extractedFile = getFileNameFromExtraData(v);
                is.set(Stream.of(types).anyMatch(e -> extractedFile.contains(e)));
            });
        } catch (ZipException e) {
            throw new RuntimeException("未知错误.");
        } finally {
            if (!is.compareAndSet(true, false)) {
                throw new RuntimeException("压缩包内容错误.");
            }
        }

    }

    /**
     * 获取文件名称（防止中文乱码）
     *
     * @param fileHeader
     * @return
     */
    public String getFileNameFromExtraData(FileHeader fileHeader) {
        if (fileHeader.getExtraDataRecords() != null) {
            for (ExtraDataRecord extraDataRecord : fileHeader.getExtraDataRecords()) {
                long identifier = extraDataRecord.getHeader();
                if (identifier == 0x7075) {
                    byte[] bytes = extraDataRecord.getData();
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    byte version = buffer.get();
                    assert (version == 1);
                    int crc32 = buffer.getInt();
                    return new String(bytes, 5, buffer.remaining(), StandardCharsets.UTF_8);
                }
            }
        }
        return fileHeader.getFileName();
    }

}
