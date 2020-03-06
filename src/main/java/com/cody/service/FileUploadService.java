package com.cody.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 应用模块名称
 * <p>
 * 代码描述
 * <p>
 *
 * @author WQL
 * @since 2020年3月6日 0006 12:04
 */
@Service
public class FileUploadService extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * 后台统一存放文件的路径
     */
    private String serverPath = "E:\\download";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
        System.out.println("进入FileUploadService后台...");
        // 1.创建DiskFileItemFactory对象，配置缓存用
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // 2. 创建 ServletFileUpload对象
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        // 3. 设置文件名称编码
        servletFileUpload.setHeaderEncoding("utf-8");
        // 4. 开始解析文件
        // 文件md5获取的字符串
        String fileMd5 = null;
        // 文件的索引
        String chunk = null;

        try {
            List<FileItem> items = servletFileUpload.parseRequest(request);

            for (FileItem fileItem : items) {
                if (fileItem.isFormField()) {
                    // 普通数据,例如字符串
                    String fieldName = fileItem.getFieldName();
                    if ("info".equals(fieldName)) {
                        String info = fileItem.getString("utf-8");
                        System.out.println("info:" + info);
                    }
                    if ("fileMd5".equals(fieldName)) {
                        fileMd5 = fileItem.getString("utf-8");
                        System.out.println("fileMd5:" + fileMd5);
                    }
                    if ("chunk".equals(fieldName)) {
                        chunk = fileItem.getString("utf-8");
                        System.out.println("chunk:" + chunk);
                    }
                } else {
                    // >> 文件
                    if (StringUtils.isEmpty(fileMd5)) {
                        // 假如md5没有，就用test作为目录名
                        fileMd5 = "test";
                    }
                    if (StringUtils.isEmpty(chunk)) {
                        // filename
                        chunk = fileItem.getName();
                    }

                    // 如果文件夹没有创建文件夹
                    File file = new File(serverPath + File.separator + fileMd5);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    // 落地保存文件
                    // 这时保存的每个块，块先存好，后续会调合并接口，将所有块合成一个大文件
                    File chunkFile = new File(serverPath + File.separator + fileMd5 + File.separator + chunk);
                    FileUtils.copyInputStreamToFile(fileItem.getInputStream(), chunkFile);
                }
            }

        } catch (FileUploadException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
