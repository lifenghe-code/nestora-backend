package com.lifh.nestora.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lifh.nestora.annotation.LoginCheck;
import com.lifh.nestora.common.BaseResponse;
import com.lifh.nestora.common.ResultUtils;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.common.PageRequest;
import com.lifh.nestora.model.dto.space.SpaceAddRequest;
import com.lifh.nestora.model.dto.space.SpaceQueryRequest;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.model.vo.SpaceVO;
import com.lifh.nestora.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    SpaceService spaceService;

    @GetMapping("/get-space-by-user")
    BaseResponse<IPage<SpaceVO>> getSpace(PageRequest pageRequest, HttpServletRequest request){
        IPage<SpaceVO> spacesByUserId = spaceService.getSpacesByUserId(pageRequest, request);
        return ResultUtils.success(spacesByUserId);
    }

    @PostMapping("/add-space")
    BaseResponse<SpaceVO> addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        SpaceVO spaceVO = spaceService.addSpace(spaceAddRequest, request);
        ThrowUtils.throwIf(spaceVO.getId()<0, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(spaceVO);
    }

    @GetMapping("/get-picture-by-spaceId")
    BaseResponse<IPage<PictureVO>> getPictureBySpaceId(SpaceQueryRequest spaceQueryRequest,  HttpServletRequest request){

        IPage<PictureVO> pictureBySpaceId = spaceService.getPictureBySpaceId(spaceQueryRequest, request);
        return ResultUtils.success(pictureBySpaceId);
    }
    @PostMapping("/upload-picture")
    public BaseResponse<SpaceVO> uploadSpaceImage(@RequestParam("files") MultipartFile[] files, Long spaceId, HttpServletRequest request) throws IOException {
        SpaceVO spaceVO = spaceService.uploadPrivatePicture(files, spaceId, request);
        return ResultUtils.success(spaceVO);
    }
}
