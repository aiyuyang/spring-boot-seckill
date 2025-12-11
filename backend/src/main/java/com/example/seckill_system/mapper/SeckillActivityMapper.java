package com.example.seckill_system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill_system.entity.SeckillActivity;

import io.lettuce.core.dynamic.annotation.Param;

/**
 * Seckill Activity Mapper Interface
 * 
 * <p>This interface extends MyBatis-Plus's {@link BaseMapper} to provide
 * standard database operations for {@link SeckillActivity} entities.</p>
 * 
 * <p><b>Inherited Methods</b>:</p>
 * <p>By extending {@link BaseMapper}, this interface automatically provides:</p>
 * <ul>
 *   <li>{@code insert(SeckillActivity entity)}: Insert new activity</li>
 *   <li>{@code selectById(Long id)}: Query activity by ID</li>
 *   <li>{@code updateById(SeckillActivity entity)}: Update activity by ID</li>
 *   <li>{@code deleteById(Long id)}: Delete activity by ID</li>
 *   <li>{@code selectList(Wrapper wrapper)}: Query activities with conditions</li>
 *   <li>And many more utility methods</li>
 * </ul>
 * 
 * <p><b>Custom Methods</b>:</p>
 * <p>This interface can be extended with custom SQL methods if needed.
 * For example, the {@code decreaseStock} method could be used for atomic
 * stock reduction, though the current implementation uses MyBatis-Plus's
 * {@code UpdateWrapper} for this purpose.</p>
 * 
 * <p><b>MyBatis-Plus Integration</b>:</p>
 * <p>MyBatis-Plus automatically generates SQL implementations for all
 * {@link BaseMapper} methods, eliminating the need for XML mapper files
 * for standard CRUD operations.</p>
 * 
 * @author Ai Yuyang
 * @see com.baomidou.mybatisplus.core.mapper.BaseMapper
 * @see SeckillActivity
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {
    
    /**
     * Decreases the stock count for the specified activity.
     * 
     * <p><b>Note</b>: This method is defined but may not be actively used.
     * The current implementation uses MyBatis-Plus's {@code UpdateWrapper}
     * for stock reduction in {@link com.example.seckill_system.service.impl.SeckillOrderServiceImpl}.</p>
     * 
     * <p><b>Alternative Implementation</b>:</p>
     * <p>If this method is used, it should be implemented with a conditional update:</p>
     * <pre>{@code
     * UPDATE seckill_activity 
     * SET available_stock = available_stock - 1 
     * WHERE id = #{activity_id} AND available_stock > 0
     * }</pre>
     * 
     * @param activity_id The activity ID to decrease stock for
     * @return Number of affected rows (1 if successful, 0 if stock is already 0)
     */
    int decreaseStock(@Param("activity_id") Long activity_id);
}
