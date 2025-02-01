package com.lifh.nestora.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifh.nestora.exception.BusinessException;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.mapper.SpaceMapper;
import com.lifh.nestora.common.PageRequest;
import com.lifh.nestora.model.dto.file.UploadPictureResult;
import com.lifh.nestora.model.dto.picture.PictureUploadRequest;
import com.lifh.nestora.model.dto.space.SpaceAddRequest;
import com.lifh.nestora.model.dto.space.SpaceQueryRequest;
import com.lifh.nestora.model.entity.Picture;
import com.lifh.nestora.model.entity.Space;
import com.lifh.nestora.model.entity.User;
import com.lifh.nestora.model.enums.SpaceTypeEnum;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.model.vo.SpaceVO;
import com.lifh.nestora.model.vo.UserVO;
import com.lifh.nestora.service.PictureService;
import com.lifh.nestora.service.SpaceService;
import com.lifh.nestora.service.UserService;
import com.lifh.nestora.utils.ImageCompressUtil;
import com.lifh.nestora.utils.MultipartFileCopyUtil;
import com.lifh.nestora.utils.OssUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * @author li_fe
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-01-30 14:03:41
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    UserService userService;

    @Resource
    PictureService pictureService;
    @Resource
    OssUtil ossUtil;
    @Resource
    ImageCompressUtil imageCompressUtil;
    public SpaceVO addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        // 首先检查是否登录
        User loginUser = userService.getLoginUser(request);
        // 检查参数是否异常
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceAddRequest), ErrorCode.PARAMS_ERROR,"参数为空");

        String spaceName = spaceAddRequest.getSpaceName();
        Integer spaceLevel = spaceAddRequest.getSpaceLevel();
        Long userId = spaceAddRequest.getUserId();
        ThrowUtils.throwIf(!userId.equals(loginUser.getId()),ErrorCode.OPERATION_ERROR,"操作用户与登录用户不匹配");

        if (!StrUtil.isNotBlank(spaceName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称错误");
        }
        if (!SpaceTypeEnum.getValues().contains(spaceLevel)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型错误");
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest,space);
        space.setMaxCount(Objects.requireNonNull(SpaceTypeEnum.getEnumByValue(spaceLevel)).getMaxCount());
        space.setMaxSize(Objects.requireNonNull(SpaceTypeEnum.getEnumByValue(spaceLevel)).getMaxSize());
        space.setTotalCount(space.getMaxCount());
        space.setTotalSize(space.getMaxSize());
        boolean save = this.save(space);
        return getSpaceVO(space);
    }

    @Override
    public IPage<SpaceVO> getSpacesByUserId(PageRequest pageRequest, HttpServletRequest request) {
        // 首先检查是否登录
        User loginUser = userService.getLoginUser(request);
        Page<Space> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        Page<Space> resultPage = this.page(page, queryWrapper);
        // 定义一个转换函数
        Function<Space, SpaceVO> mapperFunction = this::getSpaceVO;
        return resultPage.convert(mapperFunction);
    }


    @Override
    public IPage<PictureVO> getPictureBySpaceId(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<Picture> page = new Page<>(spaceQueryRequest.getCurrent(), spaceQueryRequest.getPageSize());
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spaceId", spaceQueryRequest.getSpaceId());
        Page<Picture> resultPage = pictureService.page(page, queryWrapper);
        return pictureService.getPictureVOPage(resultPage, request);
    }
    @Transactional
    @Override
    public SpaceVO uploadPrivatePicture(MultipartFile[] multipartFiles, Long spaceId, HttpServletRequest request) throws IOException {
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOlist = new ArrayList<>();
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);
        Space space = this.getById(spaceId);
        // 剩余的数量和空间大小
        Long totalCount = space.getTotalCount();
        Long totalSize = space.getTotalSize();
        // 首先判断空间是否足够
        Long uploadCount = Arrays.stream(multipartFiles).count();
        Long uploadSize = Arrays.stream(multipartFiles)
                .filter(file -> !file.isEmpty()) // 过滤掉空文件
                .mapToLong(MultipartFile::getSize) // 将文件映射为其大小
                .sum()/1024; // 计算总和
        if(!(totalCount > uploadCount && totalSize > uploadSize)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间不足");
        }
        MultipartFile[] multipartFiles1 = MultipartFileCopyUtil.deepCopyMultipartFiles(multipartFiles);
        List<UploadPictureResult> uploadPictureResults = ossUtil.uploadImages(multipartFiles);
        List<UploadPictureResult> uploadThumbnailResults = imageCompressUtil.uploadThumbnails(multipartFiles1);
        Iterator<UploadPictureResult> iterator1 = uploadPictureResults.iterator();
        Iterator<UploadPictureResult> iterator2 = uploadThumbnailResults.iterator();
        List<Picture> pictures = new ArrayList<>();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            UploadPictureResult uploadPictureResult = iterator1.next();
            UploadPictureResult uploadThumbnailResult = iterator2.next();
            // 构造要入库的图片信息
            Picture picture = new Picture();
            // 上传缩略图
            // 上传图片
            picture.setThumbnailUrl(uploadThumbnailResult.getUrl());
            picture.setUrl(uploadPictureResult.getUrl());
            picture.setName(uploadPictureResult.getPicName());
            picture.setPicSize(uploadPictureResult.getPicSize());
            picture.setPicWidth(uploadPictureResult.getPicWidth());
            picture.setPicHeight(uploadPictureResult.getPicHeight());
            picture.setPicScale(uploadPictureResult.getPicScale());
            picture.setPicFormat(uploadPictureResult.getPicFormat());
            picture.setUserId(loginUser.getId());
            picture.setSpaceId(spaceId);
            pictures.add(picture);
        }

        boolean result = pictureService.saveBatch(pictures);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
        space.setTotalCount(totalCount - uploadCount);
        space.setTotalSize(totalSize - uploadSize);
        this.saveOrUpdate(space);
        return getSpaceVO(space);
    }

    @Override
    public SpaceVO getSpaceVO(Space space) {

        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

}
