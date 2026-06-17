<p align="center">
  <img src="https://img.shields.io/badge/version-2.1.1-blue?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/license-Apache%202.0-green?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21%2B-orange?style=flat-square" alt="JDK">
  <img src="https://img.shields.io/badge/Node-18%2B-brightgreen?style=flat-square" alt="Node">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-4FC08D?style=flat-square&logo=vuedotjs" alt="Vue">
</p>

<h1 align="center">Mock Server</h1>

<p align="center">
  <b>基于 Spring Boot + Vue 3 的全功能 API 接口模拟服务器</b>
</p>

<p align="center">
  支持 HTTP / WebSocket Mock · 多项目隔离 · 权限分级 · 动态代码编译 · 多语言国际化 · 邮件通知 · 系统监控
</p>

---

## 📖 目录

- [✨ 核心特性](#-核心特性)
- [🚀 快速开始](#-快速开始)
- [🔐 默认账号](#-默认账号)
- [📚 使用指南](#-使用指南)
- [📁 项目结构](#-项目结构)
- [⚙️ 关键配置](#-关键配置)
- [🔒 安全机制](#-安全机制)
- [📄 许可证](#-许可证)
- [📧 联系](#-联系)

---

## ✨ 核心特性

<table>
  <tr>
    <td width="50%">
      <h4>🧩 接口模拟</h4>
      <ul>
        <li>固定响应 / 条件响应 / 随机权重响应</li>
        <li>Java 自定义代码处理器，动态编译热加载</li>
        <li>响应延迟模拟、HTTP 状态码自定义</li>
        <li>WebSocket Mock 支持</li>
      </ul>
    </td>
    <td width="50%">
      <h4>📦 项目管理</h4>
      <ul>
        <li>多项目隔离，独立项目编码</li>
        <li>成员权限分级：创建者 / 管理员 / 普通成员</li>
        <li>团队协作，项目成员管理</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>
      <h4>🧬 代码模板</h4>
      <ul>
        <li>Monaco Editor 智能编辑</li>
        <li>编译验证 + 服务端动态加载</li>
        <li>模板按项目与启用状态筛选</li>
      </ul>
    </td>
    <td>
      <h4>✉️ 邮件系统</h4>
      <ul>
        <li>SMTP 邮件配置管理</li>
        <li>邮件模板（通用 / 验证码 / 告警），HTML 实时预览</li>
        <li>注册验证码邮件发送</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>
      <h4>🌍 国际化</h4>
      <ul>
        <li>中文 / English / 日本語 三种语言</li>
        <li>全站实时切换，持久化保存</li>
      </ul>
    </td>
    <td>
      <h4>📊 系统监控</h4>
      <ul>
        <li>请求趋势统计 + IOPS 实时展示</li>
        <li>CPU / 内存 / 磁盘使用率</li>
        <li>JVM 堆内存详情</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>
      <h4>🎨 现代化 UI</h4>
      <ul>
        <li>深色渐变侧边栏 + Canvas 动态游走线条</li>
        <li>分组折叠菜单，支持展开 / 收缩模式</li>
        <li>彩色渐变 Logo 流动动画</li>
      </ul>
    </td>
    <td>
      <h4>🐳 部署方案</h4>
      <ul>
        <li>Docker 多阶段构建，一键脚本</li>
        <li>前后端一体化部署，单 JAR 运行</li>
        <li>容器非 root + 只读文件系统</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🚀 快速开始

### 环境要求

> **JDK 21+** &nbsp;·&nbsp; **Maven 3.6+** &nbsp;·&nbsp; **Node.js 18+** &nbsp;·&nbsp; **npm 9+**

### 一键启动（推荐）

```bash
# 克隆项目
git clone https://github.com/carolcoral/mock-server.git
cd mock-server

# 启动（自动构建前后端，前端静态文件集成到后端 JAR）
./run.sh
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 🖥️ 前端界面 | http://localhost:8080 |
| 🔧 后端 API | http://localhost:8080/api |
| 📖 Swagger 文档 | http://localhost:8080/swagger-ui.html |

### 手动构建

```bash
# 构建全量包
./build-all-in-one.sh

# 启动
java -jar backend/target/mock-server-2.1.1.jar
```

### 开发模式

```bash
# 终端 1：后端
cd backend && mvn spring-boot:run

# 终端 2：前端
cd frontend && npm install && npm run dev
```

> 前端开发服务器：`http://localhost:3000` · 后端：`http://localhost:8080/api`

### Docker 部署

```bash
cd docker && docker-compose up -d
```

> 📖 详细构建说明见 [BUILD_README.md](./BUILD_README.md)

---

## 🔐 默认账号

| 环境 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 开发 | `admin` | `Admin@123` | 首次启动自动创建（需配置环境变量） |
| 生产 | `$ADMIN_USERNAME` | `$ADMIN_PASSWORD` | **必须设置高强度密码** |

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
```

---

## 📚 使用指南

### 接口响应模式

| 模式 | 说明 | 示例场景 |
|------|------|----------|
| **固定响应** | 预设 JSON / 文本直接返回 | 状态码 200 / 404 / 500 |
| **条件响应** | 请求参数动态匹配 | `$.userId == '123'` → 返回用户 A 数据 |
| **随机响应** | 按权重随机返回不同结果 | 灰度发布模拟 |
| **代码处理器** | Java 动态编译，读写 DB、调用外部 API | 数据脱敏、格式包装 |

### 调用示例

```bash
# HTTP Mock
curl http://localhost:8080/api/mock/{projectCode}/{apiPath}

# WebSocket Mock
new WebSocket('ws://localhost:8080/api/ws/mock/{projectCode}/{path}')
```

---

## 📁 项目结构

```
mock-server/
├── backend/                          # Spring Boot 后端
│   └── src/.../mockserver/
│       ├── config/                   # Security / Web / CORS / 邮件 / 系统配置
│       ├── controller/               # REST 控制器
│       ├── entity/                   # JPA 实体类
│       ├── filter/                   # JWT 认证过滤器
│       ├── plugin/                   # 动态编译器 & 自定义响应处理器
│       ├── repository/               # 数据访问层 (Spring Data JPA)
│       ├── service/                  # 业务逻辑（含邮件服务）
│       └── util/                     # 工具类
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── locales/                  # 国际化 (zh-CN / en-US / ja-JP)
│       ├── views/                    # 页面组件（着陆页 / 仪表盘 / 管理页）
│       ├── layout/                   # 布局组件（侧边栏 / 页脚）
│       ├── router/                   # Vue Router 路由
│       ├── stores/                   # Pinia 状态管理
│       ├── api/                      # Axios API 封装
│       └── utils/                    # 工具函数
├── docker/                           # Docker Compose + Dockerfile
├── build.sh / run.sh                 # 构建 & 启动脚本
├── BUILD_README.md                   # 构建说明
├── CHANGELOG.md                      # 版本变更日志
└── README.md
```

---

## ⚙️ 关键配置

```yaml
# JWT 认证
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 1800000                 # 30 分钟

# 管理员
admin:
  username: ${ADMIN_USERNAME:admin}
  password: ${ADMIN_PASSWORD:}        # 生产环境必须设置
```

```bash
# 环境变量
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000   # 可选：CORS 白名单
```

---

## 🔒 安全机制

| 安全特性 | 说明 |
|----------|------|
| JWT 认证 | 可配置过期时间，无状态 Token |
| 强密码策略 | 大小写 + 数字 + 特殊字符，最少 8 位 |
| 登录锁定 | 多次失败后临时锁定，可配置次数和时长 |
| IP 白名单 | 限制管理接口访问来源 |
| CORS 配置 | 跨域请求白名单控制 |
| 防注入攻击 | SQL 参数化查询 · XSS 过滤 · CSRF 防护 |
| 权限分级 | 系统管理员 / 项目创建者 / 项目管理员 / 普通用户 |

---

## 📄 许可证

```
Apache License 2.0
Copyright © 2024-2026 carolcoral
```

---

## 📧 联系

- **GitHub**：[github.com/carolcoral](https://github.com/carolcoral)
- **Email**：lxw@cnkj.site

<p align="center">
  <sub>Made with ❤️ by <a href="https://github.com/carolcoral">carolcoral</a></sub>
</p>
