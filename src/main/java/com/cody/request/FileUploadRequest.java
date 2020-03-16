package com.cody.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileUploadRequest
 *
 * @author WQL
 * @Description:
 * @date: 2020年3月16日 0016 19:01
 * @since JDK 1.8
 */
@Data
public class FileUploadRequest {

    /**
     * 当前文件块，从1开始
     */
    private Integer chunkNumber;
    /**
     * 分块大小
     */
    private Long chunkSize;
    /**
     * 当前分块大小
     */
    private Long currentChunkSize;
    /**
     * 总大小
     */
    private Long totalSize;
    /**
     * 文件标识
     */
    private String identifier;
    /**
     * 文件名
     */
    private String filename;
    /**
     * 相对路径
     */
    private String relativePath;
    /**
     * 总块数
     */
    private Integer totalChunks;

    private MultipartFile file;

}
