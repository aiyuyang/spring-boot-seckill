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

    private Long userId;

    private Long productId;

    private BigDecimal orderPrice;

    private Integer orderStatus;

    private LocalDateTime createTime;
}
