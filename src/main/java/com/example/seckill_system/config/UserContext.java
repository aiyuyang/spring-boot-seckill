package com.example.seckill_system.config;

public class UserContext {
    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    public static void setUser(Long userId) {
        userHolder.set(userId);
    }

    public static Long getUser() {
        return userHolder.get();
    }

    public static void removeUser() {
        userHolder.remove();
    }
}
