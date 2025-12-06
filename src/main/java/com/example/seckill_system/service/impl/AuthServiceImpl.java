package com.example.seckill_system.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.seckill_system.service.IAuthService;
import com.example.seckill_system.utils.JwtUtil;
import com.example.seckill_system.vo.RespBean;
import com.example.seckill_system.vo.RespBeanEnum;

@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public RespBean login(Long userId, String password) {
        // 1. 校验逻辑 (查数据库，比对密码 MD5)
        if (userId == null) return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        
        // 2. 生成 Token
        String token = jwtUtil.generateToken(userId);

        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("tokenHead", "Bearer ");

        return RespBean.success(map);
    }

    @Override
    public RespBean register(Long userId, String password) {
        // TODO: 插入数据库 user 表，注意密码要加密存储
        return RespBean.success("注册成功（模拟）");
    }

    @Override
    public RespBean logout() {
        // JWT 是无状态的，服务端没法直接让它失效。
        // 真正的登出通常有三种做法：
        // 1. 前端：直接丢弃 Token（最简单）。
        // 2. 后端：把当前的 Token 存入 Redis 黑名单，设置过期时间为 Token 的剩余时间。拦截器里查一下黑名单。
        // 3. 版本号：数据库记录用户 token_version，登录+1，Token里带版本，不一致则失效。
        
        return RespBean.success("登出成功（请前端删除Token）");
    }
   
}
