package com.lifh.nestora.job.once;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.exception.ThrowUtils;
import com.lifh.nestora.model.entity.Picture;
import com.lifh.nestora.model.vo.PictureVO;
import com.lifh.nestora.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * CommandLineRunner 的作用
 * 执行启动时的操作：它会在 Spring Boot 应用启动完成之后被调用。通常用于做一些初始化工作，如连接外部服务、预加载数据、设置应用程序环境等。
 * 接受命令行参数：run 方法的参数是命令行传入的参数，可以在启动应用时传递一些配置给应用程序。这在开发一些需要命令行输入的功能时非常有用。
 * 用于执行自定义的代码逻辑：你可以将任何启动时需要执行的逻辑放到 run 方法中，如启动数据同步、定时任务、应用健康检查等。
 */
@Component
@Slf4j
public class OnceHotPictureToCaffeine implements CommandLineRunner {
    @Resource
    private CacheManager cacheManager;
    @Resource
    private PictureService pictureService;


    @Override
    public void run(String... args) {

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
        log.info("OnceHotPictureToCaffeine end, total {}", hotPictureVo.size());
    }

    @CachePut(value = "items", key = "#key")
    public String updateCache(String key, String value) {
        // 更新逻辑（可以是更新数据库等）
        return value;
    }

}
