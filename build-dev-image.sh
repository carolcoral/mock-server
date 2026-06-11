#!/bin/bash
# ========================================
# 构建云原生开发环境镜像
# 将当前 CodeBuddy 数据（skills、plugins、历史记录等）打包到镜像中
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查必要的环境变量
if [ -z "$CNB_DOCKER_REGISTRY" ]; then
    print_error "CNB_DOCKER_REGISTRY 环境变量未设置"
    print_info "请在 CNB 云原生开发环境中运行此脚本"
    exit 1
fi

if [ -z "$CNB_REPO_SLUG_LOWERCASE" ]; then
    print_error "CNB_REPO_SLUG_LOWERCASE 环境变量未设置"
    exit 1
fi

if [ -z "$CNB_TOKEN" ]; then
    print_error "CNB_TOKEN 环境变量未设置"
    exit 1
fi

WORKSPACE_DIR="/workspace"
DOCKER_DIR="${WORKSPACE_DIR}/docker"
CODEBUDDY_HOME="$HOME/.codebuddy"
IMAGE_NAME="${CNB_DOCKER_REGISTRY}/${CNB_REPO_SLUG_LOWERCASE}/dev-environment:latest"

print_info "========================================="
print_info "CNB 云原生开发环境镜像构建"
print_info "========================================="
print_info "镜像名称: ${IMAGE_NAME}"
print_info "CodeBuddy 数据目录: ${CODEBUDDY_HOME}"
print_info "========================================="

# 步骤 1: 准备构建上下文
print_info "步骤 1/4: 准备构建上下文..."
BUILD_DIR="${DOCKER_DIR}/dev-build"
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"

# 复制 .codebuddy 数据
print_info "复制 CodeBuddy 数据..."
if [ -d "${CODEBUDDY_HOME}" ]; then
    cp -r "${CODEBUDDY_HOME}" "${BUILD_DIR}/codebuddy-data"
    print_info "CodeBuddy 数据大小: $(du -sh ${BUILD_DIR}/codebuddy-data | cut -f1)"
else
    print_warn "未找到 ${CODEBUDDY_HOME} 目录，将创建空目录"
    mkdir -p "${BUILD_DIR}/codebuddy-data"
fi

# 生成 Dockerfile
print_info "生成 Dockerfile..."
cat > "${BUILD_DIR}/Dockerfile" << 'DOCKERFILE_EOF'
# ========================================
# CNB 云原生开发环境 Dockerfile
# 包含 CodeBuddy skills、plugins、历史记录等数据
# ========================================

# 基础镜像：使用项目运行镜像
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# 复制 .codebuddy 数据到 root 目录
COPY codebuddy-data/ /root/.codebuddy/

# 确保 .codebuddy 目录权限正确
RUN chmod -R 755 /root/.codebuddy/ 2>/dev/null || true

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

# 暴露端口
EXPOSE 8080 3002 5173
DOCKERFILE_EOF

# 步骤 2: 登录 Docker Registry
print_info "步骤 2/4: 登录 Docker Registry..."
echo "$CNB_TOKEN" | docker login "${CNB_DOCKER_REGISTRY}" --username cnb --password-stdin 2>/dev/null
print_info "登录成功"

# 步骤 3: 构建基础镜像（如果需要）
BASE_IMAGE="${CNB_DOCKER_REGISTRY}/${CNB_REPO_SLUG_LOWERCASE}:latest"
print_info "步骤 3/4: 拉取基础镜像 ${BASE_IMAGE}..."
docker pull "${BASE_IMAGE}" 2>/dev/null || {
    print_warn "基础镜像不存在，先构建基础镜像..."
    cd "${WORKSPACE_DIR}"
    docker build -t "${BASE_IMAGE}" -f ./docker/Dockerfile .
}

# 步骤 4: 构建并推送开发环境镜像
print_info "步骤 4/4: 构建并推送开发环境镜像..."
cd "${BUILD_DIR}"
docker build \
    --build-arg BASE_IMAGE="${BASE_IMAGE}" \
    -t "${IMAGE_NAME}" \
    .

print_info "推送镜像到仓库..."
docker push "${IMAGE_NAME}"

# 清理
print_info "清理构建临时文件..."
rm -rf "${BUILD_DIR}"

print_info "========================================="
print_info "镜像构建并推送成功！"
print_info "镜像: ${IMAGE_NAME}"
print_info "========================================="
print_info ""
print_info "下次打开云原生开发环境时，将自动使用此镜像，"
print_info "CodeBuddy 的 skills、plugins、历史记录等数据将自动恢复。"
print_info ""
print_info "如果需要在更新 CodeBuddy 数据后重建镜像，请再次运行此脚本。"
print_info "========================================="
