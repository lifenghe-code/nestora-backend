package com.lifh.nestora.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lifh.nestora.annotation.LoginCheck;
import com.lifh.nestora.constant.UserConstant;
import com.lifh.nestora.exception.BusinessException;
import com.lifh.nestora.exception.ErrorCode;
import com.lifh.nestora.model.entity.User;
import com.lifh.nestora.model.vo.LoginUserVO;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断是否是方法级别的处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查方法或类上是否有 @LoginCheck 注解
        LoginCheck loginCheck = method.getAnnotation(LoginCheck.class);
        if (loginCheck == null) {
            loginCheck = handlerMethod.getBeanType().getAnnotation(LoginCheck.class);
        }

        // 如果有 @LoginCheck 注解，则检查用户是否登录
        if (loginCheck != null) {
            // 这里实现你的登录检查逻辑
            LoginUserVO loginUserVO = checkUserLogin(request);
            return !ObjectUtil.isNull(loginUserVO);
        }

        return true;
    }

    private LoginUserVO checkUserLogin(HttpServletRequest request) {
        // 实现你的登录检查逻辑，例如从 Session 或 Token 中获取用户信息
        // 这里假设用户登录信息存储在 Session 中
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(currentUser, loginUserVO);
        return loginUserVO;

    }
}
