package com.example.seckill_system.controller;

import com.example.seckill_system.config.AccessLimit;
import com.example.seckill_system.config.UserContext;
import com.example.seckill_system.service.ISeckillService;
import com.example.seckill_system.vo.RespBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/seckill")
@Tag(name = "秒杀核心模块")
public class SeckillController {

    @Autowired
    private ISeckillService seckillService;

    @Operation(summary = "1. 获取验证码")
    @GetMapping("/captcha")
    public void verifyCode(@RequestParam Long activityId, HttpServletResponse response) {
        Long userId = UserContext.getUser();
        seckillService.getCaptcha(userId, activityId, response);
    }

    @Operation(summary = "2. 获取秒杀地址")
    @AccessLimit(second = 5, maxCount = 5)
    @GetMapping("/path/{activityId}")
    public RespBean getSeckillPath(@PathVariable Long activityId, @RequestParam String verifyCode) {
        Long userId = UserContext.getUser();
        
        // 【修改】直接调用 Service，传入 verifyCode
        // Service 内部会校验验证码，错误则抛异常，正确则返回 path
        return seckillService.getSeckillPath(userId, activityId, verifyCode);
    }

    @Operation(summary = "3. 执行秒杀")
    @AccessLimit(second = 5, maxCount = 5)
    @PostMapping("/{path}/doSeckill/{activityId}")
    public RespBean doSeckill(@PathVariable String path, @PathVariable Long activityId) {
        Long userId = UserContext.getUser();

        // 【修改】直接调用 Service，传入 path
        // Service 内部会校验 path，错误则抛异常，正确则执行秒杀
        return seckillService.doSeckill(userId, activityId, path);
    }
}