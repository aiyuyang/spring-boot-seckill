package com.example.seckill_system.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    private Long userId;
    private Long activityId;
}
