# SpringBootZip4j
SpringBoot整合zip4j上传zip文件，递归处理文件夹
上传方法：
1.Java自带的ZipFile相关实现压缩包解压(可解压所有压缩软件的，不会乱码)
2.Zip4J工具包实现压缩包解压功能（360压缩包解压中文名称会乱码）
3.Ant工具包实现压缩包解压（360压缩包解压中文名称会乱码）


前端使用WebUploder分块上传，后端分块接收完毕后整合文件
