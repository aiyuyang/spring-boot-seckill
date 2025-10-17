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

    private Long product_id;

    private LocalDateTime start_time;

    private LocalDateTime end_time;

    private BigDecimal original_price;

    private BigDecimal seckill_price;

    private Integer initial_stock;

    private LocalDateTime create_time;
}
