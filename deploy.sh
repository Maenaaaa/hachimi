#!/bin/bash
# 一键部署脚本 - 从 Docker Hub 拉取镜像部署
# 用法: ./deploy.sh [DockerHub用户名]

set -e

DOCKER_USERNAME=${1:-your-username}

echo "=== 校园淘 - Docker 部署 ==="
echo "镜像来源: $DOCKER_USERNAME/campus-exchange-*"

# 检查 .env
if [ ! -f .env ]; then
  echo "[1/4] 创建 .env 配置文件..."
  cp .env.example .env
  echo "请编辑 .env 文件填入实际密码和 API Key"
  echo "然后重新运行此脚本"
  exit 1
fi

# 登录检查
echo "[1/3] 拉取镜像..."
docker pull $DOCKER_USERNAME/campus-exchange-backend:latest
docker pull $DOCKER_USERNAME/campus-exchange-frontend:latest

# 启动服务
echo "[2/3] 启动服务..."
DOCKER_USERNAME=$DOCKER_USERNAME docker compose -f docker-compose.prod.yml up -d

echo "[3/3] 部署完成！"
echo ""
echo "前端访问: http://localhost"
echo "后端API:  http://localhost:8080"
echo "MinIO控制台: http://localhost:9001"
echo ""
echo "查看日志: docker compose -f docker-compose.prod.yml logs -f"
