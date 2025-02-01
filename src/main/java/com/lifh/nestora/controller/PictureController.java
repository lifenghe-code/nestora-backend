package com.lifh.nestora.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifh.nestora.annotation.AuthCheck;
import com.lifh.nestora.common.BaseResponse;
import com.lifh.nestora.common.DeleteRequest;
import com.lifh.nestora.common.ResultUtils;
import com.lifh.nestora.exception.BusinessException;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.model.vo.PictureTagCategory;
import com.lifh.nestora.constant.UserConstant;
import com.lifh.nestora.model.dto.picture.PictureQueryRequest;
import com.lifh.nestora.model.dto.picture.PictureUpdateRequest;
import com.lifh.nestora.model.dto.picture.PictureUploadRequest;
import com.lifh.nestora.model.entity.Picture;
import com.lifh.nestora.model.entity.User;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.service.PictureService;
import com.lifh.nestora.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;


    @GetMapping("/all")
    public BaseResponse<Long> all(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long count = pictureService.count();
        return ResultUtils.success(count);
    }

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = null;
        try {
            pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtils.success(pictureVO);
    }

    /**
     * 上传图片
     */
    @PostMapping("/user-upload")
    public BaseResponse<PictureVO> userUploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestParam String category,
            @RequestParam List<String> tags,
            @RequestParam String introduction,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = null;
        PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
        pictureUploadRequest.setCategory(category);
        pictureUploadRequest.setTags(tags);
        pictureUploadRequest.setIntroduction(introduction);
        try {
            pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResultUtils.success(pictureVO);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        // 首先判断有没有登录
        User loginUser = userService.getLoginUser(request);
        // 判断要删除的类是否合法
        ThrowUtils.throwIf(deleteRequest.getId()<=0, ErrorCode.PARAMS_ERROR);
        // 判断是不是管理员或者是登录用户进行删除
        Picture oldPicture = pictureService.getById(deleteRequest.getId());
        if(!loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE) && !oldPicture.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 操作数据库
        boolean result = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }
    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        log.info(pictureQueryRequest.getTags().toString());
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 判断是否查询热门数据，从缓存查询
        if(StrUtil.contains(pictureQueryRequest.getTags().toString(),"hot")){
            Page<PictureVO> pictureVOPageFromCache = pictureService.getPictureVOPageFromCache(pictureQueryRequest);
            if(ObjectUtil.isNull(pictureVOPageFromCache)){
                // 查询数据库
                Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                        pictureService.getQueryWrapper(pictureQueryRequest));
                // 获取封装类
                return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
            }
            return  ResultUtils.success(pictureVOPageFromCache);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    @PostMapping("/search")
    public BaseResponse<Page<PictureVO>> searchPicture(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureQueryRequest),ErrorCode.PARAMS_ERROR);
        Page<PictureVO> pictureVOPage = pictureService.searchPicture(pictureQueryRequest, request);
        return ResultUtils.success(pictureVOPage);
    }
    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/private/page/vo")
    public BaseResponse<Page<PictureVO>> listPrivateVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {

        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }
    /**
     * 上传缩略图
     */
//    @PostMapping("/user-upload-thumbnail")
//    public BaseResponse<Picture> userUploadThumbnail(@RequestPart("file") MultipartFile multipartFile) {
//
//        return null;
//    }

}
