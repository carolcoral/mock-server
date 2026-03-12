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

# 内部JAVA_HOME变量（不污染全局环境）
INTERNAL_JAVA_HOME=""

# 加载.env文件中的配置
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    print_info "已加载.env配置文件"
else
    print_error "未找到.env配置文件"
    exit 1
fi

# 设置默认端口（如果.env中未设置）
SERVER_PORT=${SERVER_PORT:-8080}
FRONTEND_PORT=${FRONTEND_PORT:-3000}

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM=Linux;;
    Darwin*)    PLATFORM=Mac;;
    CYGWIN*|MINGW32*|MSYS*|MINGW*) PLATFORM=Windows;;
    *)          PLATFORM="UNKNOWN:${OS}"
esac

# 自动检测并设置INTERNAL_JAVA_HOME（检测Java 17）
auto_set_java_home() {
    print_info "检测Java环境..."

    # 优先检查INTERNAL_JAVA_HOME是否已设置
    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "17" ]; then
            print_success "使用已配置的INTERNAL_JAVA_HOME: $INTERNAL_JAVA_HOME (Java $JAVA_VERSION)"
            return 0
        fi
    fi

    # 检查全局JAVA_HOME是否为Java 17，如果是则复制到INTERNAL_JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "17" ]; then
            INTERNAL_JAVA_HOME="$JAVA_HOME"
            print_success "从全局JAVA_HOME复制: $INTERNAL_JAVA_HOME (Java $JAVA_VERSION)"
            return 0
        fi
    fi

    # 根据操作系统查找Java 17
    case "${PLATFORM}" in
        Mac)
            # macOS: 使用java_home命令查找Java 17
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                INTERNAL_JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)
                if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
                    print_success "通过java_home命令检测到Java 17: $INTERNAL_JAVA_HOME"
                    return 0
                fi
            fi

            # 备用方案：查找Homebrew安装的Java 17
            if [ -d "/usr/local/Cellar/openjdk@17" ]; then
                INTERNAL_JAVA_HOME=$(find /usr/local/Cellar/openjdk@17 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$INTERNAL_JAVA_HOME" ]; then
                    INTERNAL_JAVA_HOME="${INTERNAL_JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            elif [ -d "/opt/homebrew/Cellar/openjdk@17" ]; then
                # Apple Silicon Mac
                INTERNAL_JAVA_HOME=$(find /opt/homebrew/Cellar/openjdk@17 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$INTERNAL_JAVA_HOME" ]; then
                    INTERNAL_JAVA_HOME="${INTERNAL_JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            fi
            ;;
        Linux)
            # Linux: 查找常见Java 17安装路径
            for java_path in \
                "/usr/lib/jvm/java-17-openjdk-amd64" \
                "/usr/lib/jvm/java-17-openjdk" \
                "/usr/lib/jvm/jdk-17" \
                "/opt/jdk-17" \
                "/usr/lib/jvm/temurin-17-jdk-amd64" \
                "/usr/lib/jvm/jdk-17-oracle-x64"; do
                if [ -d "$java_path" ]; then
                    INTERNAL_JAVA_HOME="$java_path"
                    break
                fi
            done
            ;;
        Windows)
            # Windows: 查找常见Java 17安装路径
            if [ -d "/c/Program Files/Java/jdk-17" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files/Java/jdk-17"
            elif [ -d "/c/Program Files (x86)/Java/jdk-17" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files (x86)/Java/jdk-17"
            elif [ -d "/c/Program Files/Java/jdk-17.0" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files/Java/jdk-17.0"
            fi
            ;;
    esac

    # 验证Java版本
    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "17" ]; then
            print_success "自动检测到Java 17: $INTERNAL_JAVA_HOME"
            return 0
        else
            print_warning "检测到的Java版本不是17: $JAVA_VERSION"
            return 1
        fi
    fi

    print_error "未找到Java 17，请确保已安装JDK 17"
    return 1
}

# 调用函数设置INTERNAL_JAVA_HOME
auto_set_java_home
if [ $? -ne 0 ]; then
    exit 1
fi

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 检查依赖
print_info "检查依赖..."

if ! command_exists java; then
    print_error "未找到Java，请先安装JDK 17"
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

# 第一步：构建前端
print_info "=========================================="
print_info "第一步：构建前端..."
print_info "=========================================="
cd frontend || exit 1

# 检查是否需要安装依赖
if [ ! -d "node_modules" ] || [ "package-lock.json" -nt "node_modules/.last-install" ] || [ "package.json" -nt "node_modules/.last-install" ]; then
    print_info "检测到依赖变更或node_modules不存在，安装npm依赖..."
    # 先清理旧的node_modules确保干净安装
    if [ -d "node_modules" ]; then
        print_info "清理旧的node_modules..."
        rm -rf node_modules
    fi
    npm ci --prefer-offline --no-audit --no-fund

    if [ $? -ne 0 ]; then
        print_error "前端依赖安装失败，尝试使用npm install..."
        npm install
        if [ $? -ne 0 ]; then
            print_error "前端依赖安装失败"
            exit 1
        fi
    fi
    # 记录安装时间
    touch node_modules/.last-install
else
    print_info "node_modules已存在且为最新，跳过依赖安装"
fi

print_info "构建前端生产版本..."
# 设置前端环境变量（将后端端口传递给前端）
export VITE_SERVER_PORT=$SERVER_PORT
export VITE_FRONTEND_PORT=$FRONTEND_PORT
npm run build

if [ $? -ne 0 ]; then
    print_error "前端构建失败"
    exit 1
fi

print_success "前端构建成功，静态文件位于: $(pwd)/dist"

# 返回根目录
cd ..

# 第二步：构建后端（包含前端静态文件）
print_info "=========================================="
print_info "第二步：构建后端（包含前端静态文件）..."
print_info "=========================================="
cd backend || exit 1

# 检查是否需要更新依赖
if [ ! -d "target" ] || [ "pom.xml" -nt "target/.last-build" ]; then
    print_info "检测到依赖变更，更新Maven依赖..."
    mvn dependency:resolve
    if [ $? -ne 0 ]; then
        print_error "Maven依赖解析失败"
        exit 1
    fi
fi

print_info "清理并编译..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    print_error "后端编译失败"
    exit 1
fi

print_info "打包（前端静态文件将自动复制到jar包中）..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    print_error "后端打包失败"
    exit 1
fi

# 记录构建时间
touch target/.last-build

print_success "后端构建成功"

# 返回根目录
cd ..

print_info "=========================================="
print_success "构建完成！"
print_info "=========================================="
print_info "JAR包位置: backend/target/mock-server-1.0.0.jar"
print_info ""
print_info "运行命令:"
print_info "  java -jar backend/target/mock-server-1.0.0.jar"
print_info ""
print_info "或者直接运行一键启动脚本:"
print_info "  ./run.sh"
