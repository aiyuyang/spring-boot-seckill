package com.example.seckill_system.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    
    // 从 application.yml 读取配置
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 Token
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
    }

    /**
     * 解析 Token 获取 UserId
     * 如果解析失败（过期、被篡改），会抛出异常
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                                .setSigningKey(secret)
                                .parseClaimsJws(token)
                                .getBody();

            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }


}
