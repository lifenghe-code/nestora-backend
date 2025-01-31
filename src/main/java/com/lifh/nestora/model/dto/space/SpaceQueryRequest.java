package com.lifh.nestora.model.dto.space;

import com.lifh.nestora.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {


    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
