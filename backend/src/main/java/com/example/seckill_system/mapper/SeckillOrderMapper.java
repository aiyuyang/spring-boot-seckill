package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.SeckillOrder;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    // Use MyBatis-Plus BaseMapper's built-in methods with QueryWrapper
    // No need for custom SQL methods
}