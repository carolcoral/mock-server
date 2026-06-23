#!/bin/bash

echo "=========================================="
echo "Mock Server 运行脚本 v2.1.2"
echo "=========================================="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() { echo -e "${GREEN}[✓]${NC} $1"; }
print_error()   { echo -e "${RED}[✗]${NC} $1"; }
print_info()    { echo -e "${YELLOW}[→]${NC} $1"; }

# 获取脚本所在目录
if [ -L "$0" ]; then
    SCRIPT_PATH="$(readlink -f "$0" 2>/dev/null || readlink "$0" 2>/dev/null || echo "$0")"
else
    SCRIPT_PATH="$0"
fi
SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_PATH")" 2>/dev/null && pwd)"
cd "$SCRIPT_DIR" 2>/dev/null || { print_error "无法进入脚本目录: $SCRIPT_DIR"; exit 1; }

# 查找 jar 包
find_jar() {
    local jar=""
    jar=$(find "$SCRIPT_DIR" -maxdepth 1 -name "mock-server-*.jar" -type f 2>/dev/null | head -n 1)
    [ -n "$jar" ] && [ -f "$jar" ] && { echo "$jar"; return 0; }
    jar=$(find "$SCRIPT_DIR/backend/target" -maxdepth 1 -name "mock-server-*.jar" -type f 2>/dev/null | head -n 1)
    [ -n "$jar" ] && [ -f "$jar" ] && { echo "$jar"; return 0; }
    return 1
}

JAR_FILE=$(find_jar)

if [ -z "$JAR_FILE" ]; then
    print_info "未找到 jar 包，开始构建..."
    bash ./build-all-in-one.sh || { print_error "构建失败"; exit 1; }
    JAR_FILE=$(find_jar)
else
    print_info "jar 包已存在，跳过构建"
fi

# 加载 .env
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
else
    print_error "未找到 .env 配置文件"
    exit 1
fi

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

# 自动检测 Java 21
INTERNAL_JAVA_HOME=""
auto_set_java_home() {
    # 优先检查已设置的 INTERNAL_JAVA_HOME
    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        [ "$JAVA_VERSION" = "21" ] && return 0
    fi

    # 检查全局 JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            INTERNAL_JAVA_HOME="$JAVA_HOME"
            return 0
        fi
    fi

    case "${PLATFORM}" in
        Mac)
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                INTERNAL_JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
                [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ] && return 0
            fi
            for prefix in /usr/local /opt/homebrew; do
                if [ -d "$prefix/Cellar/openjdk@21" ]; then
                    INTERNAL_JAVA_HOME=$(find "$prefix/Cellar/openjdk@21" -name "libexec" -type d 2>/dev/null | head -n 1)
                    [ -n "$INTERNAL_JAVA_HOME" ] && INTERNAL_JAVA_HOME="${INTERNAL_JAVA_HOME}/openjdk.jdk/Contents/Home"
                    [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ] && return 0
                fi
            done
            ;;
        Linux)
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
            for java_path in "/c/Program Files/Java/jdk-21" "/c/Program Files/Java/jdk-21.0"; do
                if [ -d "$java_path" ]; then
                    INTERNAL_JAVA_HOME="$java_path"
                    break
                fi
            done
            ;;
    esac

    if [ -n "$INTERNAL_JAVA_HOME" ] && [ -x "$INTERNAL_JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$($INTERNAL_JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            return 0
        fi
    fi

    print_error "未找到 Java 21，请确保已安装 JDK 21"
    return 1
}

auto_set_java_home || exit 1
print_success "Java 21: $INTERNAL_JAVA_HOME"

# 确认 jar 包
JAR_FILE=$(find_jar)
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    print_error "jar 包不存在，请先构建: ./build-all-in-one.sh"
    exit 1
fi

# 工作目录
JAR_DIR=$(dirname "$JAR_FILE")
if [ "$JAR_DIR" = "$SCRIPT_DIR" ]; then
    DATA_DIR="$SCRIPT_DIR/data"
    LOG_DIR="$SCRIPT_DIR/logs"
else
    DATA_DIR="$SCRIPT_DIR/backend/data"
    LOG_DIR="$SCRIPT_DIR/backend/logs"
fi
mkdir -p "$DATA_DIR" "$LOG_DIR"

PID_FILE="$SCRIPT_DIR/.pid"

# 停止已有进程
if [ -f "$PID_FILE" ]; then
    PID_FROM_FILE=$(cat "$PID_FILE" 2>/dev/null)
    if [ ! -z "$PID_FROM_FILE" ] && kill -0 "$PID_FROM_FILE" 2>/dev/null; then
        print_info "停止已有进程 (PID: $PID_FROM_FILE)..."
        kill "$PID_FROM_FILE" 2>/dev/null
        sleep 2
        kill -0 "$PID_FROM_FILE" 2>/dev/null && { kill -9 "$PID_FROM_FILE" 2>/dev/null; sleep 1; }
    else
        rm -f "$PID_FILE"
    fi
fi

# 兜底：通过端口清理
OLD_PID=$(lsof -ti:$SERVER_PORT 2>/dev/null || ss -tlnp 2>/dev/null | grep -oP "pid=\K[0-9]+" | head -1)
if [ ! -z "$OLD_PID" ] && [ "$OLD_PID" != "$PID_FROM_FILE" ]; then
    kill "$OLD_PID" 2>/dev/null
    sleep 2
    kill -0 "$OLD_PID" 2>/dev/null && kill -9 "$OLD_PID" 2>/dev/null
fi

# 构造启动参数
if [ "$JAR_DIR" = "$SCRIPT_DIR" ]; then
    JAVA_DB_URL="jdbc:sqlite:./data/mock-server.db"
    JAVA_LOG_PATH="./logs/mock-server.log"
else
    JAVA_DB_URL="jdbc:sqlite:./backend/data/mock-server.db"
    JAVA_LOG_PATH="./backend/logs/mock-server.log"
fi

# 启动服务
print_info "启动后端服务 (端口: $SERVER_PORT)..."
nohup "$INTERNAL_JAVA_HOME/bin/java" \
    -DDB_URL="$JAVA_DB_URL" \
    -DLOG_FILE_PATH="$JAVA_LOG_PATH" \
    -jar "$JAR_FILE" \
    > "$LOG_DIR/server.log" 2>&1 &

NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"
print_success "进程已启动 (PID: $NEW_PID)"

# 等待启动
print_info "等待服务就绪..."
sleep 10

# 检测服务状态
HEALTH_URL="http://localhost:${SERVER_PORT}/actuator/health"
FALLBACK_URL="http://localhost:${SERVER_PORT}/api/v3/api-docs"
MAX_RETRIES=3
RETRY_COUNT=0
STARTED=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RETRY_COUNT=$((RETRY_COUNT + 1))

    # 方案1: actuator/health（公开）
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$HEALTH_URL" 2>/dev/null)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
        STARTED=true; break
    fi

    # 方案2: API docs 可达即认为成功
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$FALLBACK_URL" 2>/dev/null)
    if [ "$HTTP_CODE" != "000" ]; then
        STARTED=true; break
    fi

    # 方案3: 进程存活 + 端口监听
    if [ -f "$PID_FILE" ]; then
        PID_CHECK=$(cat "$PID_FILE" 2>/dev/null)
        if [ -n "$PID_CHECK" ] && kill -0 "$PID_CHECK" 2>/dev/null; then
            if command -v ss >/dev/null 2>&1; then
                ss -tlnp 2>/dev/null | grep -q ":${SERVER_PORT}" && { STARTED=true; break; }
            elif command -v netstat >/dev/null 2>&1; then
                netstat -tlnp 2>/dev/null | grep -q ":${SERVER_PORT}" && { STARTED=true; break; }
            fi
        fi
    fi

    [ $RETRY_COUNT -lt $MAX_RETRIES ] && sleep 2
done

if [ "$STARTED" = true ]; then
    print_success "服务启动成功"
else
    print_error "服务启动失败，请查看日志: $LOG_DIR/server.log"
    exit 1
fi

echo ""
echo "=========================================="
print_success "Mock Server 已启动！"
echo "=========================================="
echo "  访问地址:  http://localhost:${SERVER_PORT}"
echo "  API 文档:  http://localhost:${SERVER_PORT}/swagger-ui.html"
echo "  运行日志:  $LOG_DIR/server.log"
echo "  停止服务:  kill \$(cat $PID_FILE)"
echo "=========================================="
