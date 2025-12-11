package com.example.seckill_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill_system.entity.SeckillActivity;

/**
 * Seckill Activity Service Interface
 * 
 * <p>This interface extends MyBatis-Plus's {@link IService} to provide
 * standard CRUD operations for {@link SeckillActivity} entities.</p>
 * 
 * <p><b>Inherited Methods</b>:</p>
 * <p>By extending {@link IService}, this interface automatically provides:</p>
 * <ul>
 *   <li>{@code getById(Long id)}: Query activity by ID</li>
 *   <li>{@code save(SeckillActivity entity)}: Create new activity</li>
 *   <li>{@code updateById(SeckillActivity entity)}: Update existing activity</li>
 *   <li>{@code removeById(Long id)}: Delete activity by ID</li>
 *   <li>{@code list()}: Query all activities</li>
 *   <li>And many more utility methods</li>
 * </ul>
 * 
 * <p><b>Usage</b>:</p>
 * <p>This service is primarily used for:</p>
 * <ul>
 *   <li>Querying activity details from the database</li>
 *   <li>Managing activity lifecycle (create, update, delete)</li>
 *   <li>Providing data for activity preheating operations</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.service.impl.SeckillActivityServiceImpl
 * @see com.baomidou.mybatisplus.extension.service.IService
 */
public interface ISeckillActivityService extends IService<SeckillActivity>{
    
}
