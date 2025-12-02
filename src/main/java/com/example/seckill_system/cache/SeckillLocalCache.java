package com.example.seckill_system.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SeckillLocalCache {
    private static final Map<Long, Boolean> SOLD_OUT_MAP = new ConcurrentHashMap<>();

    public static boolean isSoldOut(Long activity_id) {
        return SOLD_OUT_MAP.getOrDefault(activity_id, false);
    }

    public static void setSoldOut(Long activity_id) {
        SOLD_OUT_MAP.put(activity_id, true);
    }

    public static void clearSoldOutMark(Long actiity_id) {
        SOLD_OUT_MAP.remove(actiity_id);
    }

    public static void initMark(Long activity_id, boolean isSoldOut) {
        SOLD_OUT_MAP.put(activity_id, isSoldOut);
    }
}
