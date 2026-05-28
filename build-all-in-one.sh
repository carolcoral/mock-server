#!/bin/bash

echo "=========================================="
echo "Mock Server 一键构建脚本"
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

# 获取脚本所在目录的绝对路径（项目根目录）
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

# ==========================================
# 第一步：检测基础环境
# ==========================================
print_info "=========================================="
print_info "第一步：检测基础环境..."
print_info "=========================================="

check_command() {
    local cmd="$1"
    local name="$2"
    if ! command -v "$cmd" >/dev/null 2>&1; then
        print_error "未找到 ${name}，请先安装后重试"
        exit 1
    fi
    local version_info
    version_info=$("$cmd" --version 2>&1 | head -n 1)
    print_success "${name} 已安装: ${version_info}"
}

# 检测 Java 21
check_java21() {
    # 优先使用 JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java >/dev/null 2>&1; then
        JAVA_CMD="java"
    else
        print_error "未找到 Java，请先安装 JDK 21"
        exit 1
    fi

    JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | head -n 1 | cut -d'"' -f2)
    JAVA_MAJOR=$(echo "$JAVA_VERSION" | cut -d'.' -f1)

    if [ "$JAVA_MAJOR" != "21" ]; then
        print_error "需要 JDK 21，当前版本为: ${JAVA_VERSION}，请先安装 JDK 21"
        print_info "提示: 可运行 ./setup-env.sh 自动安装 JDK 21"
        exit 1
    fi

    print_success "JDK 21 已安装: ${JAVA_VERSION} (JAVA_HOME=${JAVA_HOME:-$("$JAVA_CMD" -XshowSettings:properties -version 2>&1 | grep 'java.home' | awk '{print $NF}')})"
}

check_java21

# 检测 Maven
check_command "mvn" "Maven"

# 检测 Node.js
check_command "node" "Node.js"

# 检测 npm
check_command "npm" "npm"

print_success "基础环境检测通过"

# ==========================================
# 第二步：构建前端静态资源
# ==========================================
print_info ""
print_info "=========================================="
print_info "第二步：构建前端静态资源..."
print_info "=========================================="

FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"

if [ ! -d "$FRONTEND_DIR" ]; then
    print_error "前端目录不存在: $FRONTEND_DIR"
    exit 1
fi

cd "$FRONTEND_DIR"

print_info "安装前端依赖..."
npm install
if [ $? -ne 0 ]; then
    print_error "前端依赖安装失败 (npm install)"
    exit 1
fi
print_success "前端依赖安装完成"

print_info "构建前端生产版本..."
npm run build
if [ $? -ne 0 ]; then
    print_error "前端构建失败 (npm run build)"
    exit 1
fi
print_success "前端构建成功，输出目录: $(pwd)/dist"

# 返回项目根目录
cd "$PROJECT_ROOT"

# ==========================================
# 第三步：复制前端静态资源到后端
# ==========================================
print_info ""
print_info "=========================================="
print_info "第三步：复制前端静态资源到后端..."
print_info "=========================================="

FRONTEND_DIST="$FRONTEND_DIR/dist"
BACKEND_STATIC="$BACKEND_DIR/src/main/resources/static"

if [ ! -d "$FRONTEND_DIST" ]; then
    print_error "前端构建产物不存在: $FRONTEND_DIST"
    exit 1
fi

# 清理旧的 static 目录
if [ -d "$BACKEND_STATIC" ]; then
    print_info "清理旧的 static 目录..."
    rm -rf "$BACKEND_STATIC"
fi

# 复制 dist 并重命名为 static
print_info "复制 dist -> src/main/resources/static..."
cp -r "$FRONTEND_DIST" "$BACKEND_STATIC"

if [ $? -ne 0 ]; then
    print_error "复制前端静态资源失败"
    exit 1
fi

# 验证关键文件是否存在
if [ -f "$BACKEND_STATIC/index.html" ]; then
    print_success "前端静态资源复制完成: $BACKEND_STATIC"
    print_info "  - index.html: $( [ -f "$BACKEND_STATIC/index.html" ] && echo '存在' || echo '不存在' )"
    print_info "  - assets目录: $( [ -d "$BACKEND_STATIC/assets" ] && echo '存在' || echo '不存在' )"
else
    print_error "index.html 不存在，复制可能不完整"
    exit 1
fi

# ==========================================
# 第四步：Maven 打包
# ==========================================
print_info ""
print_info "=========================================="
print_info "第四步：Maven 打包..."
print_info "=========================================="

cd "$BACKEND_DIR"

print_info "执行 mvn clean package..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    print_error "Maven 打包失败"
    exit 1
fi

print_success "Maven 打包完成"

# 返回项目根目录
cd "$PROJECT_ROOT"

# ==========================================
# 构建完成
# ==========================================
echo ""
print_info "=========================================="
print_success "构建完成！"
print_info "=========================================="

# 查找生成的 JAR 包
JAR_FILE=$(find "$BACKEND_DIR/target" -maxdepth 1 -name "*.jar" ! -name "*sources.jar" ! -name "*javadoc.jar" 2>/dev/null | head -n 1)

if [ -n "$JAR_FILE" ]; then
    JAR_NAME=$(basename "$JAR_FILE")
    print_info "JAR 包位置: $JAR_FILE"
    echo ""
    print_info "运行命令:"
    print_info "  java -jar $JAR_FILE"
    echo ""
    print_info "或者直接运行一键启动脚本:"
    print_info "  ./run.sh"
else
    print_warning "未找到 JAR 包，请检查 backend/target/ 目录"
fi
