# ========================================
# Mock Server - Debian 构建版
# ========================================

FROM debian:12-slim

LABEL maintainer="carolcoral"
LABEL description="Mock Server v2.1.0"

# ---- 基础依赖 + Eclipse Temurin JDK 21 ----
# Debian 12 默认源无 openjdk-21，通过 Adoptium 官方源安装
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates gnupg sudo bash procps tzdata wget gosu \
    && mkdir -p /etc/apt/keyrings \
    && wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" \
    > /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends temurin-21-jdk \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# ---- 确保 Java 21 优先（Maven 安装可能拉入 openjdk-17）----
ENV JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# ---- 复制源码 ----
COPY . /app/build
WORKDIR /app/build

# ---- 安装环境 + 构建 ----
# setup-env.sh 检测到 Java 21 后跳过，只装 Maven + Node.js
RUN bash setup-env.sh < /dev/null \
    && bash build-all-in-one.sh

# ---- 保留产物，删除源码 ----
RUN mkdir -p /app/data /app/logs /app/frontend/dist \
    && cp backend/target/mock-server-*.jar /app/mock-server.jar \
    && cp -r frontend/dist/* /app/frontend/dist/ \
    && rm -rf /app/build \
    && rm -rf /root/.m2 /root/.npm /root/.cache /root/.sdkman

# ---- 非 root 用户 ----
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /bin/sh appuser

# ---- 启动脚本 ----
# 以 root 启动时先修复 volume 目录权限，再用 gosu 切换到 appuser 运行 Java
# gosu 比 su 更适合容器环境，避免 seccomp 阻止 setgroups/setuid 系统调用
RUN printf '#!/bin/sh\n\
# 确保数据目录存在且权限正确（支持以 root 启动时自动修复）\n\
mkdir -p /app/data /app/logs\n\
if [ "$(id -u)" = "0" ]; then\n\
    chown -R appuser:appgroup /app/data /app/logs 2>/dev/null || true\n\
    exec gosu appuser java ${JAVA_OPTS} -jar /app/mock-server.jar --server.port=8080\n\
else\n\
    exec java ${JAVA_OPTS} -jar /app/mock-server.jar --server.port=8080\n\
fi\n' > /app/start.sh \
    && chmod +x /app/start.sh \
    && chown -R appuser:appgroup /app

USER appuser
WORKDIR /app
EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"
VOLUME ["/app/data", "/app/logs"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["/app/start.sh"]
