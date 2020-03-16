package com.cody.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.cody.request.FileUploadRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: FileUtils
 *
 * @author WQL
 * @Description:
 * @date: 2020年3月16日 0016 19:13
 * @since JDK 1.8
 */
@Slf4j
public class FileUtils {

    public static String generatePath(String uploadFolder, FileUploadRequest chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append(uploadFolder).append("/").append(chunk.getIdentifier());

        // 判断uploadFolder/identifier 路径是否存在，不存在则创建
        if (!Files.isWritable(Paths.get(sb.toString()))) {
            log.info("path not exist,create path: {}", sb.toString());

            try {
                Files.createDirectories(Paths.get(sb.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.append("/").append(chunk.getIdentifier()).append("-").append(chunk.getChunkNumber()).toString();
    }

    /**
     * @Description: 合并文件
     * @Date: 2020/3/13
     * @Param targetFile: 合并后的文件夹
     * @Param folder: 分片目标文件夹
     * @Param fileName: 原文件名称
     * @return: void
     */
    public static void merge(String targetFile, String folder) {
        try {
            // 创建空文件
            File outputFile = new File(targetFile);
            File parentFile = outputFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            outputFile.createNewFile();

            Files.list(Paths.get(folder)).filter(path -> path.getFileName().toString().contains("-"))
                .sorted((o1, o2) -> {
                    String p1 = o1.getFileName().toString();
                    String p2 = o2.getFileName().toString();
                    int i1 = p1.lastIndexOf("-");
                    int i2 = p2.lastIndexOf("-");
                    return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
                }).forEach(path -> {
                    try {
                        // 以追加的形式写入文件
                        Files.write(Paths.get(targetFile), Files.readAllBytes(path), StandardOpenOption.APPEND);
                        // 合并后删除该块
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
