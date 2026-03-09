# ========================================
# Mock Server Dockerfile
# 包含前端和后端的完整应用
# ========================================

# ========================================
# 阶段 1: 构建前端
# ========================================
FROM node:18-alpine AS frontend-builder

# 设置工作目录
WORKDIR /app/frontend

# 复制前端依赖文件
COPY frontend/package*.json ./

# 安装所有依赖（包括开发依赖，用于构建）
RUN npm ci

# 复制前端源代码
COPY frontend/ ./

# 构建前端生产版本
RUN npm run build

# ========================================
# 阶段 2: 构建后端
# ========================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

# 设置工作目录
WORKDIR /app/backend

# 复制后端依赖文件
COPY backend/pom.xml ./

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B

# 复制后端源代码
COPY backend/ ./

# 构建后端（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# ========================================
# 阶段 3: 最终运行镜像
# ========================================
FROM openjdk:17-jdk-slim

# 作者信息
LABEL maintainer="carolcoral <github.com/carolcoral>"
LABEL description="Mock Server - 完整的前后端一体化应用"

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p /app/data /app/logs /app/frontend

# 从前端构建阶段复制构建产物
COPY --from=frontend-builder /app/frontend/dist /app/frontend/dist

# 从后端构建阶段复制 jar 包
COPY --from=backend-builder /app/backend/target/mock-server-*.jar /app/mock-server.jar

# 创建启动脚本
RUN echo '#!/bin/bash\n\
echo "========================================"\n\
echo "Mock Server 启动中..."\n\
echo "========================================"\n\
echo "前端静态文件目录: /app/frontend/dist"\n\
echo "后端 API 端口: 8080"\n\
echo "前端访问端口: ${FRONTEND_PORT:-3002}"\n\
echo "========================================"\n\
\n\
# 启动后端应用（包括静态文件服务）\n\
java -jar /app/mock-server.jar \\\n\
    --server.port=8080 \\\n\
    ${JAVA_OPTS}\n\
' > /app/start.sh && chmod +x /app/start.sh

# 暴露端口
# 8080: 后端 API 和前端静态文件服务
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

# 设置 Java 运行时参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 挂载点
VOLUME ["/app/data", "/app/logs"]

# 启动命令
ENTRYPOINT ["/app/start.sh"]
