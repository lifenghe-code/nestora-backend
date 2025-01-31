package com.lifh.nestora.job.cycle;

import cn.hutool.json.JSONUtil;
import com.lifh.nestora.model.entity.Picture;
import com.lifh.nestora.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将redis中的图片查看次数数据持久化到数据库中
 */

@Component
@Slf4j
public class CyclePictureViewsToMysql {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PictureService pictureService;

    /**
     * 每1分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000 )
    //
    public void run() {
        // 先读取redis中的数据
        RScoredSortedSet<String> zSet = redissonClient.getScoredSortedSet("image:views");       //redissonClient.
        // 输出所有元素的分数
        Collection<ScoredEntry<String>> entries = zSet.entryRangeReversed(0, -1);
        List<Picture> updateData = new ArrayList<>();
        for (ScoredEntry<String> entry : entries) {
            System.out.println("元素: " + entry.getValue() + ", 分数: " + entry.getScore());
            Picture picture = new Picture();
            picture.setId(Long.valueOf(entry.getValue())); // 图像 ID
            picture.setView(entry.getScore().intValue()); // 浏览次数
            if(picture.getView() > 5){
                String tags = picture.getTags();
                List<String> list = JSONUtil.toList(tags, String.class);
                if(!list.contains("hot")){
                    list.add("hot");
                    picture.setTags(list.toString());
                }
            }
            updateData.add(picture);
        }
        pictureService.updateBatchById(updateData);
        log.info("完成数据持久化操作");
    }
}
