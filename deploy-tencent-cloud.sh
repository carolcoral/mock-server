#!/bin/bash

#=======================================================================
# Mock Server 腾讯云 CNB 部署脚本
# 支持一键部署到腾讯云云原生环境
# 作者: carolcoral
#=======================================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[信息]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_step() {
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN} 步骤 $1: $2${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# 显示欢迎信息
show_welcome() {
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                          ║${NC}"
    echo -e "${GREEN}║   Mock Server 腾讯云 CNB 部署工具       ║${NC}"
    echo -e "${GREEN}║                                          ║${NC}"
    echo -e "${GREEN}║   作者: carolcoral                       ║${NC}"
    echo -e "${GREEN}║   GitHub: github.com/carolcoral          ║${NC}"
    echo -e "${GREEN}║                                          ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
    echo ""
}

# 检查依赖
check_dependencies() {
    print_step "1" "检查依赖环境"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        print_error "未找到 Node.js，请先安装 Node.js 18+"
        exit 1
    fi
    
    # 检查 npm
    if ! command -v npm &> /dev/null; then
        print_error "未找到 npm，请先安装 npm 9+"
        exit 1
    fi
    
    # 检查 CloudBase CLI
    if ! command -v cloudbase &> /dev/null; then
        print_info "安装 CloudBase CLI..."
        npm install -g @cloudbase/cli
    fi
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        print_error "未找到 Maven，请先安装 Maven 3.6+"
        exit 1
    fi
    
    # 检查 JDK
    if ! command -v java &> /dev/null; then
        print_error "未找到 Java，请先安装 JDK 17+"
        exit 1
    fi
    
    print_success "依赖检查通过"
}

# 配置环境变量
configure_env() {
    print_step "2" "配置环境变量"
    
    print_info "请配置以下环境变量（按回车使用默认值）："
    echo ""
    
    # JWT 密钥
    read -p "JWT密钥（建议随机生成32位以上）: " JWT_SECRET
    JWT_SECRET=${JWT_SECRET:-"mock-server-secret-key-2024-$(openssl rand -hex 16)"}
    
    # 管理员账号
    read -p "管理员用户名 [admin]: " ADMIN_USERNAME
    ADMIN_USERNAME=${ADMIN_USERNAME:-"admin"}
    
    read -p "管理员密码 [Admin@123]: " ADMIN_PASSWORD
    ADMIN_PASSWORD=${ADMIN_PASSWORD:-"Admin@123"}
    
    read -p "管理员邮箱 [admin@mockserver.com]: " ADMIN_EMAIL
    ADMIN_EMAIL=${ADMIN_EMAIL:-"admin@mockserver.com"}
    
    # 数据库配置
    print_warning "建议使用腾讯云 MySQL，开发环境可使用 SQLite"
    read -p "使用 MySQL 数据库? [y/N]: " use_mysql
    
    if [[ "$use_mysql" =~ ^[Yy]$ ]]; then
        read -p "MySQL 主机: " MYSQL_HOST
        read -p "MySQL 端口 [3306]: " MYSQL_PORT
        MYSQL_PORT=${MYSQL_PORT:-"3306"}
        
        read -p "MySQL 数据库名 [mock_server]: " MYSQL_DATABASE
        MYSQL_DATABASE=${MYSQL_DATABASE:-"mock_server"}
        
        read -p "MySQL 用户名: " MYSQL_USERNAME
        read -p "MySQL 密码: " MYSQL_PASSWORD
        
        USE_MYSQL=true
    else
        USE_MYSQL=false
        print_info "将使用 SQLite 数据库"
    fi
    
    # CORS 配置
    read -p "CORS 允许的域名（多个用逗号分隔，*表示全部） [*]: " CORS_ALLOWED_ORIGINS
    CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS:-"*"}
    
    print_success "环境变量配置完成"
}

# 构建项目
build_project() {
    print_step "3" "构建项目"
    
    # 构建后端
    print_info "构建后端..."
    cd backend
    mvn clean package -DskipTests -Dmaven.test.skip=true
    
    if [ ! -f "target/mock-server-1.0.0.jar" ]; then
        print_error "后端构建失败，未找到 JAR 文件"
        exit 1
    fi
    
    print_success "后端构建完成"
    cd ..
    
    # 构建前端
    print_info "构建前端..."
    cd frontend
    
    if [ ! -d "node_modules" ]; then
        print_info "安装前端依赖..."
        npm install --registry=https://registry.npmmirror.com
    fi
    
    npm run build
    
    if [ ! -d "dist" ]; then
        print_error "前端构建失败，未找到 dist 目录"
        exit 1
    fi
    
    print_success "前端构建完成"
    cd ..
}

# 登录腾讯云
login_tencent_cloud() {
    print_step "4" "登录腾讯云"
    
    print_info "正在启动 CloudBase CLI 登录..."
    cloudbase login
    
    if [ $? -ne 0 ]; then
        print_error "登录失败"
        exit 1
    fi
    
    print_success "登录成功"
}

# 选择环境
select_environment() {
    print_step "5" "选择云开发环境"
    
    print_info "获取环境列表..."
    cloudbase env:list
    
    echo ""
    read -p "请输入环境ID（或回车创建新环境）: " ENV_ID
    
    if [ -z "$ENV_ID" ]; then
        print_info "创建新环境..."
        
        read -p "环境名称: " ENV_NAME
        read -p "选择区域 [ap-guangzhou]: " REGION
        REGION=${REGION:-"ap-guangzhou"}
        
        cloudbase env:create --name "$ENV_NAME" --region "$REGION"
        
        if [ $? -ne 0 ]; then
            print_error "环境创建失败"
            exit 1
        fi
        
        # 获取新创建的环境ID
        ENV_ID=$(cloudbase env:list --json | grep -o '"EnvId":"[^"]*' | head -1 | cut -d'"' -f4)
    fi
    
    export ENV_ID
    print_success "环境选择完成: $ENV_ID"
}

# 配置环境变量
setup_environment_variables() {
    print_step "6" "配置环境变量"
    
    print_info "设置环境变量..."
    
    # JWT 配置
    cloudbase env:set JWT_SECRET="$JWT_SECRET" --envId "$ENV_ID"
    cloudbase env:set JWT_EXPIRATION="86400000" --envId "$ENV_ID"
    
    # 管理员配置
    cloudbase env:set ADMIN_USERNAME="$ADMIN_USERNAME" --envId "$ENV_ID"
    cloudbase env:set ADMIN_PASSWORD="$ADMIN_PASSWORD" --envId "$ENV_ID"
    cloudbase env:set ADMIN_EMAIL="$ADMIN_EMAIL" --envId "$ENV_ID"
    
    # 应用配置
    cloudbase env:set SPRING_PROFILES_ACTIVE="cloud" --envId "$ENV_ID"
    cloudbase env:set ENABLE_REQUEST_LOG="true" --envId "$ENV_ID"
    
    # CORS 配置
    cloudbase env:set CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" --envId "$ENV_ID"
    
    # 数据库配置
    if [ "$USE_MYSQL" = true ]; then
        cloudbase env:set SPRING_DATASOURCE_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4" --envId "$ENV_ID"
        cloudbase env:set MYSQL_USERNAME="$MYSQL_USERNAME" --envId "$ENV_ID"
        cloudbase env:set MYSQL_PASSWORD="$MYSQL_PASSWORD" --envId "$ENV_ID"
        
        print_warning "请确保 MySQL 实例已创建并运行"
    else
        print_info "使用 SQLite 数据库"
    fi
    
    # JVM 配置
    cloudbase env:set JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -Dfile.encoding=UTF-8" --envId "$ENV_ID"
    
    # 时区配置
    cloudbase env:set TZ="Asia/Shanghai" --envId "$ENV_ID"
    
    print_success "环境变量配置完成"
}

# 部署应用
deploy_application() {
    print_step "7" "部署应用"
    
    print_info "开始部署..."
    cloudbase framework:deploy --envId "$ENV_ID"
    
    if [ $? -ne 0 ]; then
        print_error "部署失败"
        exit 1
    fi
    
    print_success "部署完成"
}

# 验证部署
verify_deployment() {
    print_step "8" "验证部署"
    
    print_info "获取应用访问地址..."
    
    # 获取访问地址
    APP_URL=$(cloudbase app:list --envId "$ENV_ID" --json | grep -o '"DefaultDomain":"[^"]*' | head -1 | cut -d'"' -f4)
    
    if [ -z "$APP_URL" ]; then
        print_warning "无法获取应用地址，请手动查看"
    else
        print_success "应用已部署到: $APP_URL"
        
        echo ""
        print_info "等待应用启动（30秒）..."
        sleep 30
        
        # 健康检查
        print_info "检查应用健康状态..."
        if curl -f -s "$APP_URL/api/v3/api-docs" > /dev/null; then
            print_success "应用运行正常！"
        else
            print_warning "应用可能还在启动中，请稍后手动检查"
        fi
        
        echo ""
        print_success "部署成功！"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "  访问地址:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "  前端应用: $APP_URL"
        echo "  API文档: $APP_URL/api/swagger-ui.html"
        echo "  后端API: $APP_URL/api"
        echo ""
        echo "  Swagger认证:"
        echo "    用户名: $ADMIN_USERNAME"
        echo "    密码: $ADMIN_PASSWORD"
        echo ""
        echo "  默认登录账号:"
        echo "    用户名: $ADMIN_USERNAME"
        echo "    密码: $ADMIN_PASSWORD"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    fi
}

# 显示部署后操作
show_post_deploy_instructions() {
    print_step "9" "部署后操作"
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  后续操作"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. 访问应用并登录"
    echo "2. 创建项目并配置接口"
    echo "3. 测试 Mock 接口"
    echo "4. 配置自定义域名（推荐）"
    echo "5. 配置监控告警"
    echo ""
    echo "查看日志:"
    echo "  cloudbase app:logs mock-server -f --envId $ENV_ID"
    echo ""
    echo "查看监控:"
    echo "  登录腾讯云控制台 > 云开发 > 应用监控"
    echo ""
    echo "更新应用:"
    echo "  ./deploy-tencent-cloud.sh --update"
    echo ""
    echo "回滚版本:"
    echo "  cloudbase app:rollback mock-server --version v1.0.0"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    print_warning "重要提示："
    echo "1. 生产环境请修改默认密码"
    echo "2. 建议使用 MySQL 数据库"
    echo "3. 配置自定义域名和 HTTPS"
    echo "4. 定期备份数据"
    echo ""
}

# 主函数
main() {
    show_welcome
    
    # 检查参数
    case "${1:-}" in
        --help|-h)
            echo "使用方法:"
            echo "  $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --help, -h        显示帮助信息"
            echo "  --update, -u      更新部署（跳过构建）"
            echo "  --build-only, -b  仅构建，不部署"
            echo "  --deploy-only, -d 仅部署，不构建"
            echo "  --skip-tests      跳过测试"
            echo ""
            exit 0
            ;;
        --update|-u)
            UPDATE_MODE=true
            ;;
        --build-only|-b)
            BUILD_ONLY=true
            ;;
        --deploy-only|-d)
            DEPLOY_ONLY=true
            ;;
    esac
    
    # 部署模式
    if [ "$DEPLOY_ONLY" = true ]; then
        login_tencent_cloud
        select_environment
        setup_environment_variables
        deploy_application
        verify_deployment
        show_post_deploy_instructions
        exit 0
    fi
    
    # 构建模式
    if [ "$BUILD_ONLY" = true ]; then
        check_dependencies
        configure_env
        build_project
        exit 0
    fi
    
    # 更新模式
    if [ "$UPDATE_MODE" = true ]; then
        print_info "更新模式：跳过初始配置"
        build_project
        login_tencent_cloud
        select_environment
        deploy_application
        verify_deployment
        show_post_deploy_instructions
        exit 0
    fi
    
    # 完整部署流程
    check_dependencies
    configure_env
    build_project
    login_tencent_cloud
    select_environment
    setup_environment_variables
    deploy_application
    verify_deployment
    show_post_deploy_instructions
    
    print_success "所有步骤完成！"
}

# 捕获错误
trap 'print_error "部署过程中发生错误"; exit 1' ERR

# 执行主函数
main "$@"
