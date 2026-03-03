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

print_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM=Linux;;
    Darwin*)    PLATFORM=Mac;;
    CYGWIN*|MINGW32*|MSYS*|MINGW*) PLATFORM=Windows;;
    *)          PLATFORM="UNKNOWN:${OS}"
esac

# 自动检测并设置JAVA_HOME（优先使用Java 17）
auto_set_java_home() {
    print_info "检测Java环境..."
    
    # 如果JAVA_HOME已设置且是Java 17，直接使用
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1,2)
        if [ "$JAVA_VERSION" = "17" ]; then
            print_success "使用已配置的JAVA_HOME: $JAVA_HOME (Java $JAVA_VERSION)"
            return 0
        fi
    fi
    
    # 根据操作系统查找Java 17
    case "${PLATFORM}" in
        Mac)
            # macOS: 查找Homebrew安装的Java 17
            if [ -d "/usr/local/Cellar/openjdk@17" ]; then
                JAVA_HOME=$(find /usr/local/Cellar/openjdk@17 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$JAVA_HOME" ]; then
                    JAVA_HOME="${JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            elif [ -d "/opt/homebrew/Cellar/openjdk@17" ]; then
                # Apple Silicon Mac
                JAVA_HOME=$(find /opt/homebrew/Cellar/openjdk@17 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$JAVA_HOME" ]; then
                    JAVA_HOME="${JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            fi
            ;;
        Linux)
            # Linux: 查找常见Java安装路径
            for java_path in \
                "/usr/lib/jvm/java-17-openjdk-amd64" \
                "/usr/lib/jvm/java-17-openjdk" \
                "/usr/lib/jvm/jdk-17" \
                "/opt/jdk-17"; do
                if [ -d "$java_path" ]; then
                    JAVA_HOME="$java_path"
                    break
                fi
            done
            ;;
        Windows)
            # Windows: 查找常见Java安装路径
            if [ -d "/c/Program Files/Java/jdk-17" ]; then
                JAVA_HOME="/c/Program Files/Java/jdk-17"
            elif [ -d "/c/Program Files (x86)/Java/jdk-17" ]; then
                JAVA_HOME="/c/Program Files (x86)/Java/jdk-17"
            fi
            ;;
    esac
    
    # 验证Java版本
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        JAVA_SUBVERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f2)
        if [ "$JAVA_VERSION" = "17" ] || ([ "$JAVA_VERSION" = "17" ] && [ "$JAVA_SUBVERSION" = "0" ]); then
            export JAVA_HOME
            export PATH="$JAVA_HOME/bin:$PATH"
            # 将JAVA_HOME传递给Maven
            export MAVEN_OPTS="-Djava.home=$JAVA_HOME $MAVEN_OPTS"
            print_success "自动检测到Java 17: $JAVA_HOME"
            # 验证Maven使用的Java版本
            MAVAEN_JAVA_VERSION=$(mvn -version 2>&1 | grep "Java version" | cut -d':' -f2 | cut -d' ' -f2 | cut -d'.' -f1,2)
            if [ "$MAVAEN_JAVA_VERSION" != "17" ]; then
                print_warning "Maven可能未正确使用Java 17（检测到: $MAVAEN_JAVA_VERSION）"
            fi
            return 0
        fi
    fi
    
    # 如果没有找到Java 17，检查java命令是否可用
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "17" ]; then
            print_success "使用系统Java: $(command -v java) (Java $JAVA_VERSION)"
            return 0
        else
            print_warning "检测到Java版本为 $JAVA_VERSION，建议安装Java 17"
            print_warning "尝试强制设置JAVA_HOME..."
            # 尝试查找Java 17安装路径
            if [ -d "/usr/local/Cellar/openjdk@17" ]; then
                JAVA_HOME="/usr/local/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home"
                export JAVA_HOME
                export PATH="$JAVA_HOME/bin:$PATH"
                print_success "已强制设置JAVA_HOME: $JAVA_HOME"
            fi
            return 0
        fi
    fi
    
    print_warning "未检测到Java，请确保已安装JDK 17+"
}

# 在脚本开始时自动设置JAVA_HOME
auto_set_java_home

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

# 设置Maven使用Java 17
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# 检查是否需要更新依赖
if [ ! -d "target" ] || [ "pom.xml" -nt "target/.last-build" ]; then
    print_info "检测到依赖变更，更新Maven依赖..."
    JAVA_HOME="$JAVA_HOME" mvn dependency:resolve
    if [ $? -ne 0 ]; then
        print_error "Maven依赖解析失败"
        exit 1
    fi
fi

print_info "清理并编译..."
JAVA_HOME="$JAVA_HOME" mvn clean compile -q

if [ $? -ne 0 ]; then
    print_error "后端编译失败，尝试更新依赖..."
    print_info "执行 mvn clean install -U..."
    JAVA_HOME="$JAVA_HOME" mvn clean install -U -DskipTests
    if [ $? -ne 0 ]; then
        print_error "后端编译失败"
        exit 1
    fi
fi

print_info "打包..."
JAVA_HOME="$JAVA_HOME" mvn package -DskipTests -q

if [ $? -ne 0 ]; then
    print_error "后端打包失败"
    exit 1
fi

# 记录构建时间
touch target/.last-build

print_success "后端构建成功"

# 返回根目录
cd ..

# 构建前端
print_info "开始构建前端..."
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
