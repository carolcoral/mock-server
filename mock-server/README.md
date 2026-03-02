# Mock Server - API接口模拟服务器

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-blue.svg)](https://vuejs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3-06B6D4.svg)](https://tailwindcss.com/)
[![SQLite](https://img.shields.io/badge/SQLite-3-003B57.svg)](https://www.sqlite.org/)

作者: **carolcoral** (github同名)

一个功能强大的前后端分离的API接口模拟服务器，支持自定义接口配置、多项目管理、权限控制、高并发和缓存。

## 🌟 主要特性

### 核心功能
- ✅ 前后端分离架构（Vue3 + Spring Boot）
- ✅ 支持HTTP和WebSocket请求
- ✅ 自定义接口配置（增删改查）
- ✅ 多项目管理
- ✅ 用户权限管理（管理员/普通用户）
- ✅ 高并发支持（Caffeine缓存）
- ✅ SQLite数据库存储
- ✅ Swagger文档（需认证）
- ✅ Docker支持
- ✅ 随机响应和条件响应

### 自定义接口功能
1. **多状态码支持**: 支持200、400、401、403、500、503等状态码
2. **状态开关**: 可启用/禁用特定状态码
3. **响应延迟**: 可设置响应延迟时间
4. **随机响应**: 支持随机返回不同状态码，可设置权重
5. **请求类型**: 支持HTTP和WebSocket
6. **内存缓存**: 数据加载到内存，极速响应
7. **条件响应**: 根据请求参数返回不同响应（可选）

### 权限管理
- **多账号管理**: 支持创建多个账号
- **角色管理**: 管理员和普通用户角色
- **项目管理**: 不同项目下创建不同接口
- **权限控制**: 普通用户只能管理有权限的项目

## 🚀 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- SQLite 3
- Caffeine Cache
- Swagger OpenAPI 3
- WebSocket

### 前端
- Vue 3.4
- Vue Router 4
- Pinia
- Element Plus
- Tailwind CSS
- Axios

## 📦 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+
- npm 9+

### 后端启动

```bash
# 进入后端目录
cd backend

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run

# 或使用jar包
java -jar target/mock-server-1.0.0.jar
```

后端服务将运行在: http://localhost:8080

API文档: http://localhost:8080/api/swagger-ui.html
- 用户名: `admin`
- 密码: `Admin@123`

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

前端将运行在: http://localhost:3000

### Docker部署

```bash
# 构建镜像
cd docker
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

# 数据库配置
spring:
  datasource:
    url: jdbc:sqlite:./data/mock-server.db  # SQLite数据库路径
    driver-class-name: org.sqlite.JDBC

# JWT配置
jwt:
  secret: mock-server-secret-key-2024      # JWT密钥
  expiration: 86400000                      # Token过期时间（24小时）

# 管理员配置
admin:
  username: admin                          # 管理员用户名
  password: Admin@123                      # 管理员密码
  email: admin@mockserver.com              # 管理员邮箱
```

### 前端配置

前端配置文件在 `frontend/vite.config.js`，主要配置API代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true
    }
  }
}
```

## 📖 使用指南

### 1. 登录系统

访问前端页面: http://localhost:3000

默认账号:
- 用户名: `admin`
- 密码: `Admin@123`

### 2. 创建项目

1. 登录后进入"项目管理"
2. 点击"创建项目"
3. 填写项目名称、编码和描述
4. 保存项目

### 3. 创建接口

1. 进入"接口管理"或项目详情页
2. 点击"创建接口"
3. 配置接口信息:
   - 接口名称
   - 请求路径
   - 请求方法（GET/POST/PUT/DELETE/PATCH）
   - 请求类型（HTTP/WebSocket）
   - 响应延迟（毫秒）
   - 是否启用随机返回
4. 添加响应配置:
   - HTTP状态码
   - 响应头
   - 响应体
   - 权重（用于随机返回）
   - 条件表达式（可选）

### 4. 调用Mock接口

接口创建后，可以通过以下方式调用:

```bash
# HTTP请求
curl http://localhost:8080/api/mock/{projectCode}/{path}

# 示例
curl http://localhost:8080/api/mock/demo/user/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 5. WebSocket测试

```javascript
const ws = new WebSocket('ws://localhost:8080/api/ws/mock/demo/user/ws')

ws.onopen = () => {
  ws.send(JSON.stringify({
    type: 'mock',
    projectCode: 'demo',
    path: '/user/ws',
    method: 'GET',
    params: { id: '1' }
  }))
}

ws.onmessage = (event) => {
  console.log('Response:', JSON.parse(event.data))
}
```

## 🔐 Swagger认证

访问Swagger文档: http://localhost:8080/api/swagger-ui.html

首次访问需要认证：
1. 点击页面右上角的"Authorize"按钮
2. 输入用户名: `admin`
3. 输入密码: `Admin@123`
4. 点击登录

或者使用Swagger登录接口:

```bash
curl -X POST http://localhost:8080/api/auth/swagger-login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

登录成功后，Swagger会自动在请求头中添加JWT Token。

## 📊 条件表达式示例

条件表达式用于根据请求参数返回不同的响应：

```javascript
// JSONPath格式
$.userId == '123'              // 当userId等于123时
$.status == 'active'           // 当status等于active时
$.amount > 100                 // 当amount大于100时
```

示例配置:
- **条件**: `$.userId == '123'`
- **描述**: 当userId等于123时返回此响应

## 🎲 随机响应权重配置

当启用随机返回时，可以根据权重设置不同响应的返回概率：

| 状态码 | 权重 | 概率 |
|--------|------|------|
| 200    | 80   | 80%  |
| 500    | 20   | 20%  |

权重总和为100，系统会根据权重随机选择响应。

## 🐳 Docker部署

### 使用Docker Compose

```bash
cd docker
docker-compose up -d
```

服务将运行在: http://localhost:8080

### 构建Docker镜像

```bash
# 构建后端
mvn clean package -DskipTests

# 构建镜像
docker build -t mock-server:latest -f docker/Dockerfile .

# 运行容器
docker run -d \
  --name mock-server \
  -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  mock-server:latest
```

## 📈 性能优化

### 缓存机制

应用使用了多级缓存：
1. **Caffeine Cache**: Spring Cache抽象层
2. **内存缓存**: ConcurrentHashMap高速缓存
3. **数据库**: SQLite持久化存储

接口响应时间:
- 缓存命中: < 5ms
- 缓存未命中: < 50ms

### 高并发支持

- 使用Spring WebFlux异步处理（可扩展）
- 缓存热点数据
- 数据库连接池优化
- JVM参数调优

## 🛠️ 开发指南

### 项目结构

```
mock-server/
├── backend/                      # 后端代码
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/carolcoral/mockserver/
│   │   │   │   ├── config/       # 配置类
│   │   │   │   ├── controller/   # 控制器
│   │   │   │   ├── entity/       # 实体类
│   │   │   │   ├── repository/   # 数据访问层
│   │   │   │   ├── service/      # 业务逻辑层
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── util/         # 工具类
│   │   │   │   └── filter/       # 过滤器
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
├── frontend/                     # 前端代码
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # 组件
│   │   ├── layout/               # 布局组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # 状态管理
│   │   ├── styles/               # 样式文件
│   │   ├── utils/                # 工具函数
│   │   └── views/                # 页面视图
│   └── package.json
├── docker/                       # Docker配置
│   ├── Dockerfile
│   └── docker-compose.yml
└── README.md
```

### 添加新功能

1. **添加实体类**: 在 `entity` 包下创建实体
2. **添加Repository**: 在 `repository` 包下创建数据访问接口
3. **添加Service**: 在 `service` 包下创建业务逻辑
4. **添加Controller**: 在 `controller` 包下创建控制器
5. **添加前端页面**: 在 `frontend/src/views` 下创建Vue组件
6. **配置路由**: 在 `frontend/src/router/index.js` 中添加路由

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

Apache License 2.0

Copyright (c) 2024 carolcoral

## 📧 联系方式

- GitHub: [carolcoral](https://github.com/carolcoral)
- Email: admin@mockserver.com

## 🙏 致谢

- Spring Boot
- Vue.js
- Element Plus
- Tailwind CSS
- SQLite
- 以及其他开源项目
