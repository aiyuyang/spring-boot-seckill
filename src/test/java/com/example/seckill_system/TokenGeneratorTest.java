package com.example.seckill_system;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.seckill_system.utils.JwtUtil;

@SpringBootTest
public class TokenGeneratorTest {
    
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void generateTokens() throws IOException {
        int userCount = 100000;
        File file = new File("/Users/aiyuyang/Desktop/tokens.txt");

        if (file.exists()) file.delete();

        FileWriter writer = new FileWriter(file);

        for (int i = 0; i < userCount; i++) {
            Long userId = 5000L + i;
            String token = jwtUtil.generateToken(userId);

            writer.write(token + "\n");
        }

        writer.close();
        System.out.println("Token 生成完毕，路径: " + file.getAbsolutePath());
    }
}
