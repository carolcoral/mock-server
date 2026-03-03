#!/bin/bash

echo "==================== JWT Token 检查工具 ===================="
echo ""
echo "使用说明："
echo "  1. 从浏览器 localStorage 或登录响应中获取 token"
echo "  2. 运行: ./check-token.sh <your-jwt-token>"
echo "  3. 或使用: ./check-token.sh"
echo "     然后粘贴 token（粘贴后按回车，再按 Ctrl+D）"
echo ""
echo "提示：在浏览器控制台运行: localStorage.getItem('token')"
echo ""
echo "==========================================================="
echo ""

# 获取 token（从参数或标准输入）
if [ $# -eq 1 ]; then
    TOKEN="$1"
else
    echo "请输入 JWT token（粘贴后按回车，然后按 Ctrl+D）:"
    TOKEN=$(cat)
fi

if [ -z "$TOKEN" ]; then
    echo "错误：未提供 token"
    exit 1
fi

echo ""
echo "Token: ${TOKEN:0:50}..."
echo ""

# 检查是否有 jwt 命令行工具
if command -v jwt &> /dev/null; then
    echo "使用 jwt 命令解码:"
    echo "$TOKEN" | jwt decode --
else
    echo "使用在线解码: https://jwt.io"
    echo ""
    echo "或使用 node.js 解码:"
    echo "node -e \"console.log(JSON.stringify(JSON.parse(Buffer.from('$TOKEN'.split('.')[1], 'base64').toString()), null, 2))\""
fi

echo ""
echo "手动解码方法:"
echo "1. 复制 token"
echo "2. 访问 https://jwt.io"
echo "3. 粘贴到 Encoded 区域"
echo "4. 查看 Decoded 中的 Payload"
echo ""
echo "检查以下字段:"
echo "  - exp: 过期时间（Unix 时间戳）"
echo "  - iat: 签发时间（Unix 时间戳）"
echo "  - userId: 用户 ID"
echo "  - role: 用户角色"
echo "  - sub: 用户名"
echo ""
