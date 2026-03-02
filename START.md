# Mock Server 快速启动指南

## 环境要求

- **Java**: JDK 17 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 18 或更高版本
- **npm**: 9 或更高版本

## 快速启动（推荐）

### 方式一：使用脚本一键启动

1. **构建项目**
   ```bash
   ./build.sh
   ```

2. **启动服务**
   ```bash
   ./run.sh
   ```

### 方式二：手动分步启动

#### 1. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务地址: http://localhost:8080

#### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务地址: http://localhost:3000

### 方式三：Docker部署

```bash
cd docker
docker-compose up -d
```

## 默认账号

### 系统登录
- 地址: http://localhost:3000
- 用户名: `admin`
- 密码: `Admin@123`

### Swagger文档
- 地址: http://localhost:8080/api/swagger-ui.html
- 用户名: `admin`
- 密码: `Admin@123`

## 快速测试

### 1. 查看Swagger文档

打开浏览器访问: http://localhost:8080/api/swagger-ui.html

点击右上角的"Authorize"按钮，输入账号密码登录，即可查看所有API接口。

### 2. 测试Mock接口

项目启动后会自动创建示例项目（projectCode: `demo`），包含以下示例接口：

#### 用户登录接口
```bash
curl -X POST http://localhost:8080/api/mock/demo/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

#### 获取用户信息
```bash
curl http://localhost:8080/api/mock/demo/user/info
```

#### 商品列表
```bash
curl http://localhost:8080/api/mock/demo/products
```

#### 随机响应接口（80%概率返回200，20%概率返回500）
```bash
curl http://localhost:8080/api/mock/demo/random
```

多次调用此接口，会随机返回不同的响应。

### 3. 使用WebSocket

```javascript
// 连接WebSocket
const ws = new WebSocket('ws://localhost:8080/api/ws/mock/demo/user/ws');

// 连接成功
ws.onopen = () => {
  console.log('WebSocket连接成功');
  
  // 发送Mock请求
  ws.send(JSON.stringify({
    type: 'mock',
    projectCode: 'demo',
    path: '/user/info',
    method: 'GET'
  }));
};

// 接收响应
ws.onmessage = (event) => {
  const response = JSON.parse(event.data);
  console.log('收到响应:', response);
};

// 发生错误
ws.onerror = (error) => {
  console.error('WebSocket错误:', error);
};
```

## 常见问题

### 1. 端口被占用

如果8080或3000端口被占用，可以修改配置文件：

**后端** (`backend/src/main/resources/application.yml`):
```yaml
server:
  port: 8081  # 修改端口号
```

**前端** (`frontend/vite.config.js`):
```javascript
server: {
  port: 3001,  // 修改端口号
  proxy: {
    '/api': {
      target: 'http://localhost:8081',  // 对应后端端口
      changeOrigin: true
    }
  }
}
```

### 2. 数据库文件位置

数据库文件默认在当前目录下的 `data` 文件夹中：
- 开发环境: `backend/data/mock-server.db`
- Docker环境: `docker/data/mock-server.db`

### 3. 日志文件

日志文件位置：
- 开发环境: `backend/logs/mock-server.log`
- Docker环境: `docker/logs/mock-server.log`

### 4. 忘记管理员密码

如果忘记了管理员密码，可以：
1. 删除数据库文件（`data/mock-server.db`）
2. 重启服务
3. 系统会自动重新创建管理员账号（admin/Admin@123）

**注意**: 删除数据库会清空所有数据！

## 功能速览

### 1. 项目管理
- 创建、编辑、删除项目
- 项目成员管理
- 项目权限控制

### 2. 接口管理
- 创建自定义接口
- 配置多状态码响应
- 设置响应延迟
- 随机响应权重配置
- 条件表达式

### 3. 用户管理
- 多用户支持
- 角色管理（管理员/普通用户）
- 用户状态管理

### 4. 系统特性
- JWT认证
- Swagger文档
- WebSocket支持
- 缓存优化
- Docker部署

## 下一步

1. 登录系统: http://localhost:3000
2. 创建自己的项目
3. 添加自定义接口
4. 测试Mock接口
5. 查看接口文档: http://localhost:8080/api/swagger-ui.html

## 获取帮助

- GitHub: https://github.com/carolcoral
- 查看详细文档: [README.md](./README.md)
- Swagger文档: http://localhost:8080/api/swagger-ui.html

## 许可证

Apache License 2.0
