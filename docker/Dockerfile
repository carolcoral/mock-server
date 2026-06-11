# ========================================
# Mock Server Dockerfile
# 多阶段构建：前端 -> 后端 -> 最终镜像
# 构建命令: docker build -t mock-server -f docker/Dockerfile .
# ========================================

# ========================================
# 阶段 1: 构建前端
# ========================================
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci --ignore-scripts

COPY frontend/ ./
RUN npm run build

# ========================================
# 阶段 2: 构建后端
# ========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app/backend

COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B -q

COPY backend/ ./
RUN mvn clean package -DskipTests -B -q

# ========================================
# 阶段 3: 运行镜像
# ========================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="carolcoral"
LABEL description="Mock Server - API Mock Server"

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 安装 curl 和 tzdata 用于健康检查和时区，安装后清理缓存
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

WORKDIR /app

# 创建数据和日志目录，设置权限
RUN mkdir -p /app/data /app/logs /app/frontend/dist && \
    chown -R appuser:appgroup /app

# 复制前端构建产物
COPY --from=frontend-builder --chown=appuser:appgroup /app/frontend/dist /app/frontend/dist

# 复制后端 jar 包
COPY --from=backend-builder --chown=appuser:appgroup /app/backend/target/mock-server-*.jar /app/mock-server.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/api/actuator/health || exit 1

# JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 数据持久化挂载点
VOLUME ["/app/data", "/app/logs"]

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/mock-server.jar --server.port=8080"]
