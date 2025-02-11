package com.lifh.nestora;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("com.lifh.nestora.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching //启用缓存
@EnableScheduling //定时任务
public class NestoraBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestoraBackendApplication.class, args);
    }

}
