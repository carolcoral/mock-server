<p align="center">
  <img src="badges/version.svg" alt="Version">
  <img src="badges/license.svg" alt="License">
  <img src="badges/jdk.svg" alt="JDK">
  <img src="badges/node.svg" alt="Node">
  <img src="badges/springboot.svg" alt="Spring Boot">
  <img src="badges/vue.svg" alt="Vue">
</p>

<h1 align="center">Mock Server</h1>
<p align="center">Spring Boot + Vue 3 全功能 API 模拟平台</p>

<p align="center">
  HTTP / WebSocket Mock · 多项目隔离 · 细粒度权限 · 动态代码编译 · AI 对话与生成 · Swagger 导入 · 多语言国际化 · 邮件通知 · 系统监控
</p>

---

## ✨ 核心特性

| 模块 | 能力 |
|------|------|
| 🧩 接口模拟 | 固定响应 / 条件匹配 / 随机权重 / Java 动态代码处理器 / 延迟模拟 / WebSocket Mock |
| 🔐 细粒度权限 | RBAC 角色权限体系，页面+按钮级权限控制，自定义角色，动态菜单显隐 |
| 🤖 AI 智能平台 | 多模型对话（流式 SSE + Markdown 渲染 + 上下文记忆）· 一键生成响应数据 / Java 代码 / 邮件模板 / 接口描述 · 12+ LLM 服务商 · 调用统计 |
| 📥 Swagger 导入 | 支持 Swagger 2.0 / OpenAPI 3.x，上传 JSON 或 URL 一键导入为 Mock API |
| 📦 项目管理 | 多项目隔离 · 成员权限分级（创建者 / 管理员 / 成员） |
| 🧬 代码模板 | Monaco Editor · 编译验证 · 项目级 + 系统级模板 · 热加载 · 6 种转换器 |
| ✉️ 邮件系统 | SMTP 配置 · 模板管理（通用 / 验证码 / 告警）· HTML 实时预览 · 注册验证码 |
| 🌍 国际化 | 中文 / English / 日本語 · 全站实时切换 |
| 📊 监控面板 | 请求趋势 · 来源 IP 多折线 · AI 调用量 · 新增趋势 · IOPS · JVM 堆详情 · CPU/内存/磁盘 |
| 📖 使用说明 | 交互式引导页，6 步上手：创建项目 → 导入接口 → 配置响应 → 参数匹配 → AI 生成 → 调用监控 |
| 🎨 现代 UI | 深色渐变侧边栏 · Canvas 动态线条 · 分组折叠菜单 · 收缩模式 · 全屏欢迎页 |
| 🐳 容器化 | Docker 多阶段构建 · 一键脚本 · 非 root 只读容器 |

---

## 🚀 快速开始

环境要求：JDK 21+ · Maven 3.6+ · Node.js 18+ · npm 9+

```bash
# 一键启动（自动构建前后端）
git clone https://github.com/carolcoral/mock-server.git && cd mock-server
./run.sh
```

访问地址：`http://localhost:8080` | API：`/api`

<details>
<summary>📖 更多部署方式</summary>

```bash
# 手动构建
./build-all-in-one.sh
java -jar backend/target/mock-server-2.3.0.jar

# 开发模式
cd backend && mvn spring-boot:run          # 终端 1
cd frontend && npm install && npm run dev   # 终端 2

# Docker
cd docker && docker-compose up -d
```

> 构建说明见 [BUILD_README.md](./BUILD_README.md)
</details>

---

## 🔐 默认账号

| 环境 | 用户名 | 密码 | 备注 |
|------|--------|------|------|
| 开发 | `admin` | `Admin@123` | 首次启动自动创建 |
| 生产 | `$ADMIN_USERNAME` | `$ADMIN_PASSWORD` | 务必修改强密码 |

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
```

---

## 📚 响应模式

| 模式 | 说明 | 场景 |
|------|------|------|
| 固定响应 | 预设 JSON / 文本 | 状态码 200 / 404 / 500 |
| 条件响应 | 请求参数动态匹配 | `$.userId == '123'` → 返回用户数据 |
| 随机响应 | 按权重随机返回 | 灰度发布模拟 |
| 代码处理器 | Java 动态编译，可读写 DB、调用外部 API | 数据脱敏、格式包装 |

```bash
# HTTP Mock
curl http://localhost:8080/api/mock/{projectCode}/{apiPath}

# WebSocket Mock
new WebSocket('ws://localhost:8080/api/ws/mock/{projectCode}/{path}')
```

---

## 🤖 AI 能力

| 能力 | 说明 |
|------|------|
| 💬 智能对话 | 多轮上下文记忆 · SSE 流式响应 · Markdown 渲染 + 代码高亮 · 对话历史持久化 · 智能建议引导 |
| 🎨 内容生成 | 一键生成响应数据 · Java 代码模板 · HTML 邮件模板 · 接口描述文档 |
| 🔌 多模型支持 | OpenAI · Azure · Gemini · Claude · DeepSeek · 通义千问 · 智谱GLM · Moonshot · 百川 · MiniMax · 小米MiMo · 火山引擎豆包 + 自定义兼容 |
| 📊 调用统计 | 多用户 AI 调用趋势折线图 · 年/月/日粒度 · 成功率监控 |

---

## 📁 项目结构

```
mock-server/
├── backend/                          # Spring Boot
│   └── src/.../mockserver/
│       ├── config/                   # Security / Web / CORS / 邮件
│       ├── controller/               # REST 控制器
│       ├── entity/                   # JPA 实体
│       ├── filter/                   # JWT 认证过滤器
│       ├── plugin/                   # 动态编译器 & 响应处理器
│       ├── repository/               # Spring Data JPA
│       ├── service/                  # 业务逻辑
│       └── util/                     # 工具类
├── frontend/                         # Vue 3
│   └── src/
│       ├── locales/                  # 国际化 (zh-CN / en-US / ja-JP)
│       ├── views/                    # 页面组件
│       ├── layout/                   # 布局组件
│       ├── stores/                   # Pinia 状态管理
│       └── api/                      # Axios 封装
├── docker/                           # Docker Compose + Dockerfile
├── build.sh / run.sh                 # 构建 & 启动
├── CHANGELOG.md                      # 变更日志
└── README.md
```

---

## ⚙️ 关键配置

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 1800000                 # 30 分钟
admin:
  username: ${ADMIN_USERNAME:admin}
  password: ${ADMIN_PASSWORD:}
```

```bash
# 环境变量
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

---

## 🔒 安全

| 特性 | 说明 |
|------|------|
| JWT 认证 | 无状态 Token，过期时间可配 |
| 强密码策略 | 大小写 + 数字 + 特殊字符，最少 8 位 |
| 登录锁定 | 多次失败后临时锁定 |
| IP 白名单 | 限制管理接口来源 |
| CORS | 跨域白名单控制 |
| 防注入 | SQL 参数化 · XSS 过滤 · CSRF 防护 |
| RBAC | 角色-权限体系，页面/按钮级控制，动态菜单显隐 |

---

## 📄 许可证

Apache License 2.0 · Copyright © 2024-2026 carolcoral · Author: LXW

---

## 📧 联系

GitHub [github.com/carolcoral](https://github.com/carolcoral) · Email lxw@cnkj.site

<p align="center"><sub>Made with ❤️ by <a href="https://github.com/carolcoral">carolcoral</a></sub></p>
