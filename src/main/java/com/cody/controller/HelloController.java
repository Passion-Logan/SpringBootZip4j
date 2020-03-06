package com.cody.controller;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
     * 单点续传界面
     *
     * @return
     */
    @GetMapping("pro")
    public String pro() {
        return "upload";
    }

    /**
     * 文件类型： MP3 MP4 pdf png 文件夹
     *
     * @param file
     * @return
     */
    @PostMapping("uploadFile")
    public @ResponseBody String uploadFile(@RequestParam(value = "file") MultipartFile file) {
        return JavaFunction(file);
    }

    /**
     * 原生Java方法实现 注：任意压缩软件都可解
     *
     * @param file
     * @return
     */
    public String JavaFunction(MultipartFile file) {
        File files = null;
        java.util.zip.ZipFile zip = null;
        String msg = "上传成功";

        try {
            String fileName = file.getOriginalFilename();

            String path = "E:\\download";
            files = new File(path + "\\" + fileName);
            if (!files.getParentFile().exists()) {
                files.getParentFile().mkdirs();
            }
            file.transferTo(files);

            String encoding = getEncoding(path + "\\" + fileName);
            zip = new java.util.zip.ZipFile(files, Charset.forName(encoding));

            if (!zip.getName().contains(".zip")) {
                throw new RuntimeException("压缩文件不合法.");
            }

            String[] types = {"mp3", "mp4"};
            // 校验文件内容
            CheckContentByJava(zip, types);

            // 指定解压后的文件夹
            UUID uuid = UUID.randomUUID();
            String dest = "E:\\download\\" + uuid.toString();
            File destDir = new File(dest);
            destDir.mkdir();

            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);

                // 指定文件路径+当前zip文件的名称
                String outPath = (destDir + "/" + zipEntryName).replace("/", File.separator);

                // destDir 修改 这个路径 ；
                // 根据 文件分类 选择不同路径， 视屏 图片 pdf 和 资料(文件夹和html) 保存服务器， 其余的 上传 oss
                // 解压到指定路径下，单独封装 文件上传操作 以及 数据添加操作
                // outPath 文件路径；zipEntryName 文件名

                if (!entry.isDirectory()) {
                    // 保存文件路径信息（可利用md5.zip名称的唯一性，来判断是否已经解压）
                    OutputStream out = new FileOutputStream(outPath);
                    byte[] buf1 = new byte[2048];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                    in.close();
                    out.close();
                } else {
                    // 判断路径是否存在,不存在则创建文件路径
                    File filePath = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "上传失败," + e.getMessage();
        } finally {
            try {
                if (zip != null) {
                    // 必须关闭，要不然这个zip文件一直被占用着，要删删不掉，改名也不可以，移动也不行，整多了，系统还崩了。
                    zip.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (files.exists()) {
                files.delete();
            }
        }

        return msg;
    }

    /**
     * zip4j 方法实现 注：360压缩包中文会乱码
     *
     * @param file
     * @return
     */
    public String ZipFunction(MultipartFile file) {
        File files = null;
        String msg = "上传成功";
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

            // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            if (!zipFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法.");
            }

            String[] types = {"mp3", "mp4"};
            // 校验文件内容
            // checkContent(zipFile, types);

            // 防止乱码
            zipFile.setCharset(Charset.forName("utf-8"));

            UUID uuid = UUID.randomUUID();
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
        } catch (Exception e) {
            e.printStackTrace();
            msg = "上传失败," + e.getMessage();
        } finally {
            if (files != null) {
                files.delete();
            }
        }

        return msg;
    }

    /**
     * ant解压实现 注：360压缩包中文会乱码
     *
     * @param file
     * @return
     */
    @SuppressWarnings("unchecked")
    public String AntFunntion(MultipartFile file) {
        File files = null;

        try {
            String fileName = file.getOriginalFilename();
            String path = "E:\\download";
            files = new File(path + "\\" + fileName);
            if (!files.getParentFile().exists()) {
                files.getParentFile().mkdirs();
            }

            file.transferTo(files);

            // 处理中文文件名乱码的问题
            org.apache.tools.zip.ZipFile zipfile = new org.apache.tools.zip.ZipFile(path + "\\" + fileName, "GBK");

            String strPath, gbkPath, strtemp;

            UUID uuid = UUID.randomUUID();
            String dest = "E:\\download\\" + uuid.toString();
            File tempFile = new File(dest);

            strPath = tempFile.getAbsolutePath();
            Enumeration<?> e = zipfile.getEntries();
            while (e.hasMoreElements()) {
                org.apache.tools.zip.ZipEntry zipEnt = (org.apache.tools.zip.ZipEntry)e.nextElement();
                gbkPath = zipEnt.getName();
                if (zipEnt.isDirectory()) {
                    strtemp = strPath + File.separator + gbkPath;
                    File dir = new File(strtemp);
                    dir.mkdirs();
                    continue;
                } else {
                    // 读写文件
                    InputStream is = zipfile.getInputStream(zipEnt);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    gbkPath = zipEnt.getName();
                    // 建目录
                    strtemp = strPath + File.separator + gbkPath;
                    String strsubdir = gbkPath;
                    for (int i = 0; i < strsubdir.length(); i++) {
                        if (strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {
                            String temp = strPath + File.separator + strsubdir.substring(0, i);
                            File subdir = new File(temp);
                            if (!subdir.exists())
                                subdir.mkdir();
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(strtemp);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int c;
                    while ((c = bis.read()) != -1) {
                        bos.write((byte)c);
                    }
                    bos.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "上传成功";
    }

    /**
     * 校验文件内容
     *
     * @param zipFile
     * @param types
     */
    public void CheckContent(ZipFile zipFile, String[] types) {
        AtomicBoolean is = new AtomicBoolean(true);
        try {
            zipFile.getFileHeaders().forEach(v -> {
                String extractedFile = getFileNameFromExtraData(v);
                is.set(Stream.of(types).anyMatch(e -> extractedFile.contains(e)));
                if (!is.compareAndSet(true, false)) {
                    throw new RuntimeException("压缩包内容错误.");
                }
            });
        } catch (ZipException e) {
            throw new RuntimeException("未知错误.");
        }
    }

    /**
     * 原生Java解压包校验文件内容
     *
     * @param zip
     * @param types
     */
    public void CheckContentByJava(java.util.zip.ZipFile zip, String[] types) {
        AtomicBoolean is = new AtomicBoolean(true);
        zip.stream().forEach(v -> {
            String name = v.getName();
            System.out.println(name);
            is.set(Stream.of(types).anyMatch(e -> name.contains(e)));
            if (!is.compareAndSet(true, false)) {
                throw new RuntimeException("压缩包内容错误.");
            }
        });
    }

    public String getFileNameFromExtraData(FileHeader fileHeader) {
        String fileName = fileHeader.getFileName();
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

        return fileName;
    }

    @SuppressWarnings("unchecked")
    private static String getEncoding(String path) throws Exception {
        String encoding = "GBK";
        ZipFile zipFile = new ZipFile(path);
        zipFile.setCharset(Charset.forName(encoding));
        List<FileHeader> list = zipFile.getFileHeaders();
        for (int i = 0; i < list.size(); i++) {
            FileHeader fileHeader = list.get(i);
            String fileName = fileHeader.getFileName();
            if (isMessyCode(fileName)) {
                encoding = "UTF-8";
                break;
            }
        }
        return encoding;
    }

    private static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            // 从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            if ((int)c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }

    /**
     * 后台统一存放文件的路径
     */
    private String serverPath = "E:\\download";

    @PostMapping("isMergeChunks")
    public void uploadPro(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.isMergerChunks(request, response);
    }

    @PostMapping("getFile")
    public void getFileFunction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.getFile(request, response);
    }

    /**
     * 分片操作： 已上传就合并
     *
     * @param request
     * @param response
     */
    public void isMergerChunks(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 根据前台的action参数决定要做的动作
        String action = request.getParameter("action");

        if ("mergeChunks".equals(action)) {
            // 获得需要合并的目录
            String fileMd5 = request.getParameter("fileMd5");
            // 文件的原始文件名
            String fileName = request.getParameter("fileName");

            System.out.println("当前合并的目录fileMd5：" + fileMd5);
            // 读取目录所有文件
            File f = new File(serverPath + File.separator + fileMd5);
            // 排除目录，只要文件
            File[] fileArray = f.listFiles(pathname -> {
                if (pathname.isDirectory()) {
                    return false;
                }
                return true;
            });
            // 转成集合，便于排序
            List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));

            // 从小到大排序
            Collections.sort(fileList, (o1, o2) -> {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            });

            // fileName：沿用原始的文件名，或者可以使用随机的字符串作为新文件名，但是要 保留原文件的后缀类型
            File outputFile =
                new File(serverPath + File.separator + UUID.randomUUID().toString() + File.separator + fileName);

            File parentFile = outputFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            // 创建文件
            outputFile.createNewFile();

            // 输出流
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            FileChannel outChannel = fileOutputStream.getChannel();

            // 合并，核心就是FileChannel，将多个文件合并为一个文件
            FileChannel inChannel;
            for (File file : fileList) {
                inChannel = new FileInputStream(file).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inChannel.close();

                // 删除分片
                file.delete();
            }

            // 关闭流
            fileOutputStream.close();
            outChannel.close();

            // 清除文件夹
            File tempFile = new File(serverPath + File.separator + fileMd5);
            if (tempFile.isDirectory() && tempFile.exists()) {
                tempFile.delete();
            }

            System.out.println("合并文件成功：" + outputFile.getAbsolutePath());

        } else if ("checkChunk".equals(action)) {
            // 校验文件是否已经上传并返回结果给前端，就一个作用：校验块是否存在，假如不存在，前端会再次用上传器传到后台

            // 文件唯一表示
            String fileMd5 = request.getParameter("fileMd5");
            // 当前分块下标
            String chunk = request.getParameter("chunk");
            // 当前分块大小
            String chunkSize = request.getParameter("chunkSize");

            // 直接根据块的索引号找到分块文件
            File checkFile = new File(serverPath + File.separator + fileMd5 + File.separator + chunk);

            // 检查文件是否存在，且大小一致（必须满足这两个条件才认为块是已传成功）
            response.setContentType("text/html;charset=utf-8");
            if (checkFile.exists() && checkFile.length() == Integer.parseInt((chunkSize))) {
                response.getWriter().write("{\"ifExist\":1}");
            } else {
                // 假如文件没存在，说明没有上传成功，返回0
                response.getWriter().write("{\"ifExist\":0}");
            }
        }
    }

    /**
     * 获取分片文件
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
