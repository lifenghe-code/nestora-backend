package com.lifh.nestora.job.cycle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.model.entity.Picture;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

//@Component
@Slf4j
public class CycleHotPictureToCaffeine {
    @Resource
    private CacheManager cacheManager;
    @Resource
    private PictureService pictureService;
    /**
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000 )

    public void run() {
        List<Picture> pictureList = pictureService.list();
        if (CollUtil.isEmpty(pictureList)) {
            return;
        }
        // 只缓存热门图片
        List<Picture> hotPicture = pictureList.stream().filter(obj -> StrUtil.containsAny(obj.getTags(), "hot"))
                .collect(Collectors.toList());

        List<PictureVO> hotPictureVo = hotPicture.stream().map(obj -> pictureService.getPictureVO(obj)).collect(Collectors.toList());
        Cache cache = cacheManager.getCache("picture");
        ThrowUtils.throwIf(ObjectUtil.isNull(cache), ErrorCode.SYSTEM_ERROR);
        for(PictureVO pictureVO:hotPictureVo){
            cache.putIfAbsent(pictureVO.getCategory() +"-"+ StrUtil.toString(pictureVO.getId()),pictureVO);
        }
        log.info("CycleHotPictureToCaffeine end, total {}", hotPictureVo.size());

    }
}
