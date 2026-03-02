# 基础镜像
FROM openjdk:17-jdk-slim

# 作者信息
LABEL maintainer="carolcoral <github.com/carolcoral>"
LABEL description="Mock Server - API接口模拟服务器"

# 设置工作目录
WORKDIR /app

# 创建数据目录和日志目录
RUN mkdir -p /app/data /app/logs

# 复制后端jar包
COPY backend/target/mock-server-*.jar /app/mock-server.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v3/api-docs || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/mock-server.jar"]
