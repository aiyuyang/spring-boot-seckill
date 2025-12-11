# 🚀 High-Concurrency Seckill System

[![中文文档](https://img.shields.io/badge/Docs-中文-red.svg)](README_CN.md)

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green) ![React](https://img.shields.io/badge/React-19-blue) ![Redis](https://img.shields.io/badge/Redis-Cache-red) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async-blue) ![MySQL](https://img.shields.io/badge/MySQL-Persistence-blue) ![Docker](https://img.shields.io/badge/Docker-Container-blue)

## 📖 Introduction

A complete high-concurrency seckill (flash sale) system with both **backend service** and **frontend interface**. Built with Spring Boot + React, leveraging Redis caching, RabbitMQ message queue, and other technologies to achieve **12,000+ QPS** on a single machine.

### Core Features

- ✅ **Over-selling Prevention**: Redis Lua script atomic operations + MySQL unique index
- ✅ **High Concurrency**: Redis pre-stock reduction + local cache flag + MQ async peak shaving
- ✅ **Precise Countdown**: Client-server time synchronization
- ✅ **Containerized Deployment**: Docker Compose one-click startup
- ✅ **Frontend-Backend Separation**: React + Spring Boot RESTful API

## 🏗️ System Architecture

```
┌─────────────┐
│  Frontend   │  React + Vite + Ant Design
│  (Port 80)  │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────┐
│  Backend    │  Spring Boot
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

### Tech Stack

**Backend**:
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- Redis (Lettuce)
- RabbitMQ
- MySQL 8.0
- JWT Authentication

**Frontend**:
- React 19
- Vite 7
- Ant Design 6
- Axios
- React Router

**Deployment**:
- Docker & Docker Compose
- Nginx

## 🚀 Quick Start

### Option 1: Docker Compose One-Click Startup (Recommended)

```bash
# 1. Start all services
./start.sh

# Or manually
docker-compose up -d

# 2. Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8081
# Swagger: http://localhost:8081/swagger-ui.html
# RabbitMQ: http://localhost:15672 (guest/guest)
```

### Option 2: Local Development

See [documents/API_DOCUMENTATION_CN.md](documents/API_DOCUMENTATION_CN.md) for detailed deployment and running instructions.

## 📁 Project Structure

```
spring-boot-seckill/
├── backend/                 # Backend service
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Java source code
│   │       └── resources/  # Configuration files
│   ├── Dockerfile          # Backend container image
│   └── pom.xml
├── frontend/               # Frontend application
│   ├── src/
│   │   ├── pages/         # Page components
│   │   ├── components/    # Common components
│   │   ├── api/          # API interfaces
│   │   └── utils/        # Utility functions
│   ├── Dockerfile         # Frontend container image
│   └── package.json
├── docker-compose.yml     # Container orchestration config
├── start.sh              # One-click startup script
└── README.md             # Project documentation
```

## 📚 Documentation

- [Chinese Documentation](README_CN.md) - Full Chinese documentation
- [Architecture Document](documents/ARCHITECTURE_CN.md) - System architecture details
- [API Documentation](documents/API_DOCUMENTATION_CN.md) - Complete API reference
- [Project Summary](documents/PROJECT_SUMMARY_CN.md) - Project retrospective report

## 🎯 Core Features

### 1. User Authentication
- JWT Token authentication
- Simulated login (enter user ID)

### 2. Activity Management
- Activity list display
- Activity detail view
- Activity status (not started/in progress/ended)

### 3. Seckill Functionality
- Precise countdown (synchronized with server time)
- Image captcha anti-bot
- Seckill token hiding (obtain token after captcha verification)
- Async order processing (MQ peak shaving)
- Order result polling

### 4. Performance Optimization
- Redis pre-stock reduction (Lua script atomic operations)
- Local cache flag (JVM memory)
- RabbitMQ async peak shaving
- Connection pool optimization

## ⚡ Performance Metrics

| Concurrent Threads | QPS | Avg Response Time | Error Rate |
|-------------------|-----|------------------|------------|
| 100 | 12,704.9 | 7ms | 0.00% |
| 1,000 | 9,915.7 | 99ms | 0.00% |
| 3,000 | 10,001.5 | 291ms | 0.00% |

**Data Consistency**: 100,000 concurrent requests, **0 over-selling, 0 under-selling** ✅

## 🔧 Development Environment

- JDK 17+
- Maven 3.6+
- Node.js 18+
- Docker & Docker Compose

## 📝 API Documentation

After starting the backend service, access Swagger UI:
- http://localhost:8081/swagger-ui.html

Main endpoints:
- `POST /auth/login` - User login
- `GET /activity/list` - Get activity list
- `GET /activity/{id}` - Get activity details
- `GET /seckill/captcha` - Get captcha
- `GET /seckill/token/{activityId}` - Get seckill token
- `POST /seckill/doSeckill/{activityId}/{token}` - Execute seckill
- `GET /order/result/{activityId}` - Query order result

## 🐛 Common Issues

See the "Common Issues" section in the Chinese documentation.

## 📄 License

This project is for learning and research purposes only.

## 👤 Author

**Ai Yuyang**  
Email: ai.yuyang2024@gmail.com

---

**⭐ If this project helps you, please give it a Star!**

