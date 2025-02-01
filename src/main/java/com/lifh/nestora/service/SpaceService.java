package com.lifh.nestora.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lifh.nestora.common.PageRequest;
import com.lifh.nestora.model.dto.picture.PictureUploadRequest;
import com.lifh.nestora.model.dto.space.SpaceAddRequest;
import com.lifh.nestora.model.dto.space.SpaceQueryRequest;
import com.lifh.nestora.model.entity.Space;
import com.lifh.nestora.model.entity.User;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.model.vo.SpaceVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * @author li_fe
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-01-30 14:03:41
 */
public interface SpaceService {
    /***
     * 新建私人空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    SpaceVO addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request);

    /**
     * 根据登录用户，获得用户的空间
     *
     * @param pageRequest
     * @param request
     * @return
     */
    IPage<SpaceVO> getSpacesByUserId(PageRequest pageRequest, HttpServletRequest request);

    /**
     * 获取spaceVO信息
     *
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 根据spaceId获取图片
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    IPage<PictureVO> getPictureBySpaceId(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request);

    SpaceVO uploadPrivatePicture(MultipartFile[] multipartFiles, Long spaceId, HttpServletRequest request) throws IOException;
}