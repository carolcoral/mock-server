#!/bin/bash

echo "=========================================="
echo "Mock Server 构建脚本"
echo "作者: carolcoral"
echo "=========================================="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印彩色信息
print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 检查依赖
print_info "检查依赖..."

if ! command_exists java; then
    print_error "未找到Java，请先安装JDK 17+"
    exit 1
fi

if ! command_exists mvn; then
    print_error "未找到Maven，请先安装Maven 3.6+"
    exit 1
fi

if ! command_exists node; then
    print_error "未找到Node.js，请先安装Node.js 18+"
    exit 1
fi

if ! command_exists npm; then
    print_error "未找到npm，请先安装npm 9+"
    exit 1
fi

print_success "依赖检查通过"

# 构建后端
print_info "开始构建后端..."
cd backend || exit 1

print_info "清理并编译..."
mvn clean compile

if [ $? -ne 0 ]; then
    print_error "后端编译失败"
    exit 1
fi

print_info "打包..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    print_error "后端打包失败"
    exit 1
fi

print_success "后端构建成功"

# 返回根目录
cd ..

# 构建前端
print_info "开始构建前端..."
cd frontend || exit 1

print_info "安装依赖..."
npm install

if [ $? -ne 0 ]; then
    print_error "前端依赖安装失败"
    exit 1
fi

print_info "构建生产版本..."
npm run build

if [ $? -ne 0 ]; then
    print_error "前端构建失败"
    exit 1
fi

print_success "前端构建成功"

# 返回根目录
cd ..

print_success "构建完成！"
print_info "后端jar包: backend/target/mock-server-1.0.0.jar"
print_info "前端dist目录: frontend/dist/"
print_info ""
print_info "启动后端: cd backend && mvn spring-boot:run"
print_info "启动前端: cd frontend && npm run dev"
