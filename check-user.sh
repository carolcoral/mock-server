#!/bin/bash

echo "==================== 检查数据库用户 ===================="

# 检查数据库文件是否存在
if [ ! -f "data/mock-server.db" ]; then
    echo "错误：数据库文件不存在: data/mock-server.db"
    exit 1
fi

echo "数据库文件存在，大小: $(du -h data/mock-server.db | cut -f1)"

# 使用 sqlite3 命令行工具检查（如果可用）
if command -v sqlite3 &> /dev/null; then
    echo ""
    echo "使用 sqlite3 查询用户表:"
    sqlite3 data/mock-server.db "SELECT id, username, email, role, enabled FROM user;"
    echo ""
    echo "用户总数:"
    sqlite3 data/mock-server.db "SELECT COUNT(*) FROM user;"
else
    echo ""
    echo "sqlite3 命令不可用，请手动检查数据库或使用以下命令安装:"
    echo "  apt-get install sqlite3  # Ubuntu/Debian"
    echo "  yum install sqlite3      # CentOS/RHEL"
    echo "  brew install sqlite3     # macOS"
    echo ""
    echo "安装后运行: sqlite3 data/mock-server.db \"SELECT id, username, email, role, enabled FROM user;\""
fi

echo "==================== 检查完成 ===================="
