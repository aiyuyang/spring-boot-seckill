# 🚀 High-Concurrency Seckill System | 高并发秒杀系统

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green) ![Redis](https://img.shields.io/badge/Redis-Cache-red) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async-blue) ![MySQL](https://img.shields.io/badge/MySQL-Persistence-blue)

## 📖 项目简介 (Introduction)

这是一个基于 **Spring Boot** 构建的高性能、高并发商品秒杀系统。项目完整实现了从用户登录、商品列表缓存、秒杀接口隐藏、库存预减、到异步削峰下单的全流程架构。

核心目标是解决在高并发场景下（如“双11”抢购）常见的**超卖（Over-selling）**、**少卖**以及**数据库宕机**问题。

通过多级缓存（本地标记 + Redis）和消息队列（RabbitMQ）技术，系统实现了**单机 QPS 12,000+** 的吞吐量，并保证了数据的强一致性。

## 🏗️ 核心架构 (Architecture)

系统采用典型的**分层架构**设计，针对秒杀场景进行了深度优化：

1.  **Web层**：Nginx 反向代理 + 全局异常处理。
2.  **网关/安全层**：MD5 双重加密（前端+后端），分布式 Session/Token 验证，秒杀接口地址动态隐藏。
3.  **缓存层 (Redis)**：
    * 对象缓存（User Token）。
    * **库存预热**：秒杀开始前将库存加载至 Redis。
    * **原子性扣减**：使用 **Lua 脚本** 保证 Redis 库存扣减的原子性。
4.  **异步层 (RabbitMQ)**：
    * **流量削峰**：秒杀请求先入队，由消费者异步处理落库，保护数据库。
5.  **持久层 (MySQL)**：MyBatis-Plus 操作，乐观锁/唯一索引防止超卖。

## 🛠️ 技术栈 (Tech Stack)

* **核心框架**: Spring Boot 2.x / 3.x
* **持久层**: MyBatis-Plus
* **中间件**:
    * **Redis**: 缓存、分布式 Session、分布式锁、Lua 脚本库存预减。
    * **RabbitMQ**: 消息队列，实现异步下单。
* **数据库**: MySQL 8.0
* **工具**: JMeter (压测), Lombok, Guava (本地缓存标记), MD5.

## ⚡️ 性能压测报告 (Performance Report)

> 测试环境：MacBook Air M2 (本地单机部署), 10万库存, 10万 Token
> 测试工具：Apache JMeter 5.6.3 (CLI Mode)

本系统经过了严格的压力测试，验证了在不同并发度下的系统表现，并成功定位了系统的**最佳性能点（Sweet Spot）**和**物理瓶颈**。

### 📊 核心数据对比

| 并发线程数 (Threads) | 总吞吐量 (QPS/TPS) | 平均响应时间 (Avg RT) | 错误率 (Error %) | 系统状态 |
| :--- | :--- | :--- | :--- | :--- |
| **100 (最佳)** | **12,704.9 /sec** 👑 | **7 ms** ⚡️ | 0.00% | **最佳性能区**，无排队，资源利用率最高 |
| **1,000** | 9,915.7 /sec | 99 ms | 0.00% | **饱和区**，Tomcat 线程池满，出现排队 |
| **3,000** | 10,001.5 /sec | 291 ms | 0.00% | **过载区**，QPS 封顶，响应时间线性增长 |

### 📈 压测结论 (Insights)
1.  **极高的吞吐量**：单机环境下，通过 Redis 预减库存和内存标记，系统能抗住 **1.2万+ QPS** 的读写压力。
2.  **优雅的过载保护**：在 3000 线程的高压下（远超 Tomcat 默认 200 线程），系统吞吐量稳定在 1万左右，未发生崩溃或错误，体现了良好的鲁棒性。
3.  **数据强一致性**：在 10万次高并发请求结束后，数据库订单数严格等于 10万，Redis 库存归零，**无任何超卖或少卖现象**。

## 💡 核心优化点 (Key Optimizations)

1.  **秒杀地址隐藏 (Get Path)**
    * 用户必须先获取动态生成的随机 Path 才能发起秒杀，防止恶意脚本通过固定 URL 刷接口。
2.  **Redis 预减库存 + Lua 脚本**
    * 将数据库层面的压力转移到 Redis。利用 Lua 脚本保证“读取库存-判断库存-扣减库存”的原子性，避免并发冲突。
3.  **内存标记 (Memory Flag)**
    * 在 JVM 本地维护一个 `Map<Long, Boolean> isOverMap`。一旦商品卖空，直接在本地拦截请求，**连 Redis 都不用查**，进一步减少网络 I/O。
4.  **RabbitMQ 异步下单**
    * Redis 扣减成功后，直接返回“排队中”，将订单消息发送到 MQ。后台消费者慢慢消费消息写库。即便数据库每秒只能写 2000 条，也不会阻塞前端 1万的 QPS。
5.  **解决超卖问题**
    * **Redis层**：Lua 脚本原子性。
    * **DB层**：唯一索引（`user_id` + `activity_id`）防止同一用户重复购买；SQL 语句 `UPDATE ... SET stock = stock - 1 WHERE stock > 0` 利用行锁兜底。

## 🚀 快速开始 (Quick Start)

为了让你能在 5 分钟内运行起本项目，我们提供了 Docker Compose 脚本以一键启动所有依赖服务。

### 1. 环境准备 (Prerequisites)
* **JDK**: 1.8+
* **Maven**: 3.x
* **Docker** & **Docker Compose** (必须)

### 2. 启动中间件 (Start Services)
在项目根目录下创建一个 `docker-compose.yml` 文件，并将以下内容复制进去，然后在终端运行启动命令。

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  # MySQL 服务
  mysql:
    image: mysql:8.0
    container_name: seckill-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: password  # 数据库密码
      MYSQL_DATABASE: seckill_db     # 自动创建的数据库名
    command: --default-authentication-plugin=mysql_native_password
    volumes:
      - ./data/mysql:/var/lib/mysql

  # Redis 服务
  redis:
    image: redis:alpine
    container_name: seckill-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  # RabbitMQ 服务
  rabbitmq:
    image: rabbitmq:management
    container_name: seckill-rabbitmq
    ports:
      - "5672:5672"   # 应用连接端口
      - "15672:15672" # 管理控制台端口
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
````

**启动命令**:

```bash
docker-compose up -d
```

### 3\. 数据库初始化 (Init Database)

服务启动后，需要导入表结构：

1.  使用数据库管理工具（如 Navicat/DBeaver）连接 MySQL。
      * **Host**: `localhost`
      * **Port**: `3306`
      * **User**: `root`
      * **Password**: `password`
2.  连接后，你应该能看到名为 `seckill_db` 的空数据库。
3.  在该数据库下运行项目中的 `src/main/resources/schema.sql` 脚本，创建表并插入测试数据。

### 4\. 修改配置 (Configuration)

确保 `src/main/resources/application.yml` 中的配置与 Docker 设置保持一致：

```yaml
spring:
  datasource:
    # 注意：数据库名已变为 seckill_db
    url: jdbc:mysql://localhost:3306/seckill_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    username: root
    password: password # 对应 docker-compose 里的 MYSQL_ROOT_PASSWORD
  redis:
    host: localhost
    port: 6379
    # password:  # 如果 redis 没有设密码则注释掉
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 5\. 运行项目 (Run)

在项目根目录下（即包含 `pom.xml` 的目录），执行以下命令启动应用：

```bash
mvn spring-boot:run
```

### 6\. 验证 (Verify)

  * **应用访问**: `http://localhost:8080/login/toLogin`
  * **RabbitMQ 控制台**: `http://localhost:15672` (用户/密码: guest/guest)


-----

**Author**: Ai Yuyang
**Contact**: ai.yuyang2024@gmail.com