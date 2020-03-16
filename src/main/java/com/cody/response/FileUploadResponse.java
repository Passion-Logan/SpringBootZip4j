package com.cody.response;

import java.util.ArrayList;

import lombok.Data;

/**
 * ClassName: FileUploadResponse
 *
 * @author WQL
 * @Description:
 * @date: 2020年3月16日 0016 19:05
 * @since JDK 1.8
 */
@Data
public class FileUploadResponse {

    /**
     * 是否合并
     */
    boolean needMerge;

    /**
     * 存放的路径
     */
    String path;

    /**
     * 文件名称
     */
    String name;

    /**
     * 已经上传的分块
     */
    ArrayList uploadde;

}
