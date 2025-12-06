package com.example.seckill_system.config;

import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> rateLimitScript;

    @PostConstruct
    public void init() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limit.lua")));
        rateLimitScript.setResultType(Long.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);

            if (accessLimit == null) {
                return true;
            }

            int seconds = accessLimit.second();
            int maxCount = accessLimit.maxCount();

            String userId = request.getParameter("userId");
            // todo 1
        }
        
        
        return true;
    }

    private void render(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // todo 这里简单写死 JSON 格式，之后建议配合 Result 对象
        out.write("{\"code\": 500, \"msg\": \"" + msg + "\"}");
        out.flush();
        out.close();
    }
}
