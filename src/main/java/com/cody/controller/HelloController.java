package com.cody.controller;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

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
     * zip4j 方法实现
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
     * 原生Java方法实现
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
            // 必须关闭，要不然这个zip文件一直被占用着，要删删不掉，改名也不可以，移动也不行，整多了，系统还崩了。
            try {
                if (zip != null) {
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

}
