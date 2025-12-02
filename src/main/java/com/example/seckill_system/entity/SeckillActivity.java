package com.example.seckill_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;

    private Long productId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer initialStock;
    
    private Integer availableStock;

    private LocalDateTime createTime;
}
