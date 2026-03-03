# 安全修复报告

## 修复概览

本次修复共处理了 29 个安全问题，涵盖以下类别：

### 关键级别问题（已修复）

#### 1. 硬编码凭据和密钥
**修复内容：**
- 将 JWT 密钥从配置文件移至环境变量 `JWT_SECRET`
- 将管理员密码从配置文件移至环境变量 `ADMIN_PASSWORD`
- 将 Swagger 登录凭据移至环境变量 `SWAGGER_USERNAME` 和 `SWAGGER_PASSWORD`

**文件修改：**
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/carolcoral/mockserver/filter/JwtAuthenticationFilter.java`
- `tcb.yaml`
- `.cnb.yml`

#### 2. 弱 JWT 密钥
**修复内容：**
- JWT 过期时间从 24 小时缩短至 30 分钟
- 要求通过环境变量设置至少 256 位的强密钥

**文件修改：**
- `backend/src/main/resources/application.yml`

#### 3. 前端敏感数据泄露
**修复内容：**
- 移除登录页面显示的默认凭据
- 添加警告：生产环境应使用 HttpOnly Cookies 存储 token

**文件修改：**
- `frontend/src/views/Login.vue`

### 高级别问题（已修复）

#### 4. 不安全的 CORS 配置
**修复内容：**
- 移除通配符 `*` 允许的源
- 从环境变量 `ALLOWED_ORIGINS` 读取允许的源列表
- 开发环境默认允许 localhost 访问

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/config/SecurityConfig.java`

#### 5. 路径变量缺少输入验证
**修复内容：**
- 为 `projectCode` 添加正则验证：`^[a-zA-Z0-9_-]+$`
- 为 `username` 添加正则验证：`^[a-zA-Z0-9_]+$`
- 为 `code` 添加正则验证：`^[a-zA-Z0-9_-]+$`

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/controller/MockController.java`
- `backend/src/main/java/com/carolcoral/mockserver/controller/UserController.java`
- `backend/src/main/java/com/carolcoral/mockserver/controller/ProjectController.java`

#### 6. 日志中的敏感数据
**修复内容：**
- 移除日志中的异常堆栈信息
- 使用通用错误消息代替详细错误信息

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/filter/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/carolcoral/mockserver/service/MockService.java`
- `backend/src/main/java/com/carolcoral/mockserver/controller/MockController.java`
- `backend/src/main/java/com/carolcoral/mockserver/handler/MockWebSocketHandler.java`

#### 7. 默认管理员凭据
**修复内容：**
- 强制通过环境变量设置管理员密码
- 添加密码强度验证（至少8位，包含大小写字母、数字和特殊字符）
- 如果密码未配置或强度不足，跳过创建管理员账号

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/config/StartupConfig.java`

#### 8. 错误消息泄露信息
**修复内容：**
- 统一使用通用错误消息
- 详细错误仅在服务端日志中记录

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/controller/MockController.java`
- `backend/src/main/java/com/carolcoral/mockserver/service/MockService.java`

### 中级别问题（已修复）

#### 9. 缺少密码验证
**修复内容：**
- 为 `LoginRequest` 添加强密码验证规则
- 要求：至少 8 位，包含大小写字母、数字和特殊字符
- 用户名：3-50 位，只能包含字母、数字和下划线

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/dto/LoginRequest.java`
- `frontend/src/views/Login.vue`

#### 10. 弱密码哈希
**修复内容：**
- BCrypt 哈希轮数从 10 增加到 12

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/config/SecurityConfig.java`

#### 11. JWT 过期时间过长
**修复内容：**
- JWT 过期时间从 24 小时缩短至 30 分钟

**文件修改：**
- `backend/src/main/resources/application.yml`

#### 12. 缺少请求体验证
**修复内容：**
- 使用 `@Validated` 注解启用路径变量验证

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/controller/MockController.java`
- `backend/src/main/java/com/carolcoral/mockserver/controller/UserController.java`
- `backend/src/main/java/com/carolcoral/mockserver/controller/ProjectController.java`

#### 13. 响应延迟 DoS 风险
**修复内容：**
- 限制最大响应延迟时间为 5 秒
- 防止用户设置过长延迟导致 DoS

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/controller/MockController.java`
- `backend/src/main/java/com/carolcoral/mockserver/handler/MockWebSocketHandler.java`

### 低级别问题（已修复）

#### 14. 生产环境控制台日志
**修复内容：**
- 移除前端代码中的 `console.error` 语句

**文件修改：**
- `frontend/src/views/Login.vue`
- `frontend/src/utils/request.js`

#### 15. 添加 X-Frame-Options 头
**修复内容：**
- 在安全配置中添加 `headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)`

**文件修改：**
- `backend/src/main/java/com/carolcoral/mockserver/config/SecurityConfig.java`

## 部署指南

### 环境变量配置

1. 复制 `.env.example` 文件为 `.env`
2. 根据实际环境修改环境变量值
3. **重要**：生产环境必须设置以下强密钥和密码：
   - `JWT_SECRET` - 至少 256 位随机字符串
   - `ADMIN_PASSWORD` - 强密码（至少 8 位，包含大小写字母、数字和特殊字符）
   - `SWAGGER_USERNAME` 和 `SWAGGER_PASSWORD`（如需使用 Swagger）
   - `ALLOWED_ORIGINS` - 配置允许的前端域名

### 密码强度要求

所有用户密码必须满足以下要求：
- 最小长度：8 位
- 必须包含：大写字母、小写字母、数字、特殊字符（@$!%*?&）

### CORS 配置

生产环境必须配置 `ALLOWED_ORIGINS`，例如：
```
ALLOWED_ORIGINS=https://example.com,https://app.example.com
```

开发环境默认允许 `http://localhost:*` 和 `http://127.0.0.1:*`。

### JWT 配置

- **过期时间**：30 分钟（生产环境）
- **密钥强度**：至少 256 位
- **密钥存储**：必须通过环境变量配置

## 后续建议

### 短期建议

1. **实现 API 限流**：为所有接口添加速率限制，防止暴力破解
2. **启用 CSRF 保护**：为状态改变操作启用 CSRF 保护
3. **添加审计日志**：记录所有安全相关操作
4. **实现刷新令牌机制**：使用短期访问令牌 + 长期刷新令牌

### 中期建议

1. **实现 HttpOnly Cookies**：将 JWT token 存储在 HttpOnly、Secure、SameSite cookies 中
2. **添加内容安全策略 (CSP)**：配置 CSP 头部防止 XSS
3. **启用 HTTPS**：强制使用 HTTPS 并配置 HSTS
4. **禁用生产环境 Swagger**：仅在开发环境启用 Swagger 文档

### 长期建议

1. **实施零信任架构**：所有请求都需要认证和授权
2. **添加多因素认证 (MFA)**：为管理员账号启用 MFA
3. **实施 API 密钥认证**：为 Mock API 访问提供独立的 API 密钥机制
4. **定期安全审计**：建立定期安全扫描和审计流程

## 测试清单

- [ ] 使用弱密码无法创建管理员账号
- [ ] 未设置环境变量时跳过管理员账号创建
- [ ] 路径变量中的特殊字符被拒绝
- [ ] CORS 仅允许配置的源访问
- [ ] JWT token 在 30 分钟后过期
- [ ] 响应延迟限制在 5 秒以内
- [ ] 错误消息不泄露系统信息
- [ ] 登录页面不显示默认凭据

## 联系方式

如有安全问题，请联系安全团队。
