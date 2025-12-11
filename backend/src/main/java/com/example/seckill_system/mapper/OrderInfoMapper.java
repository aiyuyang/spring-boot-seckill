package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.OrderInfo;

/**
 * Order Info Mapper Interface
 * 
 * <p>This interface extends MyBatis-Plus's {@link BaseMapper} to provide
 * standard database operations for {@link OrderInfo} entities.</p>
 * 
 * <p><b>Inherited Functionality</b>:</p>
 * <p>By extending {@link BaseMapper}, this interface automatically provides
 * all standard CRUD operations without requiring XML mapper files or custom SQL.
 * MyBatis-Plus generates the SQL implementations at runtime.</p>
 * 
 * <p><b>Usage</b>:</p>
 * <p>This mapper is primarily used by:</p>
 * <ul>
 *   <li>{@link com.example.seckill_system.service.impl.SeckillOrderServiceImpl}:
 *       For creating order records during seckill operations</li>
 *   <li>{@link com.example.seckill_system.controller.OrderController}:
 *       For querying order details and results</li>
 * </ul>
 * 
 * <p><b>MyBatis-Plus Features</b>:</p>
 * <p>All standard operations are available:</p>
 * <ul>
 *   <li>{@code insert(OrderInfo entity)}: Create new order</li>
 *   <li>{@code selectById(Long id)}: Query order by ID</li>
 *   <li>{@code selectList(Wrapper wrapper)}: Query orders with conditions</li>
 *   <li>{@code updateById(OrderInfo entity)}: Update order by ID</li>
 *   <li>And many more utility methods</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see com.baomidou.mybatisplus.core.mapper.BaseMapper
 * @see OrderInfo
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    
}
