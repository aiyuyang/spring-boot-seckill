#!/bin/bash

echo "🚀 启动秒杀系统..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除旧容器（如果存在）
echo "📦 清理旧容器..."
docker-compose down

# 启动所有服务
echo "🔧 启动服务（MySQL, Redis, RabbitMQ, Backend, Frontend）..."
docker-compose up -d

# 等待MySQL启动
echo "⏳ 等待MySQL启动..."
sleep 10

# 检查服务状态
echo ""
echo "✅ 服务启动完成！"
echo ""
echo "📊 服务状态："
docker-compose ps

echo ""
echo "🌐 访问地址："
echo "   - 前端: http://localhost"
echo "   - 后端API: http://localhost:8081"
echo "   - Swagger文档: http://localhost:8081/swagger-ui.html"
echo "   - RabbitMQ管理: http://localhost:15672 (guest/guest)"
echo ""
echo "📝 查看日志："
echo "   docker-compose logs -f [service_name]"
echo ""
echo "🛑 停止服务："
echo "   docker-compose down"

