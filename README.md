# Mock Server - API接口模拟服务器

基于Spring Boot + Vue 3的API接口模拟服务器，支持自定义接口配置、多项目管理、权限控制和高并发。

## 🌟 主要特性

- 前后端分离架构（Vue 3 + Spring Boot）
- 一键构建部署，前端静态文件集成到后端 JAR 包
- 支持HTTP和WebSocket请求
- 自定义接口配置（多状态码、随机响应、条件响应、自定义代码响应处理器）
- 多项目管理和用户权限控制
- 多语言国际化支持（中文、English、日本語）
- 可配置日期格式（YYYY-MM-DD / DD/MM/YYYY / MM/DD/YYYY）
- JWT认证和Swagger文档
- 系统公告（支持Markdown）
- 系统监控（CPU、内存、磁盘使用率实时监控）
- 内置使用说明文档（USER_GUIDE.md）
- SQLite数据库 + Caffeine缓存
- Docker支持

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.6+
- Node.js 18+
- npm 9+

### 启动项目

#### 方式一：一键构建和运行（推荐）

```bash
./run.sh
```

此脚本会自动构建前后端，并将前端静态文件集成到后端 JAR 包中，只需一个命令即可启动完整服务。

服务地址：
- 前端界面: http://localhost:8080
- 后端API: http://localhost:8080/api
- Swagger文档: http://localhost:8080/swagger-ui.html

#### 方式二：手动构建和运行

```bash
# 1. 构建项目（包含前后端）
./build-all-in-one.sh

# 2. 运行 JAR 包
java -jar backend/target/mock-server-2.0.0.jar
```

#### 方式三：开发模式（前后端分离）

```bash
# 构建脚本
./build.sh

# 后端
cd backend && mvn spring-boot:run

# 前端（新终端）
cd frontend && npm install && npm run dev
```

服务地址：
- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api
- Swagger: http://localhost:8080/api/swagger-ui.html

> **详细构建说明**请参考 [BUILD_README.md](./BUILD_README.md)

### Docker部署

```bash
cd docker
docker-compose up -d
```

## 🔐 默认账号

**开发环境**：
- 用户名: `admin`
- 密码: `Admin@123`（首次启动需设置环境变量）

**生产环境**：必须在环境变量中设置强密码

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
```

## 📖 使用说明

### 1. 创建项目
- 登录后进入"项目管理"
- 点击"创建项目"，填写名称、编码和描述

### 2. 创建接口
- 进入"接口管理"
- 点击"创建接口"，配置：
  - 接口名称和路径
  - 请求方法（GET/POST/PUT/DELETE/PATCH）
  - 请求类型（HTTP/WebSocket）
  - 响应延迟和状态码

### 3. 配置接口响应
- 支持多种响应模式：
  - **固定响应**：直接返回预设的 JSON/文本内容
  - **条件响应**：根据请求参数动态匹配（如 `$.userId == '123'`）
  - **随机响应**：按权重随机返回不同状态码的响应
  - **自定义代码处理器**：使用 Java/JavaScript 代码动态生成响应，支持读写数据库、调用外部API等高级功能

### 4. 系统设置
- **基础设置**：应用名称、版本、系统语言（中文/English/日本語）、日期格式
- **安全配置**：密码强度策略、登录锁定、IP白名单
- **JWT配置**：Token过期时间、刷新Token过期时间、签发者/受众
- **Mock配置**：默认响应延迟、最大延迟、请求日志、请求体大小限制、Axios超时时间
- **系统公告**：创建/编辑/删除公告，支持Markdown，可设置优先级（低/普通/高/紧急）
- **系统监控**：实时查看CPU、内存、磁盘使用率，JVM内存详情，环境变量

### 5. 调用Mock接口

```bash
# HTTP
curl http://localhost:8080/api/mock/{projectCode}/{path}

# WebSocket
const ws = new WebSocket('ws://localhost:8080/api/ws/mock/{projectCode}/{path}')
```

### 6. Swagger认证

访问: http://localhost:8080/api/swagger-ui.html

Swagger登录（如需要）：
```bash
curl -X POST http://localhost:8080/api/auth/swagger-login \
  -d "username=admin&password=Admin@123"
```

## 📁 项目结构

```
mock-server/
├── backend/                      # 后端代码（Spring Boot）
│   ├── src/main/java/com/carolcoral/mockserver/
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── filter/               # JWT过滤器
│   │   ├── plugin/               # 插件（自定义响应转换器、动态编译器）
│   │   ├── repository/           # 数据访问层
│   │   ├── service/              # 业务逻辑层
│   │   └── util/                 # 工具类
│   └── pom.xml
├── frontend/                     # 前端代码（Vue 3）
│   ├── src/
│   │   ├── locales/              # 国际化语言文件（zh-CN/en-US/ja-JP）
│   │   ├── views/                # 页面
│   │   ├── router/               # 路由
│   │   ├── stores/               # 状态管理
│   │   └── utils/                # 工具函数（日期格式化等）
│   └── package.json
├── docker/                       # Docker配置
│   ├── Dockerfile
│   └── docker-compose.yml
├── build.sh                      # 构建脚本
├── run.sh                        # 启动脚本
└── README.md
```

## ⚙️ 配置说明

### 后端配置（backend/src/main/resources/application.yml）

```yaml
# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 1800000  # 30分钟

# 管理员配置
admin:
  username: ${ADMIN_USERNAME:admin}
  password: ${ADMIN_PASSWORD:}  # 生产环境必须设置
  email: ${ADMIN_EMAIL:admin@mockserver.com}
```

### 前端配置（frontend/vite.config.js）

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 环境变量

```bash
# 必需
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
ADMIN_PASSWORD=YourStrongP@ssw0rd123

# 可选
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
SWAGGER_USERNAME=swagger_admin
SWAGGER_PASSWORD=YourSw@ggerP@ss123
```

## 🎲 条件表达式示例

根据请求参数返回不同响应：

```javascript
$.userId == '123'        // userId等于123
$.status == 'active'     // status等于active
$.amount > 100           // amount大于100
```

## 🎲 随机响应权重

| 状态码 | 权重 | 概率 |
|--------|------|------|
| 200    | 80   | 80%  |
| 500    | 20   | 20%  |

## 🛠️ 开发指南

### 添加新功能

1. **后端**：创建Entity → Repository → Service → Controller
2. **前端**：创建Vue组件 → 配置路由 → API调用

### 代码规范

- Java代码遵循Spring Boot规范
- Vue代码遵循Vue 3组合式API规范
- 提交代码前运行测试

## 🔒 安全特性

- JWT认证（可配置过期时间）
- 强密码策略（大小写字母+数字+特殊字符）
- 登录失败锁定（可配置最大尝试次数和锁定时长）
- IP白名单支持
- CORS配置
- 管理员权限控制
- 防止XSS和SQL注入

## 📄 许可证

Apache License 2.0

Copyright (c) 2024-2026 carolcoral

## 📧 联系

- GitHub: [carolcoral](https://github.com/carolcoral)
- Email: lxw@cnkj.site
