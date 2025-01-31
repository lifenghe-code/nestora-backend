package com.lifh.nestora.utils;

import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片压缩工具类
 */

@Slf4j
@Component
public class ImageCompressUtil {
    @Resource
    OssUtil ossUtil;
    /**
     * 方式二压缩 Google大法 因为Thumbnails.of() 方法是一个重载方法，参数不仅仅局限于是一个文件类型 可以是以流的形式 File形式，ImageBuffer对象，URL路径,String类型指定文件路径
     * 然后可通过链式构造器传入不通参数，压缩比例，指定输出的格式等最终通过toFile("文件存储路径")返回一个已经压缩完成的图片。
     *
     * @param file 待压缩的文件
     * @return 压缩后图片路径 这个可自己指定
     */
    public UploadPictureResult uploadThumbnail(MultipartFile file) {
        //得到上传时的原文件名
        String originalFilename = file.getOriginalFilename();
        //获取文件格式
        ThrowUtils.throwIf(originalFilename == null, ErrorCode.PARAMS_ERROR);
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        //获取uuid作为文件名
        String name = UUID.randomUUID().toString().replaceAll("-", "");
            // 先尝试压缩并保存图片
        try {

            File tempFile = File.createTempFile("thumbnail-"+name, ".jpeg");
            // 在程序结束时删除临时文件
            tempFile.deleteOnExit();
            Thumbnails.of(file.getInputStream()).scale(0.6f)
                    .outputQuality(0.6f)
                    .outputFormat("jpeg")
                    .toFile(tempFile);  // Save the file with the specified name


            return ossUtil.uploadImage(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
