package com.example.seckill_system;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;

@SpringBootTest
class SeckillSystemApplicationTests {

	@Autowired
	private ISeckillActivityService seckillActivityService;

	@Test
	void contextLoads() {
	}

	@Test
	void testDatabaseConnection() {
		List<SeckillActivity> list = seckillActivityService.list();
	}
}
