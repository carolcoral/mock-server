#!/bin/bash

echo "=========================================="
echo "Mock Server Docker 构建脚本"
echo "作者: carolcoral"
echo "=========================================="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    print_error "Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装（支持 v1 和 v2）
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 检测 docker-compose 命令
if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
    print_info "使用 docker-compose (v1)"
else
    DOCKER_COMPOSE="docker compose"
    print_info "使用 docker compose (v2)"
fi

print_info "Docker 环境检查通过"

# 设置脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

print_info "当前目录: $SCRIPT_DIR"

# 检查 .env 文件
if [ ! -f "docker/.env" ]; then
    print_info ".env 文件不存在，从 .env.example 创建..."
    cp docker/.env.example docker/.env
    print_success ".env 文件已创建"
else
    print_success ".env 文件已存在"
fi

# 检查依赖
print_info "检查项目依赖..."

if [ ! -f "backend/pom.xml" ]; then
    print_error "未找到 backend/pom.xml"
    exit 1
fi

if [ ! -f "frontend/package.json" ]; then
    print_error "未找到 frontend/package.json"
    exit 1
fi

print_success "项目文件检查通过"

# 构建选项
BUILD_MODE="${1:-full}"

case "$BUILD_MODE" in
    full)
        print_info "完整构建模式：构建前端和后端"
        ;;
    backend-only)
        print_info "仅构建后端模式"
        ;;
    frontend-only)
        print_info "仅构建前端模式"
        ;;
    no-cache)
        print_info "无缓存构建模式"
        ;;
    *)
        print_error "未知构建模式: $BUILD_MODE"
        print_info "可用模式: full, backend-only, frontend-only, no-cache"
        exit 1
        ;;
esac

# 进入 docker 目录
cd docker || exit 1

# 构建 Docker 镜像
print_info "开始构建 Docker 镜像..."
print_info "这可能需要几分钟时间..."

BUILD_CMD="$DOCKER_COMPOSE build"

case "$BUILD_MODE" in
    no-cache)
        BUILD_CMD="$BUILD_CMD --no-cache"
        ;;
esac

echo ""
echo "执行命令: $BUILD_CMD"
echo ""

if eval "$BUILD_CMD"; then
    print_success "Docker 镜像构建成功"
else
    print_error "Docker 镜像构建失败"
    exit 1
fi

echo ""
echo "=========================================="
print_success "构建完成！"
echo ""
echo "启动服务："
echo "  cd docker && $DOCKER_COMPOSE up -d"
echo ""
echo "查看日志："
echo "  cd docker && $DOCKER_COMPOSE logs -f"
echo ""
echo "停止服务："
echo "  cd docker && $DOCKER_COMPOSE down"
echo ""
echo "访问应用："
echo "  前端和后端: http://localhost:8080"
echo "  API 文档: http://localhost:8080/api/swagger-ui.html"
echo "  健康检查: http://localhost:8080/api/actuator/health"
echo "=========================================="
