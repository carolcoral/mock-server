# 腾讯云 CNB 云原生构建部署指南

本文档介绍如何使用腾讯云 CloudBase (TCB) 云原生构建部署 Mock Server 项目，实现在线访问和调试。

## 🎯 推荐的部署方式

腾讯云推荐使用 **`.cnb.yml`** 文件配置自动化构建流程，这种方式相比传统的 `tcb.yaml` 更加灵活和强大。

### `.cnb.yml` vs `tcb.yaml`

| 特性 | `.cnb.yml` (推荐) | `tcb.yaml` (传统) |
|------|-------------------|-------------------|
| 构建阶段控制 | ✅ 6个阶段精细控制 | ❌ 有限控制 |
| 构建缓存 | ✅ 支持多路径缓存 | ⚠️ 有限支持 |
| 产出物管理 | ✅ 详细的产出物配置 | ❌ 基础支持 |
| 镜像构建 | ✅ 完整 Docker 支持 | ⚠️ 有限支持 |
| 多环境部署 | ✅ dev/test/prod | ✅ 支持 |
| 自动扩缩容 | ✅ 详细配置 | ✅ 支持 |
| 监控告警 | ✅ 内置规则 | ⚠️ 基础支持 |
| 安全扫描 | ✅ 镜像和依赖扫描 | ❌ 不支持 |
| 成本优化 | ✅ 预留实例、自动启停 | ❌ 不支持 |

## 📋 部署前准备

### 1. 腾讯云账号准备

- [ ] 注册腾讯云账号：https://cloud.tencent.com/
- [ ] 完成实名认证
- [ ] 开通云开发 CloudBase 服务
- [ ] 创建云开发环境
- [ ] 开通容器镜像服务（TCR）

### 2. 本地环境准备

- [ ] 安装 Node.js 18+
- [ ] 安装 Maven 3.6+
- [ ] 安装 JDK 17+
- [ ] 安装 Docker（可选）
- [ ] 安装 CloudBase CLI（可选）

```bash
# 安装 CloudBase CLI
npm install -g @cloudbase/cli

# 登录
cloudbase login
```

## 🚀 部署方式一：使用 `.cnb.yml` 配置（最新推荐）

### 步骤 1：配置 `.cnb.yml`

项目已创建 `.cnb.yml` 文件，位于项目根目录。你需要修改以下配置：

#### 1.1 修改敏感信息（必须！）

编辑 `.cnb.yml`：

```yaml
# 环境变量配置
env:
  # JWT配置（必须修改！）
  - name: JWT_SECRET
    value: ${JWT_SECRET}
    required: true
    description: "JWT加密密钥（必须自定义）"
  
  # 管理员密码（必须修改！）
  - name: ADMIN_PASSWORD
    value: ${ADMIN_PASSWORD}
    required: true
    secret: true
    description: "管理员密码（必须自定义）"
```

**生成随机密钥：**

```bash
# Linux/Mac
openssl rand -base64 32

# 或使用脚本
./deploy-tencent-cloud.sh
```

#### 1.2 配置镜像仓库（必须！）

在 `.cnb.yml` 中配置腾讯云镜像仓库（TCR）：

```yaml
# 步骤5: 镜像推送
- name: push-image
  env:
    - name: TCR_USERNAME
      value: ${TCR_USERNAME}  # 腾讯云镜像仓库用户名
    - name: TCR_PASSWORD
      value: ${TCR_PASSWORD}  # 腾讯云镜像仓库密码
    - name: TCR_NAMESPACE
      value: ${TCR_NAMESPACE:-"default"}  # 命名空间
```

#### 1.3 配置数据库（可选）

**方式A：使用腾讯云 MySQL（生产环境推荐）**

```yaml
database:
  - type: mysql
    name: mock-server-db
    config:
      host: ${MYSQL_HOST}
      port: ${MYSQL_PORT:-3306}
      database: ${MYSQL_DATABASE:-mock_server}
      username: ${MYSQL_USERNAME}
      password: ${MYSQL_PASSWORD}
```

**方式B：使用 SQLite（开发环境）**

无需配置，系统默认使用 SQLite。

### 步骤 2：上传代码

#### 方式 A：使用 Git 仓库（推荐）

1. 在 GitHub、GitLab 或腾讯云代码仓库创建项目
2. 推送代码：

```bash
# 初始化git仓库（如果还没有）
git init
git add .
git commit -m "Initial commit: Mock Server with .cnb.yml"

# 关联远程仓库
git remote add origin https://github.com/your-username/mock-server.git

# 推送代码
# 系统会自动识别 .cnb.yml 文件
git push -u origin main
```

#### 方式 B：直接上传 ZIP 包

```bash
# 打包项目（包含 .cnb.yml）
zip -r mock-server.zip . -x "*.git*" -x "*/target/*" -x "*/node_modules/*"
```

然后在腾讯云控制台上传 ZIP 包。

### 步骤 3：创建云原生应用

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **云开发 CloudBase** > **应用管理**
3. 点击 **新建应用**
4. 选择 **从源码创建**
5. 配置应用信息：
   - **应用名称**：mock-server
   - **环境**：选择已创建的云开发环境
   - **源码来源**：选择 Git 仓库或 ZIP 包
   - **构建配置**：选择 **云原生构建**
   - **构建配置文件**：系统会自动识别 `.cnb.yml`

### 步骤 4：配置构建触发器（可选）

在 `.cnb.yml` 中配置自动构建触发器：

```yaml
ci:
  triggers:
    # 代码提交触发
    - type: push
      branches:
        - main
        - master
    
    # 定时构建（每天凌晨3点）
    - type: schedule
      cron: "0 3 * * *"
```

### 步骤 5：启动部署

1. 确认配置无误后，点击 **创建并部署**
2. 查看构建日志，系统会按照 `.cnb.yml` 定义的步骤执行：
   - 步骤1：准备构建环境
   - 步骤2：构建后端（Maven）
   - 步骤3：构建前端（npm）
   - 步骤4：构建 Docker 镜像
   - 步骤5：推送镜像到 TCR
   - 步骤6：生成部署清单
3. 等待部署完成（约 5-10 分钟）

### 步骤 6：访问应用

部署完成后，你可以在应用列表中看到：

- **访问地址**：`https://your-app-id.tcloudbaseapp.com`
- **构建历史**：查看每次构建的详细日志
- **部署状态**：实时监控部署状态

## 🚀 部署方式二：通过腾讯云控制台（传统方式）

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

## 🚀 部署方式三：使用 `.cnb.yml` 自动化部署（推荐）

### GitHub Actions + `.cnb.yml` 配置

结合 GitHub Actions 和 `.cnb.yml` 实现完整的 CI/CD 流水线：

在项目根目录创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy to Tencent Cloud with CNB

on:
  push:
    branches: [ main ]
    paths:
      - 'backend/**'
      - 'frontend/**'
      - '.cnb.yml'
  pull_request:
    branches: [ main ]

jobs:
  build-and-deploy:
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
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Build backend
      run: |
        cd backend
        mvn clean package -DskipTests
    
    - name: Build frontend
      run: |
        cd frontend
        npm ci
        npm run build
    
    - name: Deploy to Tencent Cloud
      uses: TencentCloud/cloudbase-action@v2
      with:
        secretId: ${{ secrets.TENCENT_CLOUD_SECRET_ID }}
        secretKey: ${{ secrets.TENCENT_CLOUD_SECRET_KEY }}
        envId: ${{ secrets.TENCENT_CLOUD_ENV_ID }}
        # 使用 .cnb.yml 配置
        useConfigFile: true
        configFilePath: ./.cnb.yml
```

### GitLab CI/CD + `.cnb.yml` 配置

在项目根目录创建 `.gitlab-ci.yml`：

```yaml
stages:
  - build
  - deploy

# 构建后端
build-backend:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - cd backend
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - backend/target/mock-server-*.jar
    expire_in: 1 hour
  cache:
    paths:
      - ~/.m2/repository

# 构建前端
build-frontend:
  stage: build
  image: node:18
  script:
    - cd frontend
    - npm ci --cache .npm --prefer-offline
    - npm run build
  artifacts:
    paths:
      - frontend/dist/
    expire_in: 1 hour
  cache:
    paths:
      - frontend/.npm/
      - frontend/node_modules/

# 部署到腾讯云
deploy-tencent-cloud:
  stage: deploy
  image: tencentcloud/tencentcloud-cli:latest
  script:
    - echo $TENCENT_SECRET_ID | tcl configure set secretId
    - echo $TENCENT_SECRET_KEY | tcl configure set secretKey
    - tcb app:deploy --use-config .cnb.yml --envId $TENCENT_ENV_ID
  only:
    - main
  when: manual  # 手动触发部署
```

### 配置 Git Secrets

在 GitHub 仓库设置中添加以下 Secrets：

- `TENCENT_CLOUD_SECRET_ID` - 腾讯云 SecretId
- `TENCENT_CLOUD_SECRET_KEY` - 腾讯云 SecretKey
- `TENCENT_CLOUD_ENV_ID` - 云开发环境 ID

## 📖 `.cnb.yml` 配置详解

### 构建流程

`.cnb.yml` 定义了完整的 6 步构建流程：

#### Step 1: 准备构建环境

```yaml
- name: prepare-build-env
  run: |
    # 安装 Node.js 18
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
    
    # 验证安装
    node --version
    npm --version
    java -version
    mvn --version
  
  cache:
    paths:
      - /cache/maven
      - /cache/node_modules
      - /root/.m2
```

**功能**：
- 安装 Node.js 18 和 npm
- 验证所有依赖安装
- 配置构建缓存

#### Step 2: 构建后端

```yaml
- name: build-backend
  run: |
    cd backend
    
    # 配置 Maven（使用阿里云镜像）
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << 'EOF'
    <settings>
      <mirrors>
        <mirror>
          <id>aliyun</id>
          <mirrorOf>central</mirrorOf>
          <name>Aliyun Maven</name>
          <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
      </mirrors>
    </settings>
    EOF
    
    # 编译和打包
    mvn clean compile -q
    mvn package -DskipTests -q
  
  artifacts:
    paths:
      - backend/target/mock-server-*.jar
  
  cache:
    paths:
      - ~/.m2
      - /cache/maven
```

**功能**：
- 配置 Maven 阿里云镜像（加速下载）
- 编译 Java 代码
- 打包 Spring Boot JAR 文件
- 产出物：JAR 文件

#### Step 3: 构建前端

```yaml
- name: build-frontend
  run: |
    cd frontend
    
    # 配置 npm 镜像
    npm config set registry https://registry.npmmirror.com
    
    # 安装依赖
    npm ci --prefer-offline --no-audit --no-fund
    
    # 构建生产版本
    npm run build
  
  artifacts:
    paths:
      - frontend/dist/**/**
  
  cache:
    paths:
      - frontend/node_modules
      - ~/.npm
```

**功能**：
- 配置 npm 淘宝镜像
- 安装依赖（使用 `npm ci` 保证一致性）
- 构建 Vue 3 生产版本
- 产出物：dist 目录

#### Step 4: 构建 Docker 镜像

```yaml
- name: build-docker-image
  run: |
    # 创建 Dockerfile
    cat > docker/Dockerfile << 'EOF'
    FROM ccr.ccs.tencentyun.com/tencentio/openjdk:17-jre-slim
    
    LABEL maintainer="carolcoral"
    LABEL description="Mock Server - API接口模拟服务器"
    
    WORKDIR /app
    RUN mkdir -p /app/data /app/logs
    
    COPY backend/target/mock-server-*.jar /app/mock-server.jar
    COPY frontend/dist /app/public
    
    EXPOSE 8080
    
    HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
        CMD curl -f http://localhost:8080/api/v3/api-docs || exit 1
    
    ENTRYPOINT ["java", "-jar", "/app/mock-server.jar"]
    EOF
    
    # 构建镜像
    docker build -t mock-server:${BUILD_ID:-latest} -f docker/Dockerfile .
  
  artifacts:
    paths:
      - docker/Dockerfile
```

**功能**：
- 创建优化的 Dockerfile
- 使用腾讯云 OpenJDK 镜像（加速下载）
- 复制 JAR 和前端静态文件
- 配置健康检查
- 产出物：Docker 镜像

#### Step 5: 推送镜像到腾讯云仓库

```yaml
- name: push-image
  run: |
    # 获取镜像仓库信息
    TCR_NAMESPACE=${TCR_NAMESPACE:-"your-namespace"}
    TCR_REGION=${TCR_REGION:-"ap-guangzhou"}
    IMAGE_TAG=${BUILD_ID:-latest}
    
    # 打标签
    docker tag mock-server:${IMAGE_TAG} ccr.ccs.tencentyun.com/${TCR_NAMESPACE}/mock-server:${IMAGE_TAG}
    
    # 登录并推送
    echo ${TCR_PASSWORD} | docker login --username=${TCR_USERNAME} --password-stdin ccr.ccs.tencentyun.com
    docker push ccr.ccs.tencentyun.com/${TCR_NAMESPACE}/mock-server:${IMAGE_TAG}
  
  env:
    - name: TCR_USERNAME
      value: ${TCR_USERNAME}
    - name: TCR_PASSWORD
      value: ${TCR_PASSWORD}
    - name: TCR_NAMESPACE
      value: ${TCR_NAMESPACE:-"default"}
```

**功能**：
- 打标签（符合腾讯云镜像仓库规范）
- 登录腾讯云镜像仓库
- 推送镜像
- 产出物：远程镜像

#### Step 6: 生成部署清单

```yaml
- name: generate-deployment-manifest
  run: |
    cat > deployment.yaml << EOF
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: mock-server
      labels:
        app: mock-server
        version: ${BUILD_ID:-latest}
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: mock-server
      template:
        metadata:
          labels:
            app: mock-server
            version: ${BUILD_ID:-latest}
        spec:
          containers:
          - name: mock-server
            image: ccr.ccs.tencentyun.com/${TCR_NAMESPACE:-default}/mock-server:${BUILD_ID:-latest}
            ports:
            - containerPort: 8080
              name: http
            env:
            - name: SPRING_PROFILES_ACTIVE
              value: "cloud"
            resources:
              requests:
                cpu: 500m
                memory: 512Mi
              limits:
                cpu: 1000m
                memory: 1024Mi
    ...
    EOF
```

**功能**：
- 生成 Kubernetes Deployment
- 生成 Service
- 生成 Ingress
- 配置健康检查
- 配置资源限制

### 缓存优化

`.cnb.yml` 配置了多级缓存，大幅提升构建速度：

```yaml
# Maven 缓存
cache:
  paths:
    - ~/.m2
    - /cache/maven

# npm 缓存
cache:
  paths:
    - frontend/node_modules
    - ~/.npm
```

**缓存效果**：
- 首次构建：完整下载依赖（约 3-5 分钟）
- 后续构建：使用缓存（约 1-2 分钟）
- 提升效率：**60% 以上**

### 多环境部署

`.cnb.yml` 支持 3 个环境的部署配置：

```yaml
deploy:
  environments:
    # 开发环境
    - name: dev
      replicas: 1
      resources:
        cpu: 500m
        memory: 512Mi
      autoDeploy:
        enabled: true
        branches:
          - develop
    
    # 测试环境
    - name: test
      replicas: 2
      resources:
        cpu: 1000m
        memory: 1024Mi
    
    # 生产环境
    - name: prod
      replicas: 3
      resources:
        cpu: 2000m
        memory: 2048Mi
      autoscaling:
        enabled: true
        minReplicas: 3
        maxReplicas: 10
```

### 自动扩缩容

生产环境启用自动扩缩容：

```yaml
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: cpu
      target:
        type: Utilization
        averageUtilization: 70
    - type: memory
      target:
        type: Utilization
        averageUtilization: 80
```

**触发条件**：
- CPU 使用率 > 70%
- 内存使用率 > 80%
- 自动扩容到最多 10 个实例
- 负载降低后自动缩容到 3 个实例

### 监控告警

内置 4 个告警规则：

```yaml
alerting:
  rules:
    - name: high_cpu_usage
      expr: cpu_usage > 80
      for: 5m
      severity: warning
    
    - name: high_memory_usage
      expr: memory_usage > 85
      for: 5m
      severity: warning
    
    - name: high_error_rate
      expr: error_rate > 5
      for: 3m
      severity: critical
    
    - name: pod_crash_looping
      expr: pod_restart_count > 5
      for: 10m
      severity: critical
```

### 安全加固

#### 镜像安全扫描

```yaml
security:
  imageScan:
    enabled: true
    severity:
      - HIGH
      - CRITICAL
    maxVulnerabilities: 0
```

#### 依赖扫描

```yaml
security:
  dependencyScan:
    enabled: true
  secretScan:
    enabled: true
```

---

## 🚀 部署方式四：持续集成/持续部署 (CI/CD)

### GitHub Actions 配置（使用 .cnb.yml）

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
