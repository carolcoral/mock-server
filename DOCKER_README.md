# Mock Server Docker 快速部署指南

## 🚀 快速开始

### 一键部署

```bash
# 1. 语法检查（可选）
./test-docker-syntax.sh

# 2. 构建镜像
./docker-build.sh

# 3. 启动服务
./docker-start.sh
```

### 访问应用

启动成功后，访问：http://localhost:8080

- 🌐 **前端页面**: http://localhost:8080
- 🔧 **后端 API**: http://localhost:8080/api
- 📚 **API 文档**: http://localhost:8080/api/swagger-ui.html
- ❤️ **健康检查**: http://localhost:8080/api/actuator/health

## 📋 目录说明

```
workspace/
├── docker/
│   ├── Dockerfile              # Docker 镜像定义
│   ├── docker-compose.yml      # Docker Compose 配置
│   └── .env.example            # 环境变量模板
├── docker-build.sh             # 构建脚本
├── docker-start.sh             # 启动脚本
├── docker-stop.sh              # 停止脚本
├── test-docker-syntax.sh       # 语法检查脚本
└── DOCKER_DEPLOYMENT.md        # 详细部署文档
```

## 🛠️ 常用命令

### 构建相关

```bash
# 完整构建（前端 + 后端）
./docker-build.sh

# 无缓存构建
./docker-build.sh no-cache

# 手动构建
cd docker
docker compose build
```

### 启动相关

```bash
# 手动启动
cd docker
docker compose up -d

# 查看日志
docker compose logs -f

# 手动停止
docker compose down
```

### 管理相关

```bash
# 查看服务状态
docker compose ps

# 重启服务
docker compose restart

# 进入容器
docker compose exec mock-server bash

# 更新服务
docker compose up -d --build
```

## ⚙️ 配置说明

### 环境变量配置

复制 `docker/.env.example` 到 `docker/.env` 并修改：

```bash
cd docker
cp .env.example .env
vi .env  # 或使用其他编辑器
```

主要配置项：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| HOST_PORT | 宿主机端口 | 8080 |
| JWT_SECRET | JWT 密钥 | - |
| ADMIN_USERNAME | 管理员用户名 | admin |
| ADMIN_PASSWORD | 管理员密码 | Admin@123! |
| TZ | 时区 | Asia/Shanghai |
| JAVA_OPTS | JVM 参数 | -Xmx512m -Xms256m |

### 端口映射

- **容器端口**: 8080（固定）
- **宿主机端口**: 默认 8080，可通过 `HOST_PORT` 环境变量修改

例如，映射到宿主机的 3000 端口：

```bash
# 在 docker/.env 中设置
HOST_PORT=3000
```

然后访问：http://localhost:3000

### 数据持久化

数据通过 Docker Volume 持久化到宿主机：

- `docker/data/` → 数据库文件
- `docker/logs/` → 日志文件

## 🔍 故障排查

### 服务无法启动

```bash
# 查看日志
cd docker
docker compose logs

# 检查端口占用
netstat -tlnp | grep 8080
```

### 无法访问应用

1. 检查容器是否运行：`docker compose ps`
2. 检查端口映射：`docker compose ps`
3. 查看详细日志：`docker compose logs -f`

### 内存不足

修改 `docker/.env` 中的 `JAVA_OPTS`：

```bash
# 降低内存使用
JAVA_OPTS=-Xmx256m -Xms128m
```

### 重新构建

```bash
# 停止服务
./docker-stop.sh

# 删除旧镜像
docker rmi backend-mock-server 2>/dev/null

# 重新构建
./docker-build.sh

# 启动服务
./docker-start.sh
```

## 📊 资源使用

默认资源配置（可在 `docker-compose.yml` 中调整）：

- **CPU**: 最大 1.0 核心
- **内存**: 最大 1GB
- **JVM 堆内存**: 512MB

## 🌟 特性

- ✅ 单镜像部署：前端和后端打包在一起
- ✅ 统一端口：通过 8080 端口访问所有服务
- ✅ 数据持久化：数据库和日志持久化到宿主机
- ✅ 自动构建：支持多阶段构建，自动编译前后端
- ✅ 健康检查：自动监控服务健康状态
- ✅ 热重载：支持代码修改后快速重新部署
- ✅ 安全配置：支持 JWT 认证和 CORS 配置

## 📖 详细文档

查看完整的部署文档：[DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md)

## 🤝 支持

如有问题，请：
1. 查看详细部署文档
2. 检查容器日志：`docker compose logs`
3. 查看项目 Issues

## 📝 许可证

Copyright (c) 2026, XINDU.SITE
