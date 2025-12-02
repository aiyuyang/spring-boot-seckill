package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.SeckillOrder;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder>{
    SeckillOrder selectByUser_idAndActivity_id(@Param("user_id") Long user_id, @Param("activity_id") Long activity_id);
}