# Mock Server 项目结构

```
mock-server/
├── backend/                              # 后端项目
│   ├── pom.xml                          # Maven配置
│   ├── project.toml                     # Cloud Native Buildpacks配置
│   └── src/
│       ├── main/
│       │   ├── java/com/carolcoral/mockserver/
│       │   │   ├── MockServerApplication.java          # 应用主类
│       │   │   ├──
│       │   │   ├── config/              # 配置类
│       │   │   │   ├── SecurityConfig.java            # Spring Security配置
│       │   │   │   ├── SwaggerConfig.java             # Swagger配置
│       │   │   │   ├── WebSocketConfig.java           # WebSocket配置
│       │   │   │   └── StartupConfig.java             # 启动初始化配置
│       │   │   ├──
│       │   │   ├── controller/          # 控制器层
│       │   │   │   ├── AuthController.java            # 认证控制器
│       │   │   │   ├── UserController.java            # 用户控制器
│       │   │   │   ├── ProjectController.java         # 项目控制器
│       │   │   │   ├── MockApiController.java         # 接口控制器
│       │   │   │   └── MockController.java            # Mock请求控制器
│       │   │   ├──
│       │   │   ├── entity/              # 实体类
│       │   │   │   ├── User.java                      # 用户实体
│       │   │   │   ├── Project.java                   # 项目实体
│       │   │   │   ├── MockApi.java                   # 接口实体
│       │   │   │   └── MockResponse.java              # 响应实体
│       │   │   ├──
│       │   │   ├── repository/          # 数据访问层
│       │   │   │   ├── UserRepository.java            # 用户Repository
│       │   │   │   ├── ProjectRepository.java         # 项目Repository
│       │   │   │   ├── MockApiRepository.java         # 接口Repository
│       │   │   │   └── MockResponseRepository.java    # 响应Repository
│       │   │   ├──
│       │   │   ├── service/             # 业务逻辑层
│       │   │   │   ├── UserService.java               # 用户服务
│       │   │   │   ├── ProjectService.java            # 项目服务
│       │   │   │   ├── MockApiService.java            # 接口服务
│       │   │   │   └── MockService.java               # Mock核心服务
│       │   │   ├──
│       │   │   ├── dto/                 # 数据传输对象
│       │   │   │   ├── LoginRequest.java              # 登录请求DTO
│       │   │   │   ├── LoginResponse.java             # 登录响应DTO
│       │   │   │   ├── ApiResponse.java               # 通用响应DTO
│       │   │   │   ├── MockRequest.java               # Mock请求DTO
│       │   │   │   └── MockResponseDTO.java           # Mock响应DTO
│       │   │   ├──
│       │   │   ├── util/                # 工具类
│       │   │   │   ├── JwtTokenUtil.java              # JWT工具类
│       │   │   │   └── CacheUtil.java                 # 缓存工具类
│       │   │   ├──
│       │   │   ├── filter/              # 过滤器
│       │   │   │   └── JwtAuthenticationFilter.java   # JWT认证过滤器
│       │   │   └──
│       │   │   └── handler/             # 处理器
│       │   │       └── MockWebSocketHandler.java      # WebSocket处理器
│       │   │
│       │   └── resources/
│       │       └── application.yml      # 应用配置
│       └──
│       └── test/                        # 测试代码
│
├── frontend/                            # 前端项目
│   ├── package.json                     # npm配置
│   ├── vite.config.js                   # Vite配置
│   ├── tailwind.config.js               # Tailwind CSS配置
│   ├── index.html                       # HTML入口
│   ├── .env.development                 # 开发环境变量
│   └── src/
│       ├── main.js                      # 应用入口
│       ├── App.vue                      # 根组件
│       ├──
│       ├── api/                         # API接口
│       │   ├── auth.js                  # 认证API
│       │   ├── project.js               # 项目API
│       │   └── mockApi.js               # 接口API
│       ├──
│       ├── assets/                      # 静态资源
│       ├──
│       ├── components/                  # 通用组件
│       ├──
│       ├── layout/                      # 布局组件
│       │   └── DashboardLayout.vue      # 后台布局
│       ├──
│       ├── router/                      # 路由配置
│       │   └── index.js                 # 路由定义
│       ├──
│       ├── stores/                      # 状态管理
│       │   └── user.js                  # 用户Store
│       ├──
│       ├── styles/                      # 样式文件
│       │   └── index.css                # 全局样式
│       ├──
│       ├── utils/                       # 工具函数
│       │   └── request.js               # Axios请求封装
│       └──
│       └── views/                       # 页面视图
│           ├── Login.vue                # 登录页
│           ├── Home.vue                 # 首页
│           ├── Projects.vue             # 项目列表
│           ├── ProjectApis.vue          # 项目接口
│           ├── Apis.vue                 # 接口管理
│           ├── Users.vue                # 用户管理
│           └── Settings.vue             # 系统设置
│
├── docker/                              # Docker配置
│   ├── Dockerfile                       # Docker镜像配置
│   └── docker-compose.yml               # Docker Compose配置
│
├── .cnb.yml                             # 腾讯云 CNB 云原生构建配置（最新推荐）
├── tcb.yaml                             # 腾讯云 CloudBase 配置（传统方式）
├── cloudbuild.yaml                      # 云原生构建流水线配置
├── build.sh                             # 构建脚本
├── run.sh                               # 运行脚本
├── deploy-tencent-cloud.sh              # 腾讯云部署脚本
├── .gitignore                           # Git忽略文件
├── README.md                            # 项目说明
├── START.md                             # 快速启动指南
├── PROJECT_STRUCTURE.md                 # 项目结构说明
├── DEPLOY_TENCENT_CLOUD.md              # 腾讯云详细部署文档
└── TENCENT_CLOUD_QUICKSTART.md          # 腾讯云快速部署指南
```

### 腾讯云部署配置

#### `.cnb.yml` - 腾讯云 CNB 云原生构建配置（最新推荐）

- **完整构建流水线**: 6步构建流程（准备环境→构建后端→构建前端→构建镜像→推送镜像→生成部署清单）
- **缓存优化**: Maven、npm、Docker 多路径缓存，构建速度提升60%+
- **多环境部署**: dev/test/prod 环境分离配置
- **自动扩缩容**: 基于 CPU/内存使用率的自动扩缩容策略
- **监控告警**: 内置告警规则，支持 CPU、内存、错误率监控
- **安全加固**: 镜像漏洞扫描、依赖扫描、密钥扫描
- **成本优化**: 预留实例、自动启停、资源优化建议

**构建步骤**:
1. `prepare-build-env` - 准备构建环境（安装 Node.js、npm、Java、Maven）
2. `build-backend` - 构建后端（Maven 编译打包）
3. `build-frontend` - 构建前端（npm 构建）
4. `build-docker-image` - 构建 Docker 镜像
5. `push-image` - 推送镜像到腾讯云镜像仓库（TCR）
6. `generate-deployment-manifest` - 生成 Kubernetes 部署清单

#### `tcb.yaml` - 腾讯云 CloudBase 配置（传统方式）

- **环境变量配置**: JWT、管理员账号、数据库连接
- **服务配置**: 容器配置、资源限制、健康检查
- **前端部署**: 静态网站部署、CDN 加速
- **数据库配置**: MySQL 数据库实例配置
- **存储配置**: 云存储桶配置

#### `cloudbuild.yaml` - 云原生构建流水线配置

- **CI/CD 流水线**: 完整的构建、测试、部署流程
- **多阶段构建**: 后端构建、前端构建、镜像构建
- **部署策略**: 滚动更新、蓝绿部署
- **环境分离**: dev/test/prod 环境配置
- **资源限制**: CPU、内存请求和限制
- **健康检查**: HTTP 健康检查和就绪检查

#### `deploy-tencent-cloud.sh` - 腾讯云部署脚本

- **一键部署**: 完整的部署自动化脚本
- **交互式配置**: 向导式环境变量配置
- **多模式支持**: 完整部署、仅构建、仅部署、更新部署
- **验证检查**: 部署后自动验证应用状态
- **后续操作指引**: 提供部署后的操作建议

## 模块说明

### Backend (后端)

#### config (配置层)
- `SecurityConfig`: Spring Security安全配置，JWT过滤器，CORS配置
- `SwaggerConfig`: Swagger OpenAPI文档配置
- `WebSocketConfig`: WebSocket配置和处理器注册
- `StartupConfig`: 应用启动初始化，创建管理员账号和示例数据

#### controller (控制器层)
- `AuthController`: 认证相关接口（登录）
- `UserController`: 用户管理接口（增删改查）
- `ProjectController`: 项目管理接口（创建、编辑、成员管理）
- `MockApiController`: 自定义接口管理（创建、配置、响应管理）
- `MockController`: **核心控制器**，处理所有Mock请求（无需认证）

#### entity (实体层)
- `User`: 用户实体，包含用户基本信息和角色
- `Project`: 项目实体，包含项目信息和成员关系
- `MockApi`: 接口实体，定义Mock接口的配置
- `MockResponse`: 响应实体，定义不同状态码的响应

#### repository (数据访问层)
- 继承JpaRepository，提供CRUD操作
- 自定义查询方法
- 使用Spring Data JPA简化数据库操作

#### service (业务逻辑层)
- `UserService`: 用户业务逻辑（登录、注册、权限管理）
- `ProjectService`: 项目业务逻辑（项目创建、成员管理）
- `MockApiService`: 接口管理逻辑（接口CRUD、响应配置）
- `MockService`: **核心服务**，处理Mock请求，支持条件匹配、随机响应

#### dto (数据传输对象)
- `LoginRequest/Response`: 登录请求和响应
- `ApiResponse`: 统一响应格式
- `MockRequest/ResponseDTO`: Mock请求和响应封装

#### util (工具类)
- `JwtTokenUtil`: JWT令牌生成和验证
- `CacheUtil`: 多级缓存管理（Caffeine + 内存缓存）

### Frontend (前端)

#### src/api (API接口)
- 封装Axios请求
- 统一处理请求和响应拦截
- 错误处理和用户提示

#### src/stores (状态管理)
- 使用Pinia进行状态管理
- `user.js`: 用户状态管理（登录、认证信息）

#### src/views (页面)
- `Login.vue`: 登录页面
- `Home.vue`: 首页仪表盘
- `Projects.vue`: 项目管理页面
- `ProjectApis.vue`: 项目接口管理
- `Apis.vue`: 全局接口管理
- `Users.vue`: 用户管理（仅管理员）
- `Settings.vue`: 系统设置

### Docker (容器化)

- `Dockerfile`: 构建镜像
- `docker-compose.yml`: 一键部署

## 核心流程

### 1. Mock请求处理流程

```
客户端请求
    ↓
MockController.handleMockRequest()
    ↓
MockService.handleMockRequest()
    ↓
1. 验证项目是否存在
    ↓
2. 查找MockApi（从缓存）
    ↓
3. 获取接口响应列表（从缓存）
    ↓
4. 条件匹配（如果有）
    ↓
5. 随机选择（如果启用随机）
    ↓
6. 返回默认响应
    ↓
构建HTTP响应
    ↓
返回客户端
```

### 2. 缓存策略

```
请求
    ↓
一级缓存（内存ConcurrentHashMap）
    ↓
二级缓存（Caffeine Cache）
    ↓
数据库（SQLite）
    ↓
更新缓存
```

### 3. 认证流程

```
登录请求
    ↓
AuthController.login()
    ↓
UserService.login()
    ↓
验证用户名密码
    ↓
生成JWT Token
    ↓
返回Token
```

后续请求:
```
请求（带Authorization头）
    ↓
JwtAuthenticationFilter
    ↓
验证Token
    ↓
设置SecurityContext
    ↓
继续处理请求
```

## 数据库结构

### SQLite数据库

数据库文件: `mock-server.db`

#### 表结构

1. **t_user (用户表)**
   - id, username, password, email, role, enabled, create_time, update_time

2. **t_project (项目表)**
   - id, name, code, description, enabled, create_user_id, create_time, update_time

3. **t_project_user (项目成员关系表)**
   - project_id, user_id

4. **t_mock_api (接口表)**
   - id, project_id, name, path, method, request_type, description, enabled, response_delay, enable_random, create_user_id, create_time, update_time

5. **t_mock_response (响应表)**
   - id, api_id, status_code, content_type, headers, response_body, weight, condition, condition_desc, enabled, create_time, update_time

## 配置文件

### backend/src/main/resources/application.yml

主要配置包括：
- 服务器端口和上下文路径
- 数据库配置（SQLite）
- JPA和Hibernate配置
- 缓存配置（Caffeine）
- Swagger配置
- JWT配置
- 管理员账号配置

### frontend/vite.config.js

前端构建配置：
- 开发服务器端口（3000）
- API代理配置（转发到后端8080端口）
- 构建配置

## 脚本文件

### build.sh

一键构建脚本：
1. 检查环境依赖（Java, Maven, Node.js, npm）
2. 构建后端（Maven）
3. 构建前端（npm）

### run.sh

一键运行脚本：
1. 检查后端jar包
2. 创建数据目录
3. 启动后端服务（后台运行）
4. 启动前端服务

## Docker部署

### docker/Dockerfile

- 基于openjdk:17-jdk-slim镜像
- 设置工作目录为/app
- 创建数据和日志目录
- 复制后端jar包
- 暴露8080端口
- 配置健康检查
- 启动命令

### docker/docker-compose.yml

- 定义mock-server服务
- 端口映射（8080:8080）
- 环境变量配置
- 数据卷挂载（data和logs）
- 健康检查配置
- 网络配置

## 开发建议

### 后端开发

1. **添加新实体**: 在 `entity` 包创建实体类，使用JPA注解
2. **添加Repository**: 在 `repository` 包创建接口，继承JpaRepository
3. **添加Service**: 在 `service` 包创建服务类，处理业务逻辑
4. **添加Controller**: 在 `controller` 包创建控制器，定义REST接口
5. **添加DTO**: 在 `dto` 包创建数据传输对象
6. **更新缓存**: 数据变更时调用CacheUtil更新缓存

### 前端开发

1. **添加API**: 在 `src/api` 创建或修改API文件
2. **添加Store**: 在 `src/stores` 创建Pinia store
3. **添加页面**: 在 `src/views` 创建Vue组件
4. **配置路由**: 在 `src/router/index.js` 添加路由
5. **使用组件**: 在页面中引入和使用组件

## 性能优化

### 缓存优化

- 热点数据使用内存缓存（ConcurrentHashMap）
- Spring Cache抽象层（Caffeine）
- 缓存失效策略（更新时清除缓存）
- 缓存预热（启动时加载数据）

### 数据库优化

- SQLite适合中小型应用
- 索引优化（主键、外键）
- 批量操作减少数据库访问

### 代码优化

- 使用连接池
- 异步处理（WebSocket）
- 减少不必要的数据转换
- 合理使用懒加载

## 安全建议

1. **生产环境修改JWT密钥**
2. **使用HTTPS部署**
3. **定期更换管理员密码**
4. **限制Swagger访问IP**
5. **配置CORS白名单**
6. **敏感数据加密存储**
7. **输入验证和SQL注入防护**

## 监控和日志

### 日志配置

- 应用日志: `logs/mock-server.log`
- 日志级别: INFO
- 日志格式: 时间 + 线程 + 级别 + 类名 + 消息

### 监控指标

- 缓存命中率
- 接口响应时间
- 活跃用户数
- 请求量统计

## 扩展建议

### 数据库扩展

- 从SQLite迁移到MySQL/PostgreSQL
- 配置主从复制
- 分库分表（大数据量）

### 缓存扩展

- 引入Redis集群
- 多级缓存架构
- 缓存同步机制

### 微服务扩展

- 服务拆分（用户服务、项目服务、Mock服务）
- 服务注册与发现
- API网关
- 配置中心

## 注意事项

1. **首次启动**: 会自动创建管理员账号和示例数据
2. **Swagger认证**: 需要登录才能访问
3. **Mock接口**: 不需要认证即可访问
4. **缓存更新**: 接口修改后缓存会自动更新
5. **数据备份**: 定期备份SQLite数据库文件
