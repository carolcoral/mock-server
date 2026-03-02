# 腾讯云 CNB 快速部署指南

## 📋 快速开始（3步完成部署）

### 方式一：一键脚本部署（推荐）

```bash
# 1. 运行部署脚本
./deploy-tencent-cloud.sh

# 2. 按提示配置参数
# 3. 等待部署完成
```

### 方式二：CLI命令部署

```bash
# 1. 安装依赖
npm install -g @cloudbase/cli

# 2. 登录
tcb login

# 3. 部署
tcb framework:deploy
```

### 方式三：控制台部署

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **云开发 CloudBase**
3. 上传项目代码
4. 点击部署

---

## 🔑 部署前必须修改的配置

### 1. 修改敏感信息（重要！）

编辑 `tcb.yaml`：

```yaml
env:
  variables:
    # 修改JWT密钥（必须！）
    JWT_SECRET: "你的32位以上随机密钥"
    
    # 修改管理员密码（必须！）
    ADMIN_PASSWORD: "你的强密码"
```

**生成随机密钥：**
```bash
# Linux/Mac
openssl rand -base64 32

# 或使用脚本生成
./deploy-tencent-cloud.sh
```

### 2. 数据库配置（推荐）

生产环境建议使用 **腾讯云 MySQL**：

```bash
# 登录控制台创建 MySQL 实例
# 记录以下信息：
# - 实例ID
# - 主机地址
# - 端口（默认3306）
# - 数据库名
# - 用户名
# - 密码
```

然后在部署时输入 MySQL 信息即可。

---

## 🚀 部署选项

### 完整部署
```bash
./deploy-tencent-cloud.sh
```

### 仅构建
```bash
./deploy-tencent-cloud.sh --build-only
```

### 仅部署
```bash
./deploy-tencent-cloud.sh --deploy-only
```

### 更新部署
```bash
./deploy-tencent-cloud.sh --update
```

### 查看帮助
```bash
./deploy-tencent-cloud.sh --help
```

---

## 📊 部署后访问

部署成功后，你会看到：

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  访问地址:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  前端应用: https://your-app-id.tcloudbaseapp.com
  API文档: https://your-app-id.tcloudbaseapp.com/api/swagger-ui.html
  后端API: https://your-app-id.tcloudbaseapp.com/api

  Swagger认证:
    用户名: admin
    密码: Admin@123

  默认登录账号:
    用户名: admin
    密码: Admin@123
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 快速测试

```bash
# 测试 Mock 接口
curl https://your-app-id.tcloudbaseapp.com/api/mock/demo/products

# 测试登录接口
curl -X POST https://your-app-id.tcloudbaseapp.com/api/mock/demo/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

---

## ⚙️ 环境变量说明

### 必需变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `JWT_SECRET` | JWT加密密钥 | 必须自定义 |
| `ADMIN_USERNAME` | 管理员用户名 | admin |
| `ADMIN_PASSWORD` | 管理员密码 | 必须自定义 |

### 数据库变量

| 变量名 | 说明 |
|--------|------|
| `MYSQL_HOST` | MySQL主机地址 |
| `MYSQL_PORT` | MySQL端口（3306）|
| `MYSQL_DATABASE` | 数据库名 |
| `MYSQL_USERNAME` | 用户名 |
| `MYSQL_PASSWORD` | 密码 |

### 性能变量

| 变量名 | 说明 | 建议值 |
|--------|------|--------|
| `JAVA_OPTS` | JVM参数 | `-Xmx512m -Xms256m` |
| `SPRING_PROFILES_ACTIVE` | Spring环境 | `cloud` |

---

## 🔧 常用命令

### 查看日志
```bash
# 实时日志
tcb app:logs mock-server -f

# 最近100行
tcb app:logs mock-server -n 100

# 指定时间
tcb app:logs mock-server --since 1h
```

### 查看应用状态
```bash
tcb app:list
```

### 查看环境信息
```bash
tcb env:list
```

### 设置环境变量
```bash
tcb env:set KEY=value --envId your-env-id
```

### 重启应用
```bash
tcb app:restart mock-server --envId your-env-id
```

### 回滚版本
```bash
# 查看版本
tcb app:versions mock-server

# 回滚
tcb app:rollback mock-server --version v1.0.0
```

---

## 🐛 常见问题

### 1. 构建失败

**问题**: `mvn command not found`

**解决**: 确保本地已安装 Maven 和 JDK 17

```bash
# 检查安装
java -version
mvn -version
```

---

### 2. 部署失败

**问题**: `Failed to deploy`

**解决**:
1. 检查网络连接
2. 确认已登录腾讯云: `tcb login`
3. 检查环境ID是否正确
4. 查看详细日志: `tcb app:logs`

---

### 3. 应用启动失败

**问题**: `Application failed to start`

**解决**:
1. 查看日志: `tcb app:logs mock-server -f`
2. 检查数据库连接
3. 确认环境变量配置正确
4. 检查端口是否被占用

---

### 4. SQLite 文件丢失

**问题**: 重启后数据丢失

**原因**: 云原生环境重启会重置文件系统

**解决**:
- 使用腾讯云 MySQL（推荐）
- 或使用云存储挂载（需要配置）

---

### 5. 内存溢出

**问题**: `OutOfMemoryError`

**解决**:
```bash
# 增加内存限制
tcb env:set JAVA_OPTS="-Xmx1g -Xms512m" --envId your-env-id

# 重启应用
tcb app:restart mock-server
```

---

## 🛡️ 安全建议

### 部署后立即执行

1. **修改默认密码**
   ```bash
   tcb env:set ADMIN_PASSWORD="你的强密码" --envId your-env-id
   tcb app:restart mock-server --envId your-env-id
   ```

2. **配置自定义域名**
   - 登录控制台 > 云开发 > 应用管理
   - 域名管理 > 添加自定义域名
   - 启用 HTTPS

3. **限制 Swagger 访问**
   - 配置 IP 白名单
   - 或使用 API 网关认证

4. **启用访问日志**
   - 默认已启用
   - 定期查看日志分析异常访问

5. **配置监控告警**
   - 登录控制台 > 云监控
   - 设置 CPU、内存、错误率告警

---

## 📈 性能优化

### 自动扩缩容

`tcb.yaml` 默认配置：
```yaml
autoscaling:
  enabled: true
  minReplicas: 1
  maxReplicas: 5
  targetCPUUtilization: 70
  targetMemoryUtilization: 80
```

### 数据库优化

1. **使用腾讯云 MySQL**
   - 创建只读实例
   - 开启读写分离
   - 配置连接池

2. **缓存优化**
   - 增加 Caffeine 缓存大小
   - 考虑使用 Redis（腾讯云）

### JVM 优化

```bash
# 生产环境推荐配置
tcb env:set JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200" --envId your-env-id
```

---

## 📚 详细文档

- **完整部署文档**: [DEPLOY_TENCENT_CLOUD.md](./DEPLOY_TENCENT_CLOUD.md)
- **项目说明**: [README.md](./README.md)
- **项目结构**: [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)

---

## 💬 技术支持

- 腾讯云文档: https://cloud.tencent.com/document/product/876
- CloudBase CLI: https://docs.cloudbase.net/cli/intro/
- GitHub Issues: https://github.com/carolcoral/mock-server/issues

---

**祝使用愉快！🎉**
