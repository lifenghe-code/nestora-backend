package com.lifh.nestora.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求响应日志 AOP
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 **/
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    @Resource
    RedissonClient redissonClient;


    // 定义切入点，拦截 PictureController 中的 getPictureVOById 方法
    @Pointcut("execution(public * com.lifh.nestora.controller.PictureController.getPictureVOById(..))")
    public void pictureApiMethod() {}

    // 在方法执行之前触发
    @Before("pictureApiMethod()")
    public void beforeMethod(JoinPoint joinPoint) {
        // 可以在这里记录一些额外的信息，如方法参数等
        // System.out.println("即将调用方法: " + joinPoint.getSignature().getName());
    }

    // 在方法成功执行后触发
    @AfterReturning("pictureApiMethod()")
    public void afterMethod(JoinPoint joinPoint) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        String queryString = httpServletRequest.getQueryString();
        Map<String, String> stringStringMap = convertToMap(queryString);
        Long picId = Long.valueOf(stringStringMap.get("id"));

        RScoredSortedSet<String> zSet = redissonClient.getScoredSortedSet("image:views");
        boolean id = zSet.contains(stringStringMap.get("id"));
        if(id) {
            zSet.addScore(stringStringMap.get("id"), 1);
            log.info(String.valueOf(zSet.getScore(stringStringMap.get("id"))));
        }
        else{
            zSet.add(1,stringStringMap.get("id"));
        }
    }

    // 如果需要统计失败的调用（例如抛出异常时），可以使用 @AfterThrowing
    @AfterThrowing("pictureApiMethod()")
    public void afterMethodFailure(JoinPoint joinPoint) {
        System.out.println("方法 " + joinPoint.getSignature().getName() + " 执行失败。");
    }



    public Map<String, String> convertToMap(String queryString) {
        Map<String, String> map = new HashMap<String, String> ();
        List<String> splits = StrUtil.split(queryString, "&");
        // 分割查询字符串按&分割成多个键值对


        for (String split : splits) {
            // 分割每一个键值对
            List<String> split1 = StrUtil.split(split, '=');
            if (split1.size() == 2) {
                map.put(split1.get(0), split1.get(1));
            }
        }

        return map;
    }

}

