package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.SeckillActivity;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {
    int decreaseStock(@Param("activity_id") Long activity_id);

}
