@echo off
REM ============================================
REM 构建并推送多架构镜像到 Docker Hub
REM ============================================

set DOCKER_USER=phoenixshy

echo [0/4] 创建多架构构建器（仅首次需要）...
docker buildx create --name multiarch --use 2>nul
docker buildx inspect --bootstrap

echo.
echo [1/4] 登录 Docker Hub...
docker login

echo.
echo [2/4] 构建并推送后端镜像 (amd64 + arm64)...
cd backend
docker buildx build --platform linux/amd64,linux/arm64 -t %DOCKER_USER%/hachimi-backend:latest --push .
cd ..

echo.
echo [3/4] 构建并推送前端镜像 (amd64 + arm64)...
cd frontend
docker buildx build --platform linux/amd64,linux/arm64 -t %DOCKER_USER%/hachimi-frontend:latest --push .
cd ..

echo.
echo [4/4] 验证镜像架构...
docker manifest inspect %DOCKER_USER%/hachimi-backend:latest | findstr architecture
docker manifest inspect %DOCKER_USER%/hachimi-frontend:latest | findstr architecture

echo.
echo ============================================
echo 完成! 多架构镜像已推送到 Docker Hub
echo ============================================
echo.
echo 在 NAS 上执行以下命令拉取并启动:
echo docker-compose -f docker-compose.nas.yml up -d
echo.
pause
