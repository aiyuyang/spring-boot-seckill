package com.example.seckill_system.common;

public enum SeckillStatusEnum {
    SUCCESS(0, "秒杀成功，订单正在处理中"),
    QUEUEING(1, "秒杀请求正在排队处理中"),
    SOLD_OUT(2, "秒杀失败，库存不足"),
    REPEATED_ORDER(3, "秒杀失败，请勿重复下单"),
    NOT_START(4, "秒杀失败，活动未开始"),
    FINISHED(5, "秒杀失败，活动已结束"),
    ERROR(6, "秒杀失败，系统异常");

    private final int code;
    private final String msg;

    SeckillStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
}
