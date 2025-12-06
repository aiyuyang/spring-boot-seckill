package com.example.seckill_system.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 定义白名单 (不需要登录就能访问的接口)
        List<String> excludePaths = new ArrayList<>();
        excludePaths.add("/auth/**");     // 登陆接口
        excludePaths.add("/seckill/list");   // 假设有个活动列表页是公开的
        excludePaths.add("/favicon.ico");    // 浏览器图标
        excludePaths.add("/error");          // Spring Boot 默认错误页

        // 👇👇👇 Swagger 专用白名单 👇👇👇
        excludePaths.add("/swagger-ui/**");      // UI页面
        excludePaths.add("/v3/api-docs/**");     // API JSON数据
        excludePaths.add("/swagger-resources/**"); // 资源文件
        excludePaths.add("/webjars/**");         // 静态资源

        // 2. 注册认证拦截器 (Auth)
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths);

        // 3. 注册限流拦截器 (Limit)
        registry.addInterceptor(accessLimitInterceptor)
                .addPathPatterns("/**");
    }
}
