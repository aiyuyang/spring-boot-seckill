package com.example.seckill_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("order_info")
public class OrderInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long user_id;

    private Long product_id;

    private BigDecimal order_price;

    private Integer order_status;

    private LocalDateTime create_time;
}
