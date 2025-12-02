package com.example.seckill_system.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;

@RestController
@RequestMapping("/activity")
public class SeckillActivityController {

    @Resource
    private ISeckillActivityService activityService;

    @PostMapping
    public ResponseEntity<String> createActivity(@RequestBody SeckillActivity activity) {
        activity.setAvailableStock(activity.getInitialStock());
        boolean success = activityService.save(activity);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body("秒杀活动创建成功，ID: " + activity.getId());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("秒杀活动创建失败。");
    }

    @GetMapping("/list")
    public ResponseEntity<List<SeckillActivity>> getActivityList() {
        QueryWrapper<SeckillActivity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("start_time");
        List<SeckillActivity> list = activityService.list(wrapper);

        return ResponseEntity.ok(list);
    }

    @PutMapping
    public ResponseEntity<String> updateActivity(@RequestBody SeckillActivity activity) {
        if (activity.getId() == null) {
            return ResponseEntity.badRequest().body("更新失败：活动ID不能为空。");
        }
        // 注意：正在进行或已开始的活动，一般不允许修改库存或时间。
        // 这里简化处理，直接调用更新。
        boolean success = activityService.updateById(activity);

        if (success) {
            return ResponseEntity.ok("秒杀活动更新成功。");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("秒杀活动更新失败，可能活动ID不存在。");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteActivity(@PathVariable Long activity_id) {
        boolean success = activityService.removeById(activity_id);

        if (success) {
            return ResponseEntity.ok("秒杀活动删除成功。");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("秒杀活动删除失败，可能活动ID不存在。");
    }

    @PostMapping("/warmup/{activity_id}")
    public ResponseEntity<String> warmUpCache(@PathVariable Long activity_id) {
        boolean success = activityService.warmUpCache(activity_id);

        if (success) {
            return ResponseEntity.ok("活动ID: " + activity_id + " 缓存预热成功！");
        } else {
            return ResponseEntity.badRequest().body("活动ID: " + activity_id + " 缓存预热失败，请检查活动是否存在或库存是否大于0。");
        }
    }

    @PostMapping("/doSeckill/{activityId}")
    public String doSeckill(@PathVariable Long activityId, @RequestParam Long userId) {
        boolean success = activityService.doSeckill(userId, activityId);

        if (success) {
            return "恭喜！排队中，正在生成订单...";
        } else {
            return "很遗憾，秒杀失败 (库存不足 或 重复下单)";
        }
    }
}