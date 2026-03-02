# 腾讯云 CNB 云原生构建部署指南

本文档介绍如何使用腾讯云 CloudBase (TCB) 云原生构建部署 Mock Server 项目，实现在线访问和调试。

## 📋 部署前准备

### 1. 腾讯云账号准备

- [ ] 注册腾讯云账号：https://cloud.tencent.com/
- [ ] 完成实名认证
- [ ] 开通云开发 CloudBase 服务
- [ ] 创建云开发环境

### 2. 本地环境准备

- [ ] 安装 Node.js 18+
- [ ] 安装 Maven 3.6+
- [ ] 安装 JDK 17+
- [ ] 安装 CloudBase CLI（可选）

```bash
# 安装 CloudBase CLI
npm install -g @cloudbase/cli

# 登录
cloudbase login
```

## 🚀 部署方式一：通过腾讯云控制台（推荐）

### 步骤 1：上传代码到代码仓库

#### 方式 A：使用 GitHub/GitLab

1. 在 GitHub 或 GitLab 创建仓库
2. 将项目代码推送到仓库：

```bash
# 初始化git仓库（如果还没有）
git init
git add .
git commit -m "Initial commit: Mock Server"

# 关联远程仓库
git remote add origin https://github.com/your-username/mock-server.git

# 推送代码
git push -u origin main
```

#### 方式 B：直接上传 ZIP 包

1. 打包项目：

```bash
# 在项目根目录执行
zip -r mock-server.zip . -x "*.git*" -x "*/target/*" -x "*/node_modules/*"
```

2. 登录腾讯云控制台
3. 进入云开发 CloudBase
4. 创建应用并上传 ZIP 包

### 步骤 2：创建云原生应用

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **云开发 CloudBase** > **应用管理**
3. 点击 **新建应用**
4. 选择 **从源码创建**
5. 配置应用信息：
   - **应用名称**：mock-server
   - **环境**：选择已创建的云开发环境
   - **源码来源**：选择 Git 仓库或 ZIP 包
   - **构建配置**：选择 **云原生构建**

### 步骤 3：配置构建和部署

项目已经配置好了构建文件：

- `tcb.yaml` - 腾讯云 CloudBase 配置文件
- `backend/project.toml` - Cloud Native Buildpacks 配置
- `docker/Dockerfile` - Docker 镜像配置

在云原生构建配置中，系统会自动识别这些文件。

#### 关键配置说明

**tcb.yaml** 中的主要配置：

```yaml
# 应用名称
appName: mock-server

# 环境变量（生产环境请修改敏感信息）
env:
  variables:
    JWT_SECRET: your-secret-key-here  # 修改为自己的密钥
    ADMIN_PASSWORD: Admin@123          # 修改管理员密码
```

**重要**：部署前请修改以下敏感配置：
- `JWT_SECRET` - JWT 加密密钥
- `ADMIN_PASSWORD` - 管理员密码
- `ADMIN_USERNAME` - 管理员用户名（可选）

### 步骤 4：启动部署

1. 确认配置无误后，点击 **创建并部署**
2. 等待构建完成（约 5-10 分钟）
3. 构建完成后，系统会自动部署

### 步骤 5：访问应用

部署完成后，你可以在应用列表中看到：

- **前端访问地址**：`https://your-app-id.tcloudbaseapp.com`
- **后端 API 地址**：`https://your-app-id.tcloudbaseapp.com/api`
- **Swagger 文档**：`https://your-app-id.tcloudbaseapp.com/api/swagger-ui.html`

## 🚀 部署方式二：使用 CloudBase CLI（命令行）

### 步骤 1：安装并登录 CLI

```bash
# 安装 CloudBase CLI
npm install -g @cloudbase/cli

# 登录
cloudbase login
# 按照提示扫描二维码登录
```

### 步骤 2：初始化项目

```bash
# 在云开发环境中初始化
cd /workspace
cloudbase init

# 选择或创建环境
# 选择模板: 自定义应用
```

### 步骤 3：配置环境变量

```bash
# 设置环境变量
cloudbase env:set JWT_SECRET=your-secret-key-here
cloudbase env:set ADMIN_PASSWORD=Admin@123
cloudbase env:set SPRING_PROFILES_ACTIVE=cloud
```

### 步骤 4：部署应用

```bash
# 部署应用
cloudbase framework:deploy

# 或者简写
tcb framework:deploy
```

### 步骤 5：查看部署状态

```bash
# 查看应用状态
cloudbase app:list

# 查看日志
cloudbase app:logs mock-server
```

## 🚀 部署方式三：持续集成/持续部署 (CI/CD)

### GitHub Actions 配置

在项目根目录创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy to Tencent Cloud

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: Build backend
      run: |
        cd backend
        mvn clean package -DskipTests
    
    - name: Build frontend
      run: |
        cd frontend
        npm install
        npm run build
    
    - name: Deploy to Tencent Cloud
      uses: TencentCloud/cloudbase-action@v2
      with:
        secretId: ${{ secrets.TENCENT_CLOUD_SECRET_ID }}
        secretKey: ${{ secrets.TENCENT_CLOUD_SECRET_KEY }}
        envId: ${{ secrets.TENCENT_CLOUD_ENV_ID }}
        # 部署配置
        projectPath: ./
        configPath: ./tcb.yaml
```

### 配置 GitHub Secrets

在 GitHub 仓库设置中添加以下 Secrets：

- `TENCENT_CLOUD_SECRET_ID` - 腾讯云 SecretId
- `TENCENT_CLOUD_SECRET_KEY` - 腾讯云 SecretKey
- `TENCENT_CLOUD_ENV_ID` - 云开发环境 ID

## 🔧 生产环境配置优化

### 1. 数据库配置（推荐）

SQLite 适合开发环境，生产环境建议使用 MySQL：

**tcb.yaml 中添加 MySQL 配置：**

```yaml
database:
  - name: mock-server-db
    type: mysql
    config:
      instanceId: ${env.MYSQL_INSTANCE_ID}
      databaseName: mock_server
      username: ${env.DB_USER}
      password: ${env.DB_PASSWORD}
```

然后在 `application.yml` 中添加 MySQL 配置：

```yaml
spring:
  profiles:
    active: cloud
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&serverTimezone=UTC
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 环境变量配置

在云开发控制台中设置以下环境变量：

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `JWT_SECRET` | JWT 加密密钥 | 随机生成 32 位以上字符串 |
| `ADMIN_USERNAME` | 管理员用户名 | admin |
| `ADMIN_PASSWORD` | 管理员密码 | 强密码 |
| `SPRING_PROFILES_ACTIVE` | Spring 环境 | cloud |
| `JAVA_OPTS` | JVM 参数 | -Xmx1g -Xms512m |

### 3. 域名配置

#### 配置自定义域名

1. 在云开发控制台 > 应用管理 > 域名管理
2. 点击 **添加域名**
3. 输入你的域名（如：mock.yourdomain.com）
4. 按照提示配置 DNS 解析
5. 启用 HTTPS（推荐）

#### 更新前端配置

修改 `frontend/.env.production`：

```bash
VITE_APP_BASE_URL=https://your-domain.com
VITE_APP_BASE_API=/api
```

## 📊 监控和日志

### 查看应用日志

**通过控制台：**
1. 云开发 CloudBase > 应用管理
2. 选择应用 > 查看日志
3. 支持实时日志和日志查询

**通过 CLI：**
```bash
# 查看实时日志
cloudbase app:logs mock-server -f

# 查询历史日志
cloudbase app:logs mock-server --since 1h
```

### 监控指标

在云开发控制台可以查看：
- CPU 使用率
- 内存使用率
- 网络流量
- 请求数
- 响应时间

### 告警配置

**tcb.yaml** 中已经预配置了告警规则：

```yaml
monitoring:
  alerting:
    enabled: true
    rules:
      - name: high-cpu
        condition: cpu > 80
        duration: 5m
      - name: high-memory
        condition: memory > 85
        duration: 5m
      - name: error-rate
        condition: error_rate > 5
        duration: 3m
```

## 🐛 常见问题排查

### 1. 构建失败

**问题**：构建超时或失败

**解决方案**：
- 检查代码是否完整上传
- 查看构建日志，定位具体错误
- 增加构建资源限制（tcb.yaml）
- 确保 Maven 和 npm 依赖可以正常下载

```yaml
# 增加构建资源
build:
  resources:
    limits:
      cpu: 2000m
      memory: 2048Mi
```

### 2. 应用启动失败

**问题**：容器启动后立即退出

**解决方案**：
- 查看应用日志，检查错误信息
- 确保数据库连接正常
- 检查环境变量配置
- 验证端口配置是否正确

### 3. 数据库问题

**问题**：SQLite 在多实例下无法共享

**解决方案**：
- 使用云开发 MySQL 数据库
- 或使用云开发 NoSQL 数据库（需要修改代码）

### 4. 内存溢出

**问题**：应用 OOM（Out of Memory）

**解决方案**：
- 增加内存限制（tcb.yaml）
- 优化 JVM 参数：

```yaml
env:
  variables:
    JAVA_OPTS: -Xmx2g -Xms1g -XX:+UseG1GC
```

### 5. 端口冲突

**问题**：端口被占用或无法访问

**解决方案**：
- 确保端口配置正确（默认 8080）
- 检查健康检查端口
- 验证安全组规则

## 🔄 持续部署

### 自动触发部署

配置 Git 仓库的 Webhook，实现代码推送自动部署：

1. 在云开发控制台 > 应用管理 > 部署设置
2. 选择 **Git 触发部署**
3. 配置 Webhook URL
4. 在 Git 仓库设置中添加 Webhook

### 蓝绿部署

```yaml
# tcb.yaml 中配置
deploy:
  strategy:
    type: blue-green  # 蓝绿部署
    traffic:
      - version: v1
        weight: 100   # 100% 流量到旧版本
      - version: v2
        weight: 0     # 0% 流量到新版本
```

逐步切换流量：
```bash
cloudbase app:update-traffic --version=v2 --weight=50
cloudbase app:update-traffic --version=v2 --weight=100
```

## 📱 访问应用

部署成功后，你会获得以下访问地址：

### 前端应用
```
https://your-env-id.tcloudbaseapp.com
```

### 后端 API
```
https://your-env-id.tcloudbaseapp.com/api
```

### Swagger 文档
```
https://your-env-id.tcloudbaseapp.com/api/swagger-ui.html
```

### WebSocket
```
wss://your-env-id.tcloudbaseapp.com/api/ws/mock/{projectCode}/{path}
```

## 🎉 验证部署

### 1. 登录系统

访问：`https://your-env-id.tcloudbaseapp.com`

使用管理员账号登录：
- 用户名：`admin`
- 密码：`Admin@123`（或你配置的密码）

### 2. 测试 Mock 接口

```bash
curl -X POST https://your-env-id.tcloudbaseapp.com/api/mock/demo/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 3. 查看 Swagger 文档

访问：`https://your-env-id.tcloudbaseapp.com/api/swagger-ui.html`

输入账号密码查看所有 API 接口。

## 📝 更新和回滚

### 更新应用

```bash
# 修改代码后重新部署
cloudbase framework:deploy

# 只部署后端
cd backend && cloudbase framework:deploy

# 只部署前端
cd frontend && cloudbase framework:deploy
```

### 回滚版本

```bash
# 查看历史版本
cloudbase app:versions mock-server

# 回滚到指定版本
cloudbase app:rollback mock-server --version v1.0.1
```

## 🔒 安全建议

### 生产环境必做事项

1. **修改默认密码**
   - 修改 `ADMIN_PASSWORD`
   - 修改 `JWT_SECRET`

2. **启用 HTTPS**
   - 配置自定义域名
   - 启用 SSL 证书

3. **访问控制**
   - 配置 IP 白名单（可选）
   - 限制 Swagger 访问

4. **数据备份**
   - 定期备份数据库
   - 使用云开发自动备份功能

5. **监控告警**
   - 配置告警规则
   - 关注异常日志

## 💡 最佳实践

1. **环境分离**
   - 开发环境：使用 SQLite
   - 生产环境：使用 MySQL

2. **配置管理**
   - 敏感信息使用环境变量
   - 不将密钥提交到代码仓库

3. **资源规划**
   - 根据访问量调整资源限制
   - 配置合理的自动扩缩容策略

4. **日志管理**
   - 开启日志收集
   - 定期清理旧日志

5. **性能优化**
   - 监控缓存命中率
   - 优化数据库查询

## 📞 技术支持

- 腾讯云文档：https://cloud.tencent.com/document/product/876
- CloudBase CLI 文档：https://docs.cloudbase.net/cli/intro/
- 腾讯云社区：https://cloud.tencent.com/developer/ask

---

## 快速部署检查清单

部署前请确认：

- [ ] 已开通云开发环境
- [ ] 已配置环境变量（JWT_SECRET、ADMIN_PASSWORD 等）
- [ ] 已修改默认密码和密钥
- [ ] 已配置 MySQL 数据库（生产环境推荐）
- [ ] 已配置自定义域名（可选）
- [ ] 已开启监控告警

部署后请验证：

- [ ] 前端可以正常访问
- [ ] 后端 API 可以正常访问
- [ ] Swagger 文档可以正常访问
- [ ] 可以正常登录系统
- [ ] Mock 接口可以正常调用
- [ ] WebSocket 连接正常（可选）
