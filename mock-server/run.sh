#!/bin/bash

echo "=========================================="
echo "Mock Server 运行脚本"
echo "作者: carolcoral"
echo "=========================================="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

# 检查后端jar包是否存在
if [ ! -f "backend/target/mock-server-1.0.0.jar" ]; then
    print_error "后端jar包不存在，请先运行构建脚本: ./build.sh"
    exit 1
fi

# 创建数据目录
mkdir -p backend/data
mkdir -p backend/logs

print_info "启动后端服务..."
nohup java -jar backend/target/mock-server-1.0.0.jar > backend/logs/server.log 2>&1 &

if [ $? -eq 0 ]; then
    print_success "后端服务已启动"
    print_info "日志文件: backend/logs/server.log"
else
    print_error "后端服务启动失败"
    exit 1
fi

# 等待后端启动
print_info "等待后端服务启动..."
sleep 10

# 检查后端是否正常运行
if curl -s http://localhost:8080/api/v3/api-docs > /dev/null; then
    print_success "后端服务正常运行"
else
    print_error "后端服务启动失败，请检查日志"
    exit 1
fi

print_info "启动前端服务..."
cd frontend

# 检查依赖是否安装
if [ ! -d "node_modules" ]; then
    print_info "安装前端依赖..."
    npm install
fi

print_info "启动前端开发服务器..."
npm run dev
