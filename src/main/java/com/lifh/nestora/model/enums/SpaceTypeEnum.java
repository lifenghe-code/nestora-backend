package com.lifh.nestora.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum SpaceTypeEnum {
    STANDARD("标准版", 0, 20L, 1024L*100),
    PRO("专业版", 1, 50L, 1024L*200),
    ULTIMATE("旗舰版",2, 100L, 1024L*300);

    private final String text;

    private final Integer value;

    private final Long maxCount;

    private final Long maxSize;
    SpaceTypeEnum(String text, Integer value, Long maxCount, Long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value.equals(value)) {
                return spaceTypeEnum;
            }
        }
        return null;
    }

    public static List<Integer> getValues() {
        List<Integer> values = new ArrayList<>();
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            values.add(spaceTypeEnum.value);
        }
        return values;
    }
}
