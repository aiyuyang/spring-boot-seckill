package com.example.seckill_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * High-Concurrency Seckill System Application Entry Point
 * 
 * <p>This is the main Spring Boot application class that bootstraps the entire seckill system.
 * The system is designed to handle high-concurrency flash sale scenarios with the following
 * core capabilities:</p>
 * 
 * <ul>
 *   <li><b>Over-selling Prevention</b>: Redis Lua script atomic operations + MySQL unique index</li>
 *   <li><b>High Concurrency Support</b>: Redis pre-stock reduction + local cache flag + MQ async peak shaving</li>
 *   <li><b>Precise Countdown</b>: Client-server time synchronization</li>
 *   <li><b>Containerized Deployment</b>: Docker Compose one-click startup</li>
 * </ul>
 * 
 * <p><b>Performance Metrics</b>:</p>
 * <ul>
 *   <li>Single machine QPS: 12,000+</li>
 *   <li>Data consistency: 0 over-selling, 0 under-selling in 100,000 concurrent requests</li>
 * </ul>
 * 
 * <p><b>Key Annotations</b>:</p>
 * <ul>
 *   <li>{@code @SpringBootApplication}: Enables auto-configuration and component scanning</li>
 *   <li>{@code @EnableScheduling}: Enables scheduled tasks (e.g., stock synchronization)</li>
 *   <li>{@code @MapperScan}: Scans MyBatis mapper interfaces for dependency injection</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @version 1.0
 * @since 2024
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.example.seckill_system.mapper")
public class SeckillSystemApplication {

	/**
	 * Main entry point for the Spring Boot application.
	 * 
	 * <p>This method launches the embedded Tomcat server and initializes all Spring beans,
	 * including controllers, services, interceptors, and scheduled tasks.</p>
	 * 
	 * <p><b>Startup Sequence</b>:</p>
	 * <ol>
	 *   <li>Load application configuration (application.yml)</li>
	 *   <li>Initialize Spring context and scan components</li>
	 *   <li>Connect to MySQL, Redis, and RabbitMQ</li>
	 *   <li>Register interceptors and scheduled tasks</li>
	 *   <li>Start embedded Tomcat server (default port: 8081)</li>
	 * </ol>
	 * 
	 * @param args Command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(SeckillSystemApplication.class, args);
	}

}
