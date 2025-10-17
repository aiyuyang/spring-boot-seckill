package com.example.seckill_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.seckill_system.mapper")
public class SeckillSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeckillSystemApplication.class, args);
	}

}
