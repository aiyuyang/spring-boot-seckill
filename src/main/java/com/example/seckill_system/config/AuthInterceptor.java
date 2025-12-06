package com.example.seckill_system.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.seckill_system.utils.JwtUtil;

@Component
public class AuthInterceptor implements HandlerInterceptor{
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从 Header 获取 Token
        // 标准格式是: Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.isEmpty(authHeader)) {
            renderAuthError(response);
            return false;
        }

        // 处理 Bearer 前缀 (有些前端库会带，有些不带，为了兼容做个处理)
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // 2. 解析 Token
        Long userId = jwtUtil.getUserIdFromToken(token);

        if (userId == null) {
            renderAuthError(response);
            return false;
        }

        // 3. 存入 ThreadLocal
        UserContext.setUser(userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }

    private void renderAuthError(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 401, \"msg\": \"无效的令牌，请重新登陆\"}");
        
    }
}
