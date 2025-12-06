package com.example.seckill_system.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.seckill_system.utils.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 模拟登录接口 (实际项目中会校验用户名密码)
     * POST /login?userId=1001
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam Long userId) {
        // 1. 假设数据库校验用户名密码通过...
        
        // 2. 颁发 Token
        String token = jwtUtil.generateToken(userId);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return result;
    }
}
