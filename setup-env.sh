#!/bin/bash

echo "=========================================="
echo "Mock Server 环境安装脚本"
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

print_info "检测到操作系统: ${PLATFORM}"

# 检测 Linux 发行版
detect_linux_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO=$ID
        DISTRO_VERSION=$VERSION_ID
        # 处理子发行版（如 ubuntu 的衍生版）
        if [ -n "$ID_LIKE" ]; then
            DISTRO_LIKE=$ID_LIKE
        fi
    elif [ -f /etc/redhat-release ]; then
        DISTRO="rhel"
    elif [ -f /etc/debian_version ]; then
        DISTRO="debian"
    else
        DISTRO="unknown"
    fi
    # 导出为全局变量，方便其他函数使用
    export DISTRO
    export DISTRO_VERSION
    export DISTRO_LIKE
    echo "$DISTRO"
}

# 确保 sudo 命令可用（若缺失则自动安装）
ensure_sudo() {
    if command -v sudo >/dev/null 2>&1; then
        print_info "sudo 命令已可用"
        return 0
    fi

    # 已经是 root 用户，直接定义 sudo 为空函数即可
    if [ "$(id -u)" -eq 0 ]; then
        print_info "当前为 root 用户，无需 sudo"
        # 定义一个空的 sudo 函数，让后续脚本中的 sudo xxx 直接执行 xxx
        sudo() { "$@"; }
        export -f sudo 2>/dev/null || true
        return 0
    fi

    print_warning "未检测到 sudo 命令，尝试安装..."

    # 先用 su 切换到 root 安装 sudo
    if [ "$PLATFORM" != "Linux" ]; then
        print_error "非 Linux 系统且缺少 sudo，请手动安装"
        return 1
    fi

    # 提前检测发行版（如果尚未检测）
    if [ -z "$DISTRO" ]; then
        detect_linux_distro > /dev/null
    fi

    print_info "检测到发行版: $DISTRO (版本: ${DISTRO_VERSION:-未知})"

    case "$DISTRO" in
        ubuntu|debian|linuxmint|pop|elementary|zorin)
            print_info "使用 apt-get 安装 sudo..."
            su -c "apt-get update -qq && apt-get install -y -qq sudo" root
            ;;
        rhel|centos|fedora|rocky|almalinux|ol|amzn)
            if command -v dnf >/dev/null 2>&1; then
                su -c "dnf install -y sudo" root
            elif command -v yum >/dev/null 2>&1; then
                su -c "yum install -y sudo" root
            fi
            ;;
        suse|opensuse*|sled|sles)
            su -c "zypper install -y sudo" root
            ;;
        alpine)
            su -c "apk add sudo" root
            ;;
        arch|manjaro|endeavouros)
            su -c "pacman -S --noconfirm sudo" root
            ;;
        *)
            # 兜底：通过 DISTRO_LIKE 判断
            case "${DISTRO_LIKE:-}" in
                *debian*|*ubuntu*)
                    su -c "apt-get update -qq && apt-get install -y -qq sudo" root
                    ;;
                *rhel*|*fedora*|*centos*)
                    if command -v dnf >/dev/null 2>&1; then
                        su -c "dnf install -y sudo" root
                    elif command -v yum >/dev/null 2>&1; then
                        su -c "yum install -y sudo" root
                    fi
                    ;;
                *suse*)
                    su -c "zypper install -y sudo" root
                    ;;
                *arch*)
                    su -c "pacman -S --noconfirm sudo" root
                    ;;
                *)
                    print_error "无法自动安装 sudo，请手动安装后重试"
                    return 1
                    ;;
            esac
            ;;
    esac

    if command -v sudo >/dev/null 2>&1; then
        print_success "sudo 安装成功"
        return 0
    else
        print_error "sudo 安装失败，请手动安装"
        return 1
    fi
}

# 检查是否有 root 权限
check_root_or_sudo() {
    if [ "$PLATFORM" = "Windows" ]; then
        return 0  # Windows 下不需要 sudo 检测
    fi
    
    if [ "$(id -u)" -eq 0 ]; then
        return 0  # 已经是 root
    fi
    
    if command -v sudo >/dev/null 2>&1; then
        if sudo -n true 2>/dev/null; then
            return 0  # 有免密 sudo 权限
        else
            print_warning "需要管理员权限来安装 JDK 21"
            print_warning "脚本将在需要时请求 sudo 密码"
            return 1
        fi
    else
        print_warning "未找到 sudo 命令，可能需要以 root 用户运行此脚本"
        return 1
    fi
}

# 检查 Java 21 是否已安装
check_java21_installed() {
    print_step "检查 Java 21 环境..."
    
    # 方法1: 检查 JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            print_success "Java 21 已安装 (JAVA_HOME): $JAVA_HOME"
            print_info "Java 版本: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
            return 0
        fi
    fi
    
    # 方法2: 检查系统 PATH 中的 java
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            JAVA_PATH=$(command -v java)
            JAVA_HOME_PATH=$(dirname "$(dirname "$(readlink -f "$JAVA_PATH" 2>/dev/null || echo "$JAVA_PATH")")")
            print_success "Java 21 已安装 (PATH): $JAVA_PATH"
            print_info "Java 版本: $(java -version 2>&1 | head -n 1)"
            return 0
        fi
    fi
    
    # 方法3: 检查常见安装路径
    case "${PLATFORM}" in
        Mac)
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                JAVA21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
                if [ -n "$JAVA21_HOME" ] && [ -x "$JAVA21_HOME/bin/java" ]; then
                    print_success "Java 21 已安装: $JAVA21_HOME"
                    print_info "Java 版本: $("$JAVA21_HOME/bin/java" -version 2>&1 | head -n 1)"
                    return 0
                fi
            fi
            ;;
        Linux)
            for java_path in \
                "/usr/lib/jvm/java-21-openjdk-amd64" \
                "/usr/lib/jvm/java-21-openjdk" \
                "/usr/lib/jvm/jdk-21" \
                "/opt/jdk-21" \
                "/usr/lib/jvm/temurin-21-jdk-amd64" \
                "/usr/lib/jvm/jdk-21-oracle-x64"; do
                if [ -d "$java_path" ] && [ -x "$java_path/bin/java" ]; then
                    JAVA_VERSION=$("$java_path/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                    if [ "$JAVA_VERSION" = "21" ]; then
                        print_success "Java 21 已安装: $java_path"
                        print_info "Java 版本: $("$java_path/bin/java" -version 2>&1 | head -n 1)"
                        return 0
                    fi
                fi
            done
            ;;
    esac
    
    print_warning "未检测到 Java 21"
    return 1
}

# 获取当前 JDK 21 的主目录
get_java21_home() {
    # 优先使用 JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            echo "$JAVA_HOME"
            return 0
        fi
    fi
    
    case "${PLATFORM}" in
        Mac)
            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                JAVA21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
                if [ -n "$JAVA21_HOME" ]; then
                    echo "$JAVA21_HOME"
                    return 0
                fi
            fi
            ;;
        Linux)
            for java_path in \
                "/usr/lib/jvm/java-21-openjdk-amd64" \
                "/usr/lib/jvm/java-21-openjdk" \
                "/usr/lib/jvm/jdk-21" \
                "/opt/jdk-21" \
                "/usr/lib/jvm/temurin-21-jdk-amd64" \
                "/usr/lib/jvm/jdk-21-oracle-x64"; do
                if [ -d "$java_path" ] && [ -x "$java_path/bin/java" ]; then
                    JAVA_VERSION=$("$java_path/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                    if [ "$JAVA_VERSION" = "21" ]; then
                        echo "$java_path"
                        return 0
                    fi
                fi
            done
            ;;
    esac
    
    # 通过 PATH 查找
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            JAVA_CMD=$(command -v java)
            JAVA_HOME_PATH=$(dirname "$(dirname "$(readlink -f "$JAVA_CMD" 2>/dev/null || echo "$JAVA_CMD")")")
            echo "$JAVA_HOME_PATH"
            return 0
        fi
    fi
    
    return 1
}

# 在 macOS 上安装 JDK 21
install_java21_mac() {
    print_step "在 macOS 上安装 JDK 21..."
    
    # 检查是否安装了 Homebrew
    if command -v brew >/dev/null 2>&1; then
        print_info "使用 Homebrew 安装 OpenJDK 21..."
        brew install openjdk@21
        
        if [ $? -eq 0 ]; then
            print_success "OpenJDK 21 安装完成"
            
            # 创建符号链接
            print_info "创建系统符号链接..."
            if [ -d "/usr/local/opt/openjdk@21" ]; then
                sudo ln -sfn /usr/local/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk 2>/dev/null || true
            elif [ -d "/opt/homebrew/opt/openjdk@21" ]; then
                sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk 2>/dev/null || true
            fi
            
            # 设置环境变量提示
            JAVA21_HOME=$(get_java21_home)
            if [ -n "$JAVA21_HOME" ]; then
                print_info "Java 21 安装路径: $JAVA21_HOME"
                print_info ""
                print_warning "请将以下内容添加到 ~/.zshrc 或 ~/.bash_profile："
                echo ""
                echo "  export JAVA_HOME=$JAVA21_HOME"
                echo "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
                echo ""
            fi
            return 0
        else
            print_error "Homebrew 安装 OpenJDK 21 失败"
            return 1
        fi
    else
        print_error "未找到 Homebrew，请先安装 Homebrew: https://brew.sh"
        print_info "或者手动下载 JDK 21: https://adoptium.net/download/"
        return 1
    fi
}

# 在 Linux (Debian/Ubuntu) 上安装 JDK 21
install_java21_debian() {
    print_step "在 Debian/Ubuntu 上安装 JDK 21..."
    print_info "发行版版本: ${DISTRO_VERSION:-未知}"

    # 根据版本号区分处理
    case "$DISTRO" in
        ubuntu|linuxmint|pop|elementary|zorin)
            # Ubuntu 及其衍生版
            # 获取主版本号
            UBUNTU_MAJOR=$(echo "$DISTRO_VERSION" | cut -d'.' -f1)
            print_info "Ubuntu 主版本号: $UBUNTU_MAJOR"

            if [ "$UBUNTU_MAJOR" -ge 22 ] 2>/dev/null; then
                # Ubuntu 22.04+ / 24.04+ 直接使用 apt
                print_info "Ubuntu $DISTRO_VERSION (>=22.04)，使用 apt 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq openjdk-21-jdk
            elif [ "$UBUNTU_MAJOR" -ge 20 ] 2>/dev/null; then
                # Ubuntu 20.04
                print_info "Ubuntu $DISTRO_VERSION (20.04)，使用 apt 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq openjdk-21-jdk
            elif [ "$UBUNTU_MAJOR" -ge 18 ] 2>/dev/null; then
                # Ubuntu 18.04 — 需要添加 PPA 或使用 openjdk-21 可能不在官方源
                print_warning "Ubuntu 18.04 官方源可能不包含 OpenJDK 21，尝试添加 ppa:openjdk-r/ppa..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq software-properties-common 2>/dev/null || true
                sudo add-apt-repository -y ppa:openjdk-r/ppa 2>/dev/null && {
                    sudo apt-get update -qq
                    sudo apt-get install -y -qq openjdk-21-jdk
                } || {
                    print_warning "PPA 添加失败，尝试直接安装..."
                    sudo apt-get install -y -qq openjdk-21-jdk 2>/dev/null || {
                        print_warning "直接安装失败，回退到 SDKMAN..."
                        install_java21_sdkman
                        return $?
                    }
                }
            else
                # 更老版本，直接走 SDKMAN
                print_warning "Ubuntu 版本 $DISTRO_VERSION 过旧，使用 SDKMAN 安装 JDK 21..."
                install_java21_sdkman
                return $?
            fi
            ;;

        debian)
            DEBIAN_MAJOR=$(echo "$DISTRO_VERSION" | cut -d'.' -f1)
            print_info "Debian 主版本号: $DEBIAN_MAJOR"

            if [ "$DEBIAN_MAJOR" -ge 12 ] 2>/dev/null; then
                # Debian 12 (Bookworm) 及更新
                print_info "Debian $DISTRO_VERSION (>=12)，使用 apt 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq openjdk-21-jdk
            elif [ "$DEBIAN_MAJOR" -ge 11 ] 2>/dev/null; then
                # Debian 11 (Bullseye)
                print_info "Debian $DISTRO_VERSION (11)，使用 apt 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq openjdk-21-jdk
            elif [ "$DEBIAN_MAJOR" -ge 10 ] 2>/dev/null; then
                # Debian 10 (Buster) — 可能需要 backports
                print_warning "Debian 10 (Buster)，尝试从 backports 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq -t buster-backports openjdk-21-jdk 2>/dev/null || {
                    print_warning "backports 不可用，使用 SDKMAN 安装..."
                    install_java21_sdkman
                    return $?
                }
            else
                print_warning "Debian 版本 $DISTRO_VERSION 过旧，使用 SDKMAN 安装..."
                install_java21_sdkman
                return $?
            fi
            ;;

        *)
            # 其他 debian 系发行版，通用处理
            print_info "Debian 系发行版，使用 apt 安装..."
            sudo apt-get update -qq
            sudo apt-get install -y -qq openjdk-21-jdk
            ;;
    esac

    if [ $? -eq 0 ]; then
        print_success "OpenJDK 21 安装完成"
        
        # 设置默认 Java
        if command -v update-alternatives >/dev/null 2>&1; then
            print_info "设置 OpenJDK 21 为默认 Java..."
            sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java 2>/dev/null || true
            sudo update-alternatives --set javac /usr/lib/jvm/java-21-openjdk-amd64/bin/javac 2>/dev/null || true
        fi
        
        JAVA21_HOME=$(get_java21_home)
        if [ -n "$JAVA21_HOME" ]; then
            print_info "Java 21 安装路径: $JAVA21_HOME"
            # 自动设置 JAVA_HOME 环境变量
            export JAVA_HOME="$JAVA21_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_info "已自动设置 JAVA_HOME=$JAVA_HOME"
        fi
        return 0
    else
        print_error "OpenJDK 21 安装失败，尝试 SDKMAN..."
        install_java21_sdkman
        return $?
    fi
}

# 在 Linux (RHEL/CentOS/Fedora) 上安装 JDK 21
install_java21_rhel() {
    print_step "在 RHEL/CentOS/Fedora 上安装 JDK 21..."
    print_info "发行版版本: ${DISTRO_VERSION:-未知}"

    RHEL_MAJOR=$(echo "$DISTRO_VERSION" | cut -d'.' -f1)

    case "$DISTRO" in
        fedora)
            # Fedora 各版本都支持
            print_info "Fedora $DISTRO_VERSION，使用 dnf 安装..."
            sudo dnf install -y java-21-openjdk java-21-openjdk-devel
            ;;
        rhel|centos|rocky|almalinux|ol)
            if [ "$RHEL_MAJOR" -ge 9 ] 2>/dev/null; then
                # RHEL 9+ / Rocky 9+ / Alma 9+
                print_info "RHEL $DISTRO_VERSION (>=9)，使用 dnf 安装..."
                sudo dnf install -y java-21-openjdk java-21-openjdk-devel
            elif [ "$RHEL_MAJOR" -ge 8 ] 2>/dev/null; then
                # RHEL 8 / CentOS 8 / Rocky 8 / Alma 8
                print_info "RHEL $DISTRO_VERSION (8.x)，使用 dnf 安装..."
                sudo dnf install -y java-21-openjdk java-21-openjdk-devel
            elif [ "$RHEL_MAJOR" -ge 7 ] 2>/dev/null; then
                # CentOS 7 / RHEL 7 — java-21 可能不在默认源
                print_warning "RHEL/CentOS 7 默认源可能不包含 JDK 21，尝试安装..."
                if command -v yum >/dev/null 2>&1; then
                    sudo yum install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null || {
                        print_warning "yum 安装失败，使用 SDKMAN..."
                        install_java21_sdkman
                        return $?
                    }
                elif command -v dnf >/dev/null 2>&1; then
                    sudo dnf install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null || {
                        print_warning "dnf 安装失败，使用 SDKMAN..."
                        install_java21_sdkman
                        return $?
                    }
                fi
            else
                print_warning "RHEL 版本 $DISTRO_VERSION 过旧，使用 SDKMAN 安装..."
                install_java21_sdkman
                return $?
            fi
            ;;
        amzn)
            # Amazon Linux
            if [ "$RHEL_MAJOR" -ge 2023 ] 2>/dev/null || [ "$RHEL_MAJOR" -ge 2 ] 2>/dev/null; then
                # Amazon Linux 2023 / AL2
                print_info "Amazon Linux $DISTRO_VERSION，使用 dnf/yum 安装..."
                if command -v dnf >/dev/null 2>&1; then
                    sudo dnf install -y java-21-amazon-corretto-devel
                else
                    sudo yum install -y java-21-amazon-corretto-devel
                fi
            else
                print_warning "Amazon Linux 版本未知，尝试安装 Corretto 17..."
                if command -v dnf >/dev/null 2>&1; then
                    sudo dnf install -y java-21-amazon-corretto-devel 2>/dev/null || {
                        sudo dnf install -y java-21-openjdk java-21-openjdk-devel
                    }
                else
                    sudo yum install -y java-21-amazon-corretto-devel 2>/dev/null || {
                        sudo yum install -y java-21-openjdk java-21-openjdk-devel
                    }
                fi
            fi
            ;;
        *)
            # 其他 RHEL 系发行版，通用处理
            if command -v dnf >/dev/null 2>&1; then
                print_info "使用 dnf 安装 OpenJDK 21..."
                sudo dnf install -y java-21-openjdk java-21-openjdk-devel
            elif command -v yum >/dev/null 2>&1; then
                print_info "使用 yum 安装 OpenJDK 21..."
                sudo yum install -y java-21-openjdk java-21-openjdk-devel
            else
                print_error "未找到 dnf 或 yum 包管理器"
                install_java21_sdkman
                return $?
            fi
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        print_success "OpenJDK 21 安装完成"
        
        # 设置默认 Java
        if command -v alternatives >/dev/null 2>&1; then
            print_info "设置 OpenJDK 21 为默认 Java..."
            JAVA21_PATH=$(find /usr/lib/jvm -name "java-21*" -type d 2>/dev/null | head -n 1)
            if [ -n "$JAVA21_PATH" ]; then
                sudo alternatives --set java "$JAVA21_PATH/bin/java" 2>/dev/null || true
                sudo alternatives --set javac "$JAVA21_PATH/bin/javac" 2>/dev/null || true
            fi
        fi
        
        JAVA21_HOME=$(get_java21_home)
        if [ -n "$JAVA21_HOME" ]; then
            print_info "Java 21 安装路径: $JAVA21_HOME"
            export JAVA_HOME="$JAVA21_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_info "已自动设置 JAVA_HOME=$JAVA_HOME"
        fi
        return 0
    else
        print_error "OpenJDK 21 安装失败，尝试 SDKMAN..."
        install_java21_sdkman
        return $?
    fi
}

# 在 Linux (openSUSE) 上安装 JDK 21
install_java21_suse() {
    print_step "在 openSUSE 上安装 JDK 21..."
    
    if command -v zypper >/dev/null 2>&1; then
        print_info "使用 zypper 安装 OpenJDK 21..."
        sudo zypper install -y java-21-openjdk java-21-openjdk-devel
    else
        print_error "未找到 zypper 包管理器"
        return 1
    fi
    
    if [ $? -eq 0 ]; then
        print_success "OpenJDK 21 安装完成"
        
        JAVA21_HOME=$(get_java21_home)
        if [ -n "$JAVA21_HOME" ]; then
            print_info "Java 21 安装路径: $JAVA21_HOME"
            export JAVA_HOME="$JAVA21_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_info "已自动设置 JAVA_HOME=$JAVA_HOME"
        fi
        return 0
    else
        print_error "OpenJDK 21 安装失败"
        return 1
    fi
}

# 在 Linux (Alpine) 上安装 JDK 21
install_java21_alpine() {
    print_step "在 Alpine Linux 上安装 JDK 21..."
    
    if command -v apk >/dev/null 2>&1; then
        print_info "使用 apk 安装 OpenJDK 21..."
        sudo apk add openjdk21
    else
        print_error "未找到 apk 包管理器"
        return 1
    fi
    
    if [ $? -eq 0 ]; then
        print_success "OpenJDK 21 安装完成"
        
        JAVA21_HOME=$(get_java21_home)
        if [ -n "$JAVA21_HOME" ]; then
            print_info "Java 21 安装路径: $JAVA21_HOME"
            export JAVA_HOME="$JAVA21_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_info "已自动设置 JAVA_HOME=$JAVA_HOME"
        fi
        return 0
    else
        print_error "OpenJDK 21 安装失败"
        return 1
    fi
}

# 在 Linux (Arch) 上安装 JDK 21
install_java21_arch() {
    print_step "在 Arch Linux 上安装 JDK 21..."
    
    if command -v pacman >/dev/null 2>&1; then
        print_info "使用 pacman 安装 JDK 21..."
        sudo pacman -S --noconfirm jdk21-openjdk
    else
        print_error "未找到 pacman 包管理器"
        return 1
    fi
    
    if [ $? -eq 0 ]; then
        print_success "JDK 21 安装完成"
        
        JAVA21_HOME=$(get_java21_home)
        if [ -n "$JAVA21_HOME" ]; then
            print_info "Java 21 安装路径: $JAVA21_HOME"
            export JAVA_HOME="$JAVA21_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            print_info "已自动设置 JAVA_HOME=$JAVA_HOME"
        fi
        return 0
    else
        print_error "JDK 21 安装失败"
        return 1
    fi
}

# 通用 Linux 安装（使用 SDKMAN）
install_java21_sdkman() {
    print_step "使用 SDKMAN 安装 JDK 21..."
    
    # 检查是否已安装 SDKMAN
    if [ -d "$HOME/.sdkman" ]; then
        print_info "SDKMAN 已安装"
    else
        print_info "安装 SDKMAN..."
        curl -s "https://get.sdkman.io" | bash
        if [ $? -ne 0 ]; then
            print_error "SDKMAN 安装失败"
            return 1
        fi
        # 初始化 SDKMAN
        source "$HOME/.sdkman/bin/sdkman-init.sh"
    fi
    
    # 确保 SDKMAN 已初始化
    if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        source "$HOME/.sdkman/bin/sdkman-init.sh"
    fi
    
    if command -v sdk >/dev/null 2>&1; then
        print_info "使用 SDKMAN 安装 Temurin JDK 21..."
        sdk install java 21.0.5-tem
        
        if [ $? -eq 0 ]; then
            print_success "Temurin JDK 21 安装完成"
            
            JAVA21_HOME=$(get_java21_home)
            if [ -n "$JAVA21_HOME" ]; then
                print_info "Java 21 安装路径: $JAVA21_HOME"
            fi
            return 0
        fi
    fi
    
    print_error "SDKMAN 安装 JDK 21 失败"
    return 1
}

# 在 Linux 上自动选择安装方式
install_java21_linux() {
    DISTRO=$(detect_linux_distro)
    DISTRO_LIKE="${DISTRO_LIKE:-$DISTRO}"
    
    print_info "检测到 Linux 发行版: $DISTRO"
    
    # 根据发行版选择安装方式
    case "$DISTRO" in
        ubuntu|debian|linuxmint|pop|elementary|zorin)
            install_java21_debian
            return $?
            ;;
        rhel|centos|fedora|rocky|almalinux|ol|amzn)
            install_java21_rhel
            return $?
            ;;
        suse|opensuse*|sled|sles)
            install_java21_suse
            return $?
            ;;
        alpine)
            install_java21_alpine
            return $?
            ;;
        arch|manjaro|endeavouros)
            install_java21_arch
            return $?
            ;;
        *)
            # 尝试通过 DISTRO_LIKE 判断
            case "$DISTRO_LIKE" in
                *debian*|*ubuntu*)
                    install_java21_debian
                    return $?
                    ;;
                *rhel*|*fedora*|*centos*)
                    install_java21_rhel
                    return $?
                    ;;
                *suse*)
                    install_java21_suse
                    return $?
                    ;;
                *arch*)
                    install_java21_arch
                    return $?
                    ;;
            esac
            
            # 兜底方案：尝试使用 SDKMAN
            print_warning "未识别的 Linux 发行版: $DISTRO"
            print_info "尝试使用 SDKMAN 安装 JDK 21..."
            install_java21_sdkman
            return $?
            ;;
    esac
}

# 验证 Java 安装
verify_java() {
    print_step "验证 Java 安装..."
    
    JAVA21_HOME=$(get_java21_home)
    if [ -z "$JAVA21_HOME" ]; then
        print_error "无法找到 Java 21 安装路径"
        return 1
    fi
    
    if [ -x "$JAVA21_HOME/bin/java" ]; then
        JAVA_VERSION_OUTPUT=$("$JAVA21_HOME/bin/java" -version 2>&1)
        print_success "Java 安装验证通过"
        print_info "$JAVA_VERSION_OUTPUT"
        
        # 检查 javac 是否可用（确认是 JDK 而非 JRE）
        if [ -x "$JAVA21_HOME/bin/javac" ]; then
            JAVAC_VERSION=$("$JAVA21_HOME/bin/javac" -version 2>&1)
            print_info "javac: $JAVAC_VERSION"
            print_success "确认安装的是 JDK（包含编译工具）"
        else
            print_warning "未找到 javac，可能只安装了 JRE 而非 JDK"
            print_warning "项目需要 JDK 来编译代码，请确保安装了完整的 JDK"
        fi
        
        return 0
    else
        print_error "Java 安装验证失败"
        return 1
    fi
}

# 打印使用说明
print_usage_guide() {
    JAVA21_HOME=$(get_java21_home)
    
    echo ""
    print_info "=========================================="
    print_success "环境安装完成！"
    print_info "=========================================="
    echo ""
    
    if [ -n "$JAVA21_HOME" ]; then
        print_info "Java 21 安装路径: $JAVA21_HOME"
        echo ""
        
        if [ "$PLATFORM" = "Mac" ]; then
            print_warning "请将以下内容添加到 shell 配置文件："
            echo ""
            echo "  export JAVA_HOME=$JAVA21_HOME"
            echo "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
            echo ""
            print_info "如果使用 zsh，执行: echo 'export JAVA_HOME=$JAVA21_HOME' >> ~/.zshrc"
            print_info "如果使用 bash，执行: echo 'export JAVA_HOME=$JAVA21_HOME' >> ~/.bash_profile"
            echo ""
            print_info "然后执行: source ~/.zshrc  或  source ~/.bash_profile"
        else
            print_info "JAVA_HOME 已在当前会话中设置"
            print_warning "如需永久生效，请将以下内容添加到 ~/.bashrc 或 ~/.zshrc："
            echo ""
            echo "  export JAVA_HOME=$JAVA21_HOME"
            echo "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
            echo ""
        fi
    fi
    
    print_info "下一步操作："
    print_info "  1. 执行 './build-all-in-one.sh' 构建项目"
    print_info "  2. 执行 './run.sh' 启动服务"
    echo ""
}

# ==========================================
# 主流程
# ==========================================

echo ""

# 0. 确保 sudo 可用（Linux 下缺失时自动安装）
if [ "$PLATFORM" = "Linux" ]; then
    # 提前检测发行版信息（用于 ensure_sudo 和后续安装）
    detect_linux_distro > /dev/null
    print_info "检测到 Linux 发行版: $DISTRO (版本: ${DISTRO_VERSION:-未知})"
    echo ""
    ensure_sudo || {
        print_error "sudo 安装失败，无法继续"
        exit 1
    }
    echo ""
fi

# 1. 检查是否已安装 Java 21
if check_java21_installed; then
    echo ""
    print_success "Java 21 环境已就绪，无需安装"
    JAVA21_HOME=$(get_java21_home)
    if [ -n "$JAVA21_HOME" ]; then
        export JAVA_HOME="$JAVA21_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
        print_info "JAVA_HOME 已设置为: $JAVA_HOME"
    fi
    print_info "可直接运行 ./build-all-in-one.sh 构建项目"
    exit 0
fi

# 2. 确认需要安装
echo ""
print_info "即将安装 JDK 21，需要管理员权限"
echo ""
read -p "是否继续安装？[Y/n] " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]] && [ -n "$REPLY" ]; then
    print_info "安装已取消"
    exit 0
fi

# 3. 根据操作系统安装
echo ""
case "${PLATFORM}" in
    Mac)
        install_java21_mac
        INSTALL_RESULT=$?
        ;;
    Linux)
        install_java21_linux
        INSTALL_RESULT=$?
        ;;
    Windows)
        print_error "Windows 系统暂不支持自动安装"
        print_info "请手动下载安装 JDK 21: https://adoptium.net/download/"
        print_info "或者使用 winget: winget install EclipseAdoptium.Temurin.17.JDK"
        exit 1
        ;;
    *)
        print_error "不支持的操作系统: $PLATFORM"
        print_info "请手动安装 JDK 21: https://adoptium.net/download/"
        exit 1
        ;;
esac

# 4. 验证安装
echo ""
if [ $INSTALL_RESULT -eq 0 ]; then
    verify_java
    if [ $? -eq 0 ]; then
        print_usage_guide
        exit 0
    else
        print_error "Java 安装后验证失败"
        print_info "请尝试手动安装 JDK 21: https://adoptium.net/download/"
        exit 1
    fi
else
    print_error "JDK 21 安装失败"
    echo ""
    print_info "替代安装方案："
    print_info "  1. 手动下载: https://adoptium.net/download/"
    print_info "  2. 使用 SDKMAN: curl -s \"https://get.sdkman.io\" | bash && sdk install java 21.0.5-tem"
    print_info "  3. 使用 Docker: docker pull eclipse-temurin:21-jdk"
    exit 1
fi
