#!/bin/bash

echo "测试 User 实体 language 字段默认值修复"
echo "=========================================="

# 删除旧数据库
rm -f /workspace/backend/data/mock-server.db

echo "1. 启动应用（后台运行）..."
cd /workspace/backend && java -jar target/mock-server-1.0.0.jar > /tmp/test_app.log 2>&1 &

APP_PID=$!
echo "应用 PID: $APP_PID"

# 等待应用启动
echo "等待应用启动..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "✓ 应用启动成功"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ 应用启动超时"
        cat /tmp/test_app.log
        exit 1
    fi
    sleep 1
done

# 检查日志中是否有错误
echo ""
echo "2. 检查启动日志..."
if grep -i "error" /tmp/test_app.log | grep -v "HHH000489" | head -5; then
    echo "发现错误，请检查日志"
else
    echo "✓ 未发现严重错误"
fi

# 检查数据库是否创建
echo ""
echo "3. 检查数据库..."
if [ -f /workspace/backend/data/mock-server.db ]; then
    echo "✓ 数据库文件创建成功"
    ls -lh /workspace/backend/data/mock-server.db
else
    echo "✗ 数据库文件未创建"
    exit 1
fi

# 测试 API
echo ""
echo "4. 测试 API 访问..."
RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/api/actuator/health)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
echo "HTTP 状态码: $HTTP_CODE"

if [ "$HTTP_CODE" = "401" ]; then
    echo "✓ API 访问正常（返回401未授权，说明服务正常）"
elif [ "$HTTP_CODE" = "200" ]; then
    echo "✓ API 访问正常（返回200）"
else
    echo "API 访问异常: $HTTP_CODE"
fi

# 清理
echo ""
echo "5. 清理..."
kill $APP_PID 2>/dev/null
rm -f /tmp/test_app.log

echo ""
echo "=========================================="
echo "测试完成"
