package com.example.seckill_system.service;

import com.example.seckill_system.vo.RespBean;

public interface IAuthService {
    RespBean login(Long userId, String password);
    RespBean register(Long userId, String password);
    RespBean logout();
}
