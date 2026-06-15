# Mock Server

> 基于 Spring Boot + Vue 3 的 API 接口模拟服务器

[![Version](https://img.shields.io/badge/version-2.1.0-blue)](https://github.com/carolcoral/mock-server)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21%2B-orange)](#)
[![Node](https://img.shields.io/badge/Node-18%2B-brightgreen)](#)

支持 HTTP / WebSocket Mock、多项目管理、权限控制、动态代码编译、多语言及系统监控。

---

## ✨ 特性

| 类别 | 功能 |
|------|------|
| 🧩 接口模拟 | 固定 / 条件 / 随机 / 自定义代码响应 |
| 📦 项目管理 | 多项目隔离，成员权限分级，团队协作 |
| 🧬 代码模板 | Java 代码模板管理，Mongo Editor，编译验证复用 |
| 🌍 国际化 | 中文 / English / 日本語，实时切换 |
| 🔐 安全 | JWT 认证、强密码策略、登录锁定、IP 白名单 |
| 📊 监控 | 请求统计图表、IOPS 实时、CPU / 内存 / 磁盘 |
| 🎨 UI | 深色侧边栏动态线条、折叠模式、彩色渐变 Logo |
| 🐳 部署 | Docker 多阶段构建、一键脚本 |

---

## 🚀 快速开始

### 环境要求

JDK 21+ &nbsp;|&nbsp; Maven 3.6+ &nbsp;|&nbsp; Node.js 18+ &nbsp;|&nbsp; npm 9+

### 一键启动（推荐）

```bash
./run.sh
```

> 自动构建前后端，前端静态文件集成到后端 JAR。

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端界面 | http://localhost:8080 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |

### 手动构建

```bash
./build-all-in-one.sh
java -jar backend/target/mock-server-2.1.0.jar
```

### 开发模式

```bash
# 后端
cd backend && mvn spring-boot:run

# 前端（新终端）
cd frontend && npm install && npm run dev
```
前端 `http://localhost:3000` · 后端 `http://localhost:8080/api`

### Docker

```bash
cd docker && docker-compose up -d
```

> 📖 详细构建说明见 [BUILD_README.md](./BUILD_README.md)

---

## 🔐 默认账号

| 环境 | 用户名 | 密码 | 备注 |
|------|--------|------|------|
| 开发 | `admin` | `Admin@123` | 首次启动需设置环境变量 |
| 生产 | `$ADMIN_USERNAME` | `$ADMIN_PASSWORD` | 必须设置强密码 |

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
```

---

## 📖 使用指南

### 接口响应模式

| 模式 | 说明 |
|------|------|
| **固定响应** | 预设 JSON / 文本直接返回 |
| **条件响应** | 请求参数动态匹配，如 `$.userId == '123'` |
| **随机响应** | 按权重随机返回不同响应 |
| **代码处理器** | Java 动态编译，读写数据库、调用外部 API，支持代码模板复用 |

### 调用示例

```bash
# HTTP
curl http://localhost:8080/api/mock/{projectCode}/{path}

# WebSocket
new WebSocket('ws://localhost:8080/api/ws/mock/{projectCode}/{path}')
```

---

## 📁 项目结构

```
mock-server/
├── backend/                      Spring Boot 后端
│   └── src/.../mockserver/
│       ├── config/               系统配置 (Security, Web, CORS)
│       ├── controller/           REST 控制器
│       ├── entity/               实体类
│       ├── filter/               JWT 认证过滤器
│       ├── plugin/               动态编译器 & 响应处理器
│       ├── repository/           数据访问层
│       ├── service/              业务逻辑
│       └── util/                 工具类
├── frontend/                     Vue 3 前端
│   └── src/
│       ├── locales/              国际化 (zh-CN / en-US / ja-JP)
│       ├── views/                页面组件 (含着陆页 Welcome)
│       ├── layout/               布局组件 (DashboardLayout)
│       ├── router/               路由配置 (Vue Router)
│       ├── stores/               状态管理 (Pinia)
│       ├── api/                  API 请求封装
│       └── utils/                工具函数
├── docker/                       Docker 部署
├── build.sh / run.sh             构建 & 启动脚本
├── CHANGELOG.md                  变更日志
└── README.md
```

---

## ⚙️ 关键配置

```yaml
# JWT
jwt.secret: ${JWT_SECRET:your-secret-key}
jwt.expiration: 1800000  # 30分钟

# 管理员
admin.username: ${ADMIN_USERNAME:admin}
admin.password: ${ADMIN_PASSWORD:}  # 生产必须设置
```

```bash
# 环境变量
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
ADMIN_PASSWORD=YourStrongP@ssw0rd123
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000  # 可选
```

---

## 🔒 安全

- JWT 认证（可配置过期时间）
- 强密码策略（大小写 + 数字 + 特殊字符）
- 登录失败锁定（可配置次数和时长）
- IP 白名单 · CORS 配置 · 防 XSS / SQL 注入
- 管理员权限分级

---

## 📄 许可证

Apache License 2.0 · Copyright © 2024-2026 [carolcoral](https://github.com/carolcoral)

## 📧 联系

- GitHub: [github.com/carolcoral](https://github.com/carolcoral)
- Email: lxw@cnkj.site
