package com.lifh.nestora.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Value("${spring.cache.caffeine.maximumSize}")
    int maximumSize;
    @Value("${spring.cache.caffeine.expireAfterAccess}")
    int expireAfterAccess;

    private String cacheName = "picture";
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(cacheName);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maximumSize)       // 最大缓存条数
                .expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS));  // 过期时间
        return cacheManager;
    }


}

