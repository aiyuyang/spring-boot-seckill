package com.example.seckill_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillService;
import com.example.seckill_system.vo.RespBean;
import com.example.seckill_system.vo.RespBeanEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
@Tag(name = "活动管理模块")
public class SeckillActivityController {

    @Autowired
    private ISeckillActivityService seckillActivityService;
    
    @Autowired
    private ISeckillService seckillService;

    @Operation(summary = "创建活动")
    @PostMapping
    public RespBean createActivity(@RequestBody SeckillActivity activity) {
        activity.setAvailableStock(activity.getInitialStock());
        boolean success = seckillActivityService.save(activity);
        return success ? RespBean.success() : RespBean.error(RespBeanEnum.ERROR);
    }

    @Operation(summary = "活动列表")
    @GetMapping("/list")
    public RespBean getActivityList() {
        QueryWrapper<SeckillActivity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("start_time");
        List<SeckillActivity> list = seckillActivityService.list(wrapper);
        return RespBean.success(list);
    }

    @Operation(summary = "发布/预热活动")
    @PostMapping("/publish/{activityId}")
    public RespBean publishActivity(@PathVariable Long activityId) {
        // Service 中会抛出 GlobalException，这里不需要 if-else
        seckillService.prepareSeckill(activityId);
        return RespBean.success("活动发布成功，缓存已预热");
    }
    
    // ... update 和 delete 同样改为返回 RespBean ...
}