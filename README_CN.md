# 🚀 高并发秒杀系统 | High-Concurrency Seckill System

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green) ![React](https://img.shields.io/badge/React-19-blue) ![Redis](https://img.shields.io/badge/Redis-Cache-red) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async-blue) ![MySQL](https://img.shields.io/badge/MySQL-Persistence-blue) ![Docker](https://img.shields.io/badge/Docker-Container-blue)

## 📖 项目简介

这是一个完整的高并发秒杀系统，包含**后端服务**和**前端界面**。系统采用 Spring Boot + React 技术栈，通过 Redis 缓存、RabbitMQ 消息队列等技术，实现了**单机 QPS 12,000+** 的高性能秒杀功能。

### 核心特性

- ✅ **防超卖**：Redis Lua 脚本原子操作 + MySQL 唯一索引
- ✅ **高并发**：Redis 预减库存 + 本地缓存标记 + MQ 异步削峰
- ✅ **精确倒计时**：客户端与服务器时间同步
- ✅ **容器化部署**：Docker Compose 一键启动
- ✅ **前后端分离**：React + Spring Boot RESTful API

## 🏗️ 系统架构

```
┌─────────────┐
│   前端      │  React + Vite + Ant Design
│  (Port 80)  │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────┐
│   后端      │  Spring Boot
│ (Port 8081) │
└──────┬──────┘
       │
   ┌───┴───┬──────────┬──────────┐
   ▼       ▼          ▼          ▼
┌─────┐ ┌──────┐  ┌────────┐  ┌──────┐
│Redis│ │Rabbit│  │ MySQL  │  │Nginx │
│6379 │ │ MQ  │  │  3306  │  │      │
└─────┘ └──────┘  └────────┘  └──────┘
```

### 技术栈

**后端**：
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- Redis (Lettuce)
- RabbitMQ
- MySQL 8.0
- JWT 认证

**前端**：
- React 19
- Vite 7
- Ant Design 6
- Axios
- React Router

**部署**：
- Docker & Docker Compose
- Nginx

## 🚀 快速开始

### 方式一：Docker Compose 一键启动（推荐）

```bash
# 1. 启动所有服务
./start.sh

# 或手动执行
docker-compose up -d

# 2. 访问应用
# 前端: http://localhost
# 后端API: http://localhost:8081
# Swagger: http://localhost:8081/swagger-ui.html
# RabbitMQ: http://localhost:15672 (guest/guest)
```

### 方式二：本地开发运行

详见 [RUN.md](./RUN.md)

## 📁 项目结构

```
spring-boot-seckill/
├── backend/                 # 后端服务
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Java 源码
│   │       └── resources/  # 配置文件
│   ├── Dockerfile          # 后端容器镜像
│   └── pom.xml
├── frontend/               # 前端应用
│   ├── src/
│   │   ├── pages/         # 页面组件
│   │   ├── components/    # 公共组件
│   │   ├── api/          # API 接口
│   │   └── utils/        # 工具函数
│   ├── Dockerfile         # 前端容器镜像
│   └── package.json
├── docker-compose.yml     # 容器编排配置
├── start.sh              # 一键启动脚本
└── README.md            # 项目文档
```

## 📚 文档目录

- [架构文档](./documents/ARCHITECTURE_CN.md) - 系统架构详细说明
- [API 文档](./documents/API_DOCUMENTATION_CN.md) - 完整的 API 接口文档
- [项目总结](./documents/PROJECT_SUMMARY_CN.md) - 完整的项目复盘报告
- [后端 README](./backend/README.md) - 后端详细文档

## 🎯 核心功能

### 1. 用户认证
- JWT Token 认证
- 模拟登录（输入用户ID即可）

### 2. 活动管理
- 活动列表展示
- 活动详情查看
- 活动状态（未开始/进行中/已结束）

### 3. 秒杀功能
- 精确倒计时（与服务器时间同步）
- 图形验证码防刷
- 秒杀Token隐藏（验证码校验后获取Token）
- 异步下单（MQ 削峰）
- 订单结果轮询

### 4. 性能优化
- Redis 预减库存（Lua 脚本原子操作）
- 本地缓存标记（JVM 内存）
- RabbitMQ 异步削峰
- 连接池优化

## ⚡ 性能指标

| 并发线程数 | QPS | 平均响应时间 | 错误率 |
|-----------|-----|------------|--------|
| 100 | 12,704.9 | 7ms | 0.00% |
| 1,000 | 9,915.7 | 99ms | 0.00% |
| 3,000 | 10,001.5 | 291ms | 0.00% |

**数据一致性**：10万次并发请求，0 超卖，0 少卖 ✅

## 🔧 开发环境

- JDK 17+
- Maven 3.6+
- Node.js 18+
- Docker & Docker Compose

## 📝 API 文档

启动后端服务后，访问 Swagger UI：
- http://localhost:8081/swagger-ui.html

主要接口：
- `POST /auth/login` - 用户登录
- `GET /activity/list` - 获取活动列表
- `GET /activity/{id}` - 获取活动详情
- `GET /seckill/captcha` - 获取验证码
- `GET /seckill/token/{activityId}` - 获取秒杀Token
- `POST /seckill/doSeckill/{activityId}/{token}` - 执行秒杀
- `GET /order/result/{activityId}` - 查询订单结果

## 🐛 常见问题

详见 [RUN.md](./RUN.md) 中的"常见问题"章节

## 📄 许可证

本项目仅用于学习和研究目的。

## 👤 作者

**Ai Yuyang**  
Email: ai.yuyang2024@gmail.com

---

**⭐ 如果这个项目对你有帮助，请给个 Star！**
