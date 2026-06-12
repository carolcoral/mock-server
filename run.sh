#!/bin/bash

echo "=========================================="
echo "Mock Server 运行脚本"
echo "作者: carolcoral"
echo "=========================================="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

print_step() {
    echo -e "${BLUE}[步骤]${NC} $1"
}

# 获取脚本所在目录（确保路径始终相对于脚本位置）
# 兼容 bash run.sh 和 ./run.sh 两种执行方式
if [ -L "$0" ]; then
    SCRIPT_PATH="$(readlink -f "$0" 2>/dev/null || readlink "$0" 2>/dev/null || echo "$0")"
else
    SCRIPT_PATH="$0"
fi
SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_PATH")" 2>/dev/null && pwd)"
cd "$SCRIPT_DIR" 2>/dev/null || { echo "[错误] 无法进入脚本目录: $SCRIPT_DIR"; exit 1; }

# 打印调试信息（帮助定位麒麟服务器上的路径问题）
echo "[调试] 脚本路径: $SCRIPT_PATH"
echo "[调试] 脚本目录: $SCRIPT_DIR"
echo "[调试] 当前目录: $(pwd)"
echo "[调试] 目录内容: $(ls -la "$SCRIPT_DIR" 2>/dev/null | head -20)"

# 查找 jar 包：使用 find 命令（最跨平台可靠的方式）
find_jar() {
    local jar=""
    # 1. 优先检查与 run.sh 同级目录的 jar
    jar=$(find "$SCRIPT_DIR" -maxdepth 1 -name "mock-server-*.jar" -type f 2>/dev/null | head -n 1)
    if [ -n "$jar" ] && [ -f "$jar" ]; then
        echo "$jar"
        return 0
    fi
    # 2. 检查 backend/target 目录
    jar=$(find "$SCRIPT_DIR/backend/target" -maxdepth 1 -name "mock-server-*.jar" -type f 2>/dev/null | head -n 1)
    if [ -n "$jar" ] && [ -f "$jar" ]; then
        echo "$jar"
        return 0
    fi
    return 1
}

JAR_FILE=$(find_jar)
echo "[调试] find_jar 结果: '$JAR_FILE'"

print_step "检查是否需要构建..."

if [ -z "$JAR_FILE" ]; then
    print_info "未找到 jar 包，开始构建..."
    print_info "正在运行 ./build-all-in-one.sh ..."
    bash ./build-all-in-one.sh

    if [ $? -ne 0 ]; then
        print_error "构建失败，请检查错误信息"
        exit 1
    fi

    print_success "构建完成"
    JAR_FILE=$(find_jar)
    echo "[调试] 构建后 find_jar 结果: '$JAR_FILE'"
else
    print_info "jar包已存在，跳过构建"
    print_info "如需重新构建，请运行: ./build-all-in-one.sh"
fi

echo ""

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

print_info "后端端口: $SERVER_PORT"
print_info "前端端口: $FRONTEND_PORT"

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM=Linux;;
    Darwin*)    PLATFORM=Mac;;
    CYGWIN*|MINGW32*|MSYS*|MINGW*) PLATFORM=Windows;;
    *)          PLATFORM="UNKNOWN:${OS}"
esac

# 自动检测并设置INTERNAL_JAVA_HOME（检测Java 21）
auto_set_java_home() {
    print_info "检测Java环境..."
    
    # 优先检查INTERNAL_JAVA_HOME是否已设置
    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            print_success "使用已配置的INTERNAL_JAVA_HOME: $INTERNAL_JAVA_HOME (Java $JAVA_VERSION)"
            return 0
        fi
    fi
    
    # 检查全局JAVA_HOME是否为Java 21，如果是则复制到INTERNAL_JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            INTERNAL_JAVA_HOME="$JAVA_HOME"
            print_success "从全局JAVA_HOME复制: $INTERNAL_JAVA_HOME (Java $JAVA_VERSION)"
            return 0
        fi
    fi
    
    # 根据操作系统查找Java 21
    case "${PLATFORM}" in
        Mac)
            # macOS: 使用java_home命令查找Java 21
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                INTERNAL_JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
                if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
                    print_success "通过java_home命令检测到Java 21: $INTERNAL_JAVA_HOME"
                    return 0
                fi
            fi
            
            # 备用方案：查找Homebrew安装的Java 21
            if [ -d "/usr/local/Cellar/openjdk@21" ]; then
                INTERNAL_JAVA_HOME=$(find /usr/local/Cellar/openjdk@21 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$INTERNAL_JAVA_HOME" ]; then
                    INTERNAL_JAVA_HOME="${INTERNAL_JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            elif [ -d "/opt/homebrew/Cellar/openjdk@21" ]; then
                # Apple Silicon Mac
                INTERNAL_JAVA_HOME=$(find /opt/homebrew/Cellar/openjdk@21 -name "libexec" -type d 2>/dev/null | head -n 1)
                if [ -n "$INTERNAL_JAVA_HOME" ]; then
                    INTERNAL_JAVA_HOME="${INTERNAL_JAVA_HOME}/openjdk.jdk/Contents/Home"
                fi
            fi
            ;;
        Linux)
            # Linux: 查找常见Java 21安装路径
            for java_path in \
                "/usr/lib/jvm/java-21-openjdk-amd64" \
                "/usr/lib/jvm/java-21-openjdk" \
                "/usr/lib/jvm/jdk-21" \
                "/opt/jdk-21" \
                "/usr/lib/jvm/temurin-21-jdk-amd64" \
                "/usr/lib/jvm/jdk-21-oracle-x64"; do
                if [ -d "$java_path" ]; then
                    INTERNAL_JAVA_HOME="$java_path"
                    break
                fi
            done
            ;;
        Windows)
            # Windows: 查找常见Java 21安装路径
            if [ -d "/c/Program Files/Java/jdk-21" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files/Java/jdk-21"
            elif [ -d "/c/Program Files (x86)/Java/jdk-21" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files (x86)/Java/jdk-21"
            elif [ -d "/c/Program Files/Java/jdk-21.0" ]; then
                INTERNAL_JAVA_HOME="/c/Program Files/Java/jdk-21.0"
            fi
            ;;
    esac
    
    # 验证Java版本
    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            print_success "自动检测到Java 21: $INTERNAL_JAVA_HOME"
            return 0
        else
            print_info "检测到的Java版本不是17: $JAVA_VERSION"
            return 1
        fi
    fi
    
    print_error "未找到Java 21，请确保已安装JDK 21"
    return 1
}

# 调用函数设置INTERNAL_JAVA_HOME
auto_set_java_home
if [ $? -ne 0 ]; then
    exit 1
fi

# 最终确认 jar 包是否存在
JAR_FILE=$(find_jar)
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    print_error "后端jar包不存在，请先运行构建脚本: ./build-all-in-one.sh"
    exit 1
fi
print_info "找到jar包: $(basename $JAR_FILE)"

# 根据 jar 包位置确定工作目录（data/logs 放在 jar 包同级或 backend 目录下）
JAR_DIR=$(dirname "$JAR_FILE")
if [ "$JAR_DIR" = "$SCRIPT_DIR" ]; then
    # jar 包与 run.sh 同级，data/logs 也放在同级
    DATA_DIR="$SCRIPT_DIR/data"
    LOG_DIR="$SCRIPT_DIR/logs"
else
    # jar 包在 backend/target 下，保持原有行为
    DATA_DIR="$SCRIPT_DIR/backend/data"
    LOG_DIR="$SCRIPT_DIR/backend/logs"
fi

# 创建数据目录
mkdir -p "$DATA_DIR"
mkdir -p "$LOG_DIR"
print_info "数据目录: $DATA_DIR"
print_info "日志目录: $LOG_DIR"

# PID文件路径（放在项目根目录）
PID_FILE="$SCRIPT_DIR/.pid"

# 查找并停止已存在的后端进程
print_info "检查是否已有后端服务在运行..."

# 方法1：通过PID文件查找并终止（优先，与端口无关）
if [ -f "$PID_FILE" ]; then
    PID_FROM_FILE=$(cat "$PID_FILE" 2>/dev/null)
    if [ ! -z "$PID_FROM_FILE" ] && kill -0 "$PID_FROM_FILE" 2>/dev/null; then
        print_info "发现已有后端进程 (PID文件: $PID_FROM_FILE)，正在停止..."
        kill "$PID_FROM_FILE" 2>/dev/null
        sleep 2
        if kill -0 "$PID_FROM_FILE" 2>/dev/null; then
            print_info "进程仍在运行，强制终止..."
            kill -9 "$PID_FROM_FILE" 2>/dev/null
            sleep 1
        fi
        print_success "旧进程已停止（通过PID文件）"
    else
        print_info "PID文件中的进程已不存在，清理PID文件"
        rm -f "$PID_FILE"
    fi
fi

# 方法2：通过端口查找并终止（兜底，处理PID文件丢失的情况）
OLD_PID=$(lsof -ti:$SERVER_PORT 2>/dev/null || netstat -tuln 2>/dev/null | grep :$SERVER_PORT | awk '{print $NF}' | sed 's/.*://' || ss -tuln 2>/dev/null | grep :$SERVER_PORT | awk '{print $NF}' | sed 's/.*://')

if [ ! -z "$OLD_PID" ]; then
    # 检查是否是同一个进程（避免重复kill）
    if [ "$OLD_PID" != "$PID_FROM_FILE" ]; then
        print_info "发现已有进程占用${SERVER_PORT}端口 (PID: $OLD_PID)，正在停止..."
        kill $OLD_PID 2>/dev/null
        sleep 2
        if kill -0 $OLD_PID 2>/dev/null; then
            print_info "进程仍在运行，强制终止..."
            kill -9 $OLD_PID 2>/dev/null
            sleep 1
        fi
        print_success "旧进程已停止（通过端口检测）"
    fi
elif [ ! -f "$PID_FILE" ]; then
    print_info "未发现运行中的后端进程"
fi

# 构造 Java 启动参数
# jar 包与 run.sh 同级时，DB_URL 和 LOG_FILE_PATH 使用相对于工作目录的路径
if [ "$JAR_DIR" = "$SCRIPT_DIR" ]; then
    # 同级模式：工作目录为脚本所在目录，使用相对路径
    JAVA_DB_URL="jdbc:sqlite:./data/mock-server.db"
    JAVA_LOG_PATH="./logs/mock-server.log"
else
    JAVA_DB_URL="jdbc:sqlite:./backend/data/mock-server.db"
    JAVA_LOG_PATH="./backend/logs/mock-server.log"
fi

print_info "启动后端服务..."
nohup "$INTERNAL_JAVA_HOME/bin/java" \
    -DDB_URL="$JAVA_DB_URL" \
    -DLOG_FILE_PATH="$JAVA_LOG_PATH" \
    -jar "$JAR_FILE" \
    > "$LOG_DIR/server.log" 2>&1 &

NEW_PID=$!
if [ $? -eq 0 ]; then
    # 保存PID到文件
    echo "$NEW_PID" > "$PID_FILE"
    print_success "后端服务已启动 (PID: $NEW_PID)"
    print_info "PID文件: $PID_FILE"
    print_info "日志文件: $LOG_DIR/server.log"
    print_info "jar包: $(basename $JAR_FILE)"
else
    print_error "后端服务启动失败"
    exit 1
fi

# 等待后端启动
print_info "等待后端服务启动..."
sleep 10

# 检查后端是否正常运行
if curl -s http://localhost:${SERVER_PORT}/api/v3/api-docs > /dev/null; then
    print_success "后端服务正常运行"
else
    print_error "后端服务启动失败，请检查日志"
    exit 1
fi

print_info "=========================================="
print_success "Mock Server 已启动！"
print_info "=========================================="
print_info "后端服务地址: http://localhost:${SERVER_PORT}"
print_info "前端界面地址: http://localhost:${SERVER_PORT}"
print_info "API文档地址: http://localhost:${SERVER_PORT}/swagger-ui.html"
print_info "日志文件: $LOG_DIR/server.log"
print_info ""
print_info "提示: 前端静态文件已集成到后端，无需单独启动前端服务"
print_info ""
print_info "停止服务: kill $!"

