# .cnb.yml 配置指南

本文档详细介绍 `.cnb.yml` 文件的配置和使用方法。

## 📋 什么是 .cnb.yml？

`.cnb.yml` 是腾讯云 Cloud Native Build (CNB) 的配置文件，用于定义应用的构建、部署和运行配置。

### 核心优势

- ✅ **完整的构建流水线**: 6 个阶段精细控制
- ✅ **构建缓存**: 大幅提升构建速度（60%+）
- ✅ **多环境部署**: dev/test/prod 环境分离
- ✅ **自动扩缩容**: 基于负载自动调整实例数
- ✅ **监控告警**: 内置告警规则
- ✅ **安全扫描**: 镜像和依赖漏洞扫描
- ✅ **成本优化**: 预留实例、自动启停

## 🎯 快速开始

### 1. 配置敏感信息（必须！）

编辑 `.cnb.yml`：

```yaml
# 环境变量配置
env:
  # JWT配置（必须修改！）
  - name: JWT_SECRET
    value: "你的32位以上随机密钥"  # ⚠️ 生产环境必须修改！
    required: true
  
  # 管理员密码（必须修改！）
  - name: ADMIN_PASSWORD
    value: "你的强密码"  # ⚠️ 生产环境必须修改！
    required: true
    secret: true
```

### 2. 配置镜像仓库

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

### 3. 选择数据库配置

**方式A：腾讯云 MySQL（推荐生产环境）**

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

**方式B：SQLite（开发环境）**

无需配置，系统默认使用 SQLite。

### 4. 部署应用

```bash
# 方式一：使用脚本
./deploy-tencent-cloud.sh

# 方式二：使用 CLI
tcb framework:deploy

# 方式三：控制台部署
# 上传代码后，系统自动识别 .cnb.yml
```

## 🔧 配置详解

### 构建步骤

#### Step 1: 准备构建环境

**功能**：
- 安装 Node.js 18
- 安装 npm
- 验证 Java 和 Maven 安装

**缓存**：
- `/cache/maven` - Maven 依赖缓存
- `/cache/node_modules` - Node 模块缓存
- `/root/.m2` - Maven 本地仓库

#### Step 2: 构建后端

**功能**：
- 配置 Maven 阿里云镜像
- 编译 Java 代码
- 打包 Spring Boot JAR 文件

**产出物**：
- `backend/target/mock-server-*.jar`

**缓存**：
- `~/.m2` - Maven 本地仓库
- `/cache/maven` - Maven 依赖缓存

#### Step 3: 构建前端

**功能**：
- 配置 npm 淘宝镜像
- 安装依赖（`npm ci`）
- 构建 Vue 3 生产版本

**产出物**：
- `frontend/dist/**/**`

**缓存**：
- `frontend/node_modules` - Node 模块
- `~/.npm` - npm 缓存

#### Step 4: 构建 Docker 镜像

**功能**：
- 创建优化的 Dockerfile
- 使用腾讯云 OpenJDK 镜像
- 复制 JAR 和前端静态文件
- 配置健康检查

**产出物**：
- Docker 镜像：`mock-server:${BUILD_ID}`

#### Step 5: 推送镜像

**功能**：
- 打标签（符合 TCR 规范）
- 登录腾讯云镜像仓库
- 推送镜像到远程仓库

**产出物**：
- 远程镜像：`ccr.ccs.tencentyun.com/${TCR_NAMESPACE}/mock-server:${BUILD_ID}`

#### Step 6: 生成部署清单

**功能**：
- 生成 Kubernetes Deployment
- 生成 Service
- 生成 Ingress
- 配置健康检查
- 配置资源限制

**产出物**：
- `deployment.yaml` - Kubernetes 部署清单

### 部署配置

#### 开发环境

```yaml
- name: dev
  replicas: 1
  resources:
    cpu: 500m
    memory: 512Mi
  autoDeploy:
    enabled: true
    branches:
      - develop
```

#### 测试环境

```yaml
- name: test
  replicas: 2
  resources:
    cpu: 1000m
    memory: 1024Mi
```

#### 生产环境

```yaml
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

## 📊 性能数据

### 构建速度对比

| 构建类型 | 首次构建 | 后续构建（有缓存） | 提升 |
|---------|---------|-------------------|------|
| 传统构建 | 5-8 分钟 | 4-6 分钟 | - |
| CNB 构建 | 5-8 分钟 | **1-2 分钟** | **60-75%** |

### 缓存效果

- **Maven 依赖**：首次下载后缓存，后续直接使用
- **npm 模块**：node_modules 缓存，避免重复安装
- **Docker 层**：镜像层缓存，只重新构建变更的部分

## 🔍 常见问题

### 1. 构建失败：找不到 Maven 依赖

**原因**：网络问题或 Maven 镜像配置错误

**解决**：
```yaml
# 确保配置了阿里云镜像
- name: build-backend
  run: |
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << 'EOF'
    <settings>
      <mirrors>
        <mirror>
          <id>aliyun</id>
          <mirrorOf>central</mirrorOf>
          <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
      </mirrors>
    </settings>
    EOF
```

### 2. 构建失败：npm install 超时

**原因**：网络问题或 npm 镜像配置错误

**解决**：
```yaml
# 确保配置了淘宝镜像
- name: build-frontend
  run: |
    npm config set registry https://registry.npmmirror.com
    npm ci --prefer-offline --no-audit --no-fund
```

### 3. 镜像推送失败

**原因**：镜像仓库认证失败或命名空间错误

**解决**：
```yaml
# 检查环境变量配置
env:
  - name: TCR_USERNAME
    value: ${TCR_USERNAME}
  - name: TCR_PASSWORD
    value: ${TCR_PASSWORD}
  - name: TCR_NAMESPACE
    value: ${TCR_NAMESPACE:-"default"}
```

### 4. 应用启动失败

**原因**：数据库连接失败或环境变量配置错误

**解决**：
```bash
# 查看日志
tcb app:logs mock-server -f

# 检查环境变量
tcb env:list --envId your-env-id
```

### 5. 自动扩缩容不生效

**原因**：资源使用率未达到阈值

**解决**：
```yaml
# 调整扩缩容阈值
autoscaling:
  metrics:
    - type: cpu
      target:
        averageUtilization: 50  # 从 70 调整为 50
    - type: memory
      target:
        averageUtilization: 60  # 从 80 调整为 60
```

## 🔧 高级配置

### 自定义构建步骤

添加自定义构建步骤：

```yaml
build:
  steps:
    - name: my-custom-step
      run: |
        echo "自定义构建步骤"
        # 你的命令
      cache:
        paths:
          - /path/to/cache
      artifacts:
        paths:
          - /path/to/artifacts
```

### 自定义部署策略

```yaml
deploy:
  strategy:
    type: BlueGreen  # 蓝绿部署
    blueGreen:
      scaleDownDelaySeconds: 300
      autoPromotionEnabled: false
```

### 自定义域名

```yaml
domain:
  custom:
    enabled: true
    domain: mock.yourdomain.com
    ssl:
      enabled: true
      autoRenew: true
```

### 网络策略

```yaml
security:
  networkPolicy:
    enabled: true
    policies:
      - name: allow-ingress
        type: ingress
        from:
          - namespaceSelector:
              matchLabels:
                name: ingress-nginx
```

## 📚 相关文档

- **快速开始**：[TENCENT_CLOUD_QUICKSTART.md](./TENCENT_CLOUD_QUICKSTART.md)
- **详细部署**：[DEPLOY_TENCENT_CLOUD.md](./DEPLOY_TENCENT_CLOUD.md)
- **项目说明**：[README.md](./README.md)
- **API文档**：`https://your-app-id.tcloudbaseapp.com/api/swagger-ui.html`

## 💬 技术支持

- 腾讯云文档：https://cloud.tencent.com/document/product/876
- CloudBase CLI：https://docs.cloudbase.net/cli/intro/
- GitHub Issues：https://github.com/carolcoral/mock-server/issues

## 📄 许可证

Apache License 2.0

Copyright (c) 2024 carolcoral
