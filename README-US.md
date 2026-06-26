<p align="center">
  <img src="https://img.shields.io/badge/Version-2.3.0-blue?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/License-Apache%202.0-green?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/JDK-21-red?style=flat-square&logo=openjdk" alt="JDK">
  <img src="https://img.shields.io/badge/Node.js-18+-green?style=flat-square&logo=nodedotjs" alt="Node">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-brightgreen?style=flat-square&logo=vuedotjs" alt="Vue">
</p>

<h1 align="center">Mock Server</h1>
<p align="center">Full-featured API Mock Platform with Spring Boot + Vue 3</p>

<p align="center">
  HTTP / WebSocket Mock · Multi-project Isolation · Fine-grained RBAC · Dynamic Code Compilation · AI Chat & Generation · Swagger Import · i18n · Email Notifications · System Monitoring
</p>

---

## ✨ Core Features

| Module | Capabilities |
|------|------|
| 🧩 API Mocking | Fixed responses / Conditional matching / Weighted random / Java dynamic code handlers / Delay simulation / WebSocket Mock |
| 🔐 Fine-grained RBAC | Role-based access control with page & button-level permissions, custom roles, dynamic menu visibility |
| 🤖 AI Platform | Multi-model chat (SSE streaming + Markdown rendering + context memory) · One-click generation of response data / Java code / email templates / API descriptions · 12+ LLM providers · Call statistics |
| 📥 Swagger Import | Supports Swagger 2.0 / OpenAPI 3.x, upload JSON or URL to import as Mock APIs in one click |
| 📦 Project Management | Multi-project isolation · Tiered member permissions (Creator / Admin / Member) |
| 🧬 Code Templates | Monaco Editor · Compile validation · Project-level & system-level templates · Hot reload · 6 transformer types |
| ✉️ Email System | SMTP configuration · Template management (General / Verification / Alert) · Live HTML preview · Registration verification codes |
| 🌍 Internationalization | Chinese / English / 日本語 · Full-site real-time switching |
| 📊 Monitoring Dashboard | Request trends · Source IP multi-line charts · AI call volume · Creation trends · IOPS · JVM heap details · CPU/Memory/Disk |
| 📖 User Guide | Interactive step-by-step guide dialog, 6 steps: Create Project → Import APIs → Configure Responses → Parameter Matching → AI Generation → Invoke & Monitor |
| 🎨 Modern UI | Dark gradient sidebar · Canvas dynamic lines · Collapsible grouped menu · Compact mode · Full-screen welcome page |
| 🐳 Containerization | Docker multi-stage build · One-click scripts · Non-root read-only container |

---

## 🚀 Quick Start

Requirements: JDK 21+ · Maven 3.6+ · Node.js 18+ · npm 9+

```bash
# One-click start (auto builds frontend & backend)
git clone https://github.com/carolcoral/mock-server.git && cd mock-server
./run.sh
```

Access: `http://localhost:8080` | API: `/api`

<details>
<summary>📖 More Deployment Options</summary>

```bash
# Manual build
./build-all-in-one.sh
java -jar backend/target/mock-server-2.3.0.jar

# Development mode
cd backend && mvn spring-boot:run              # Terminal 1
cd frontend && npm install && npm run dev       # Terminal 2

# Docker
cd docker && docker-compose up -d
```

> See [BUILD_README.md](./BUILD_README.md) for build instructions
</details>

---

## 🔐 Default Credentials

| Environment | Username | Password | Notes |
|------|--------|------|------|
| Development | `admin` | `Admin@123` | Auto-created on first startup |
| Production | `$ADMIN_USERNAME` | `$ADMIN_PASSWORD` | Must set a strong password |

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
```

---

## 📚 Response Modes

| Mode | Description | Use Case |
|------|------|------|
| Fixed Response | Predefined JSON / text | Status codes 200 / 404 / 500 |
| Conditional Response | Dynamic matching by request params | `$.userId == '123'` → return user data |
| Random Response | Weighted random selection | Canary release simulation |
| Code Handler | Java dynamic compilation, can read/write DB and call external APIs | Data masking, format wrapping |

```bash
# HTTP Mock
curl http://localhost:8080/api/mock/{projectCode}/{apiPath}

# WebSocket Mock
new WebSocket('ws://localhost:8080/api/ws/mock/{projectCode}/{path}')
```

---

## 🤖 AI Capabilities

| Capability | Description |
|------|------|
| 💬 Smart Chat | Multi-turn context memory · SSE streaming response · Markdown rendering + code highlighting · Persistent chat history · Smart suggestions |
| 🎨 Content Generation | One-click generation of response data · Java code templates · HTML email templates · API description docs |
| 🔌 Multi-Model Support | OpenAI · Azure · Gemini · Claude · DeepSeek · Qwen · GLM · Moonshot · Baichuan · MiniMax · Xiaomi MiMo · Volcano Engine Doubao + Custom compatible |
| 📊 Call Statistics | Multi-user AI call trend charts · Year/Month/Day granularity · Success rate monitoring |

> 💡 All AI generation features (code templates, email templates, API descriptions) use SSE streaming transmission with flexible timeout configuration (30-600 seconds, default 15 minutes in production).

---

## 📁 Project Structure

```
mock-server/
├── backend/                          # Spring Boot
│   └── src/.../mockserver/
│       ├── config/                   # Security / Web / CORS / Email
│       ├── controller/               # REST Controllers
│       ├── entity/                   # JPA Entities
│       ├── filter/                   # JWT Authentication Filters
│       ├── plugin/                   # Dynamic Compiler & Response Handlers
│       ├── repository/               # Spring Data JPA
│       ├── service/                  # Business Logic
│       └── util/                     # Utilities
├── frontend/                         # Vue 3
│   └── src/
│       ├── locales/                  # i18n (zh-CN / en-US / ja-JP)
│       ├── views/                    # Page Components
│       ├── layout/                   # Layout Components
│       ├── stores/                   # Pinia State Management
│       └── api/                      # Axios Wrapper
├── docker/                           # Docker Compose + Dockerfile
├── build.sh / run.sh                 # Build & Start Scripts
├── CHANGELOG.md                      # Changelog
├── README.md                         # README (Chinese)
└── README-US.md                      # README (English)
```

---

## ⚙️ Key Configuration

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 1800000                 # 30 minutes
admin:
  username: ${ADMIN_USERNAME:admin}
  password: ${ADMIN_PASSWORD:}
```

```bash
# Environment variables
export JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
export ADMIN_PASSWORD=YourStrongP@ssw0rd123
export ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

---

## 🔒 Security

| Feature | Description |
|------|------|
| JWT Authentication | Stateless tokens, configurable expiration |
| Strong Password Policy | Uppercase + lowercase + digits + special characters, minimum 8 characters |
| Login Lockout | Temporary lockout after multiple failures |
| IP Whitelist | Restrict management API source IPs |
| CORS | Cross-origin whitelist control |
| Injection Prevention | SQL parameterization · XSS filtering · CSRF protection |
| RBAC | Role-permission system, page/button-level control, dynamic menu visibility |

---

## 📄 License

Apache License 2.0 · Copyright © 2024-2026 carolcoral · Author: LXW

---

## 📧 Contact

GitHub [github.com/carolcoral](https://github.com/carolcoral) · Email lxw@cnkj.site

<p align="center"><sub>Made with ❤️ by <a href="https://github.com/carolcoral">carolcoral</a></sub></p>
