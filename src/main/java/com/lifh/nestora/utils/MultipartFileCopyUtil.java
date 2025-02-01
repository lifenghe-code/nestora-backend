package com.lifh.nestora.utils;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileCopyUtil {

    /**
     * 深拷贝 MultipartFile 数组
     *
     * @param multipartFiles 原始 MultipartFile 数组
     * @return 深拷贝后的 MultipartFile 数组
     * @throws IOException 如果读取文件内容失败
     */
    public static MultipartFile[] deepCopyMultipartFiles(MultipartFile[] multipartFiles) throws IOException {
        if (multipartFiles == null) {
            return null;
        }

        MultipartFile[] copiedFiles = new MultipartFile[multipartFiles.length];
        for (int i = 0; i < multipartFiles.length; i++) {
            MultipartFile file = multipartFiles[i];
            if (file != null && !file.isEmpty()) {
                // 读取文件内容到字节数组
                byte[] fileContent = file.getBytes();

                // 创建新的 MultipartFile 对象
                copiedFiles[i] = new MockMultipartFile(
                        file.getName(), // 表单字段名
                        file.getOriginalFilename(), // 文件名
                        file.getContentType(), // 文件类型
                        fileContent // 文件内容
                );
            } else {
                copiedFiles[i] = null; // 如果文件为空，设置为 null
            }
        }
        return copiedFiles;
    }
}