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

# 检测操作系统
detect_platform() {
    local os
    os="$(uname -s)"
    case "${os}" in
        Linux*)     echo "Linux";;
        Darwin*)    echo "Mac";;
        CYGWIN*|MINGW32*|MSYS*|MINGW*) echo "Windows";;
        *)          echo "Unknown";;
    esac
}
PLATFORM=$(detect_platform)

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

# 多版本 JDK 环境下自动检测并设置 Java 21
check_java21() {
    print_info "检测 JDK 21 环境（支持多版本 JDK）..."

    # 方法1: 检查 JAVA_HOME 是否指向 JDK 21
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VER=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VER" = "21" ]; then
            export JAVA_HOME="$JAVA_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_success "JDK 21 已就绪 (JAVA_HOME): $JAVA_HOME"
            print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
            return 0
        fi
    fi

    # 方法2: 检查系统 PATH 中默认的 java 是否为 21
    if command -v java >/dev/null 2>&1; then
        DEFAULT_JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$DEFAULT_JAVA_VER" = "21" ]; then
            DEFAULT_JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v java)" 2>/dev/null || command -v java)")")
            export JAVA_HOME="$DEFAULT_JAVA_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_success "JDK 21 已就绪 (系统默认): $JAVA_HOME"
            print_info "  Java 版本: $(java -version 2>&1 | head -n 1)"
            return 0
        fi
    fi

    # 方法3: 系统默认 java 不是 21，在多版本环境中查找 JDK 21
    print_info "系统默认 Java 不是 JDK 21，在多版本环境中查找..."

    case "${PLATFORM}" in
        Mac)
            # macOS: 使用 java_home 命令列出所有已安装的 JDK
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                # 先尝试精确匹配 21
                JAVA21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
                if [ -n "$JAVA21_HOME" ] && [ -x "$JAVA21_HOME/bin/java" ]; then
                    export JAVA_HOME="$JAVA21_HOME"
                    export PATH="$JAVA_HOME/bin:$PATH"
                    print_success "JDK 21 已就绪 (java_home): $JAVA_HOME"
                    print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                    return 0
                fi

                # 列出所有已安装的 JDK，查找 21
                print_info "已安装的 JDK 列表:"
                /usr/libexec/java_home -V 2>&1 | grep -E '^\s+\d' | while read -r line; do
                    print_info "  $line"
                done
            fi

            # 备用方案: 查找 Homebrew 安装的 JDK 21
            for brew_prefix in "/opt/homebrew" "/usr/local"; do
                if [ -d "$brew_prefix/Cellar/openjdk@21" ]; then
                    JAVA21_HOME=$(find "$brew_prefix/Cellar/openjdk@21" -name "libexec" -type d 2>/dev/null | head -n 1)
                    if [ -n "$JAVA21_HOME" ]; then
                        JAVA21_HOME="${JAVA21_HOME}/openjdk.jdk/Contents/Home"
                    fi
                    # 也尝试直接查找 jdk 目录
                    if [ -z "$JAVA21_HOME" ] || [ ! -x "$JAVA21_HOME/bin/java" ]; then
                        JAVA21_HOME=$(find "$brew_prefix/Cellar/openjdk@21" -maxdepth 2 -name "Home" -path "*/Contents/*" -type d 2>/dev/null | head -n 1)
                    fi
                    if [ -n "$JAVA21_HOME" ] && [ -x "$JAVA21_HOME/bin/java" ]; then
                        export JAVA_HOME="$JAVA21_HOME"
                        export PATH="$JAVA_HOME/bin:$PATH"
                        print_success "JDK 21 已就绪 (Homebrew): $JAVA_HOME"
                        print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                        return 0
                    fi
                fi
            done
            ;;

        Linux)
            # Linux: 扫描常见 JDK 安装路径，检测多版本
            JVM_BASE="/usr/lib/jvm"
            if [ -d "$JVM_BASE" ]; then
                print_info "扫描 $JVM_BASE 下的 JDK..."
                # 按优先级排列的路径模式
                for pattern in \
                    "java-21-openjdk-amd64" \
                    "java-21-openjdk" \
                    "java-21-openjdk-*" \
                    "jdk-21" \
                    "jdk-21*" \
                    "temurin-21-jdk-amd64" \
                    "jdk-21-oracle-x64" \
                    "java-21-*"; do
                    for java_path in "$JVM_BASE"/$pattern; do
                        if [ -d "$java_path" ] && [ -x "$java_path/bin/java" ]; then
                            JAVA_VER=$("$java_path/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                            if [ "$JAVA_VER" = "21" ]; then
                                export JAVA_HOME="$java_path"
                                export PATH="$JAVA_HOME/bin:$PATH"
                                print_success "JDK 21 已就绪: $JAVA_HOME"
                                print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                                return 0
                            fi
                        fi
                    done
                done

                # 列出所有已安装的 JDK，帮助用户排查
                print_info "已安装的 JDK 列表:"
                for jvm_dir in "$JVM_BASE"/*/; do
                    if [ -x "$jvm_dir/bin/java" ]; then
                        jvm_ver=$("$jvm_dir/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2)
                        print_info "  $jvm_dir ($jvm_ver)"
                    fi
                done
            fi

            # 额外扫描 /opt 下的 JDK
            for opt_dir in /opt/jdk-21 /opt/jdk-21*; do
                if [ -d "$opt_dir" ] && [ -x "$opt_dir/bin/java" ]; then
                    JAVA_VER=$("$opt_dir/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                    if [ "$JAVA_VER" = "21" ]; then
                        export JAVA_HOME="$opt_dir"
                        export PATH="$JAVA_HOME/bin:$PATH"
                        print_success "JDK 21 已就绪: $JAVA_HOME"
                        print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                        return 0
                    fi
                fi
            done

            # sdkman 安装的 JDK
            if [ -d "$HOME/.sdkman/candidates/java" ]; then
                for sdk_java in "$HOME/.sdkman/candidates/java"/21.*/; do
                    if [ -d "$sdk_java" ] && [ -x "$sdk_java/bin/java" ]; then
                        JAVA_VER=$("$sdk_java/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                        if [ "$JAVA_VER" = "21" ]; then
                            export JAVA_HOME="$sdk_java"
                            export PATH="$JAVA_HOME/bin:$PATH"
                            print_success "JDK 21 已就绪 (sdkman): $JAVA_HOME"
                            print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                            return 0
                        fi
                    fi
                done
            fi
            ;;

        Windows)
            for win_path in \
                "/c/Program Files/Java/jdk-21" \
                "/c/Program Files/Java/jdk-21.0" \
                "/c/Program Files (x86)/Java/jdk-21"; do
                if [ -d "$win_path" ] && [ -x "$win_path/bin/java" ]; then
                    export JAVA_HOME="$win_path"
                    export PATH="$JAVA_HOME/bin:$PATH"
                    print_success "JDK 21 已就绪: $JAVA_HOME"
                    print_info "  Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
                    return 0
                fi
            done
            ;;
    esac

    # 未找到 JDK 21
    print_error "未找到 JDK 21，请先安装 JDK 21"
    echo ""
    print_info "系统默认 Java 版本: $(java -version 2>&1 | head -n 1 2>/dev/null || echo '未安装')"
    print_info ""
    print_info "安装方式:"
    print_info "  1. 运行 ./setup-env.sh 自动安装 JDK 21"
    print_info "  2. 手动下载: https://adoptium.net/download/"
    print_info "  3. 使用 SDKMAN: curl -s \"https://get.sdkman.io\" | bash && sdk install java 21.0.5-tem"
    exit 1
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

    # 根据检测到的 JDK 21 路径给出准确的 java 命令
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_RUNTIME="$JAVA_HOME/bin/java"
    else
        JAVA_RUNTIME="java"
    fi
    print_info "  $JAVA_RUNTIME -jar $JAR_FILE"
    echo ""
    print_info "或者直接运行一键启动脚本:"
    print_info "  ./run.sh"
else
    print_warning "未找到 JAR 包，请检查 backend/target/ 目录"
fi
