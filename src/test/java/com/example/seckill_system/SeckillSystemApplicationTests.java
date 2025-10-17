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
		System.out.println("Starting querying database...");
		List<SeckillActivity> list = seckillActivityService.list();
		System.out.println("Querying complete.");
		System.out.println("Result: " + list);
	}
}
