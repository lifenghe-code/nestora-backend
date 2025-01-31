package com.lifh.nestora.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 种类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;


    /**
     * 空间 id
     */
    private Long spaceId;


    private static final long serialVersionUID = 1L;
}