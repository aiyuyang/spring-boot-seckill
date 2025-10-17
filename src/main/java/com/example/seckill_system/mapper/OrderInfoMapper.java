package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.OrderInfo;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    
}
