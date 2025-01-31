package com.lifh.nestora.model.dto.space;

import lombok.Data;

@Data
public class SpaceAddRequest {
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    /**
     * 创建用户 id
     */
    private Long userId;
}
