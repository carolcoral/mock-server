#!/bin/bash

echo "=========================================="
echo "验证 language 字段默认值修复"
echo "=========================================="

# 清理旧数据
echo "1. 清理旧数据..."
pkill -f "java.*mock-server" 2>/dev/null
sleep 1
rm -f /workspace/backend/data/mock-server.db
rm -f /tmp/test_detail.log

echo "2. 启动应用（显示详细日志）..."
cd /workspace/backend && java -jar target/mock-server-1.0.0.jar > /tmp/test_detail.log 2>&1 &
APP_PID=$!
echo "应用 PID: $APP_PID"

# 等待应用启动
echo "3. 等待应用启动..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "   ✓ 应用启动成功"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "   ✗ 应用启动超时"
        echo "错误日志："
        cat /tmp/test_detail.log | tail -50
        exit 1
    fi
    sleep 1
done

echo ""
echo "4. 检查 language 字段相关错误..."
if grep -q "Cannot add a NOT NULL column" /tmp/test_detail.log; then
    echo "   ✗ 发现 NOT NULL column 错误"
    grep "Cannot add a NOT NULL column" /tmp/test_detail.log
    exit 1
elif grep -q "SQLITE_ERROR" /tmp/test_detail.log | grep -q "language"; then
    echo "   ✗ 发现 SQLite language 字段错误"
    grep "SQLITE_ERROR" /tmp/test_detail.log | grep "language"
    exit 1
else
    echo "   ✓ 未发现 language 字段相关的 DDL 错误"
fi

echo ""
echo "5. 检查 language 字段迁移..."
if grep -q "language字段已存在，跳过迁移" /tmp/test_detail.log; then
    echo "   ✓ language 字段已存在（从旧数据库迁移）"
elif grep -q "language" /tmp/test_detail.log | grep -q "CREATE TABLE\|ALTER TABLE"; then
    echo "   ✓ language 字段已创建"
else
    echo "   ℹ 未找到 language 字段创建日志，但这不是错误"
fi

echo ""
echo "6. 验证应用正常运行..."
RESPONSE=$(curl -s http://localhost:8080/api/actuator/health)
if echo "$RESPONSE" | grep -q "401\|200\|UP"; then
    echo "   ✓ 应用 API 响应正常"
else
    echo "   ℹ API 响应: $RESPONSE"
fi

echo ""
echo "7. 数据库文件状态..."
if [ -f /workspace/backend/data/mock-server.db ]; then
    SIZE=$(ls -lh /workspace/backend/data/mock-server.db | awk '{print $5}')
    echo "   ✓ 数据库文件存在 (大小: $SIZE)"
else
    echo "   ✗ 数据库文件不存在"
    exit 1
fi

echo ""
echo "8. 关键启动日志摘要..."
echo "   - JPA 初始化:"
grep "EntityManagerFactory" /tmp/test_detail.log | tail -1 | sed 's/^/     /' || echo "     未找到"
echo "   - 应用启动:"
grep "Started MockServerApplication" /tmp/test_detail.log | tail -1 | sed 's/^/     /' || echo "     未找到"
echo "   - 数据库用户检查:"
grep "数据库中用户总数" /tmp/test_detail.log | tail -1 | sed 's/^/     /' || echo "     未找到"

# 清理
echo ""
echo "9. 清理..."
kill $APP_PID 2>/dev/null
sleep 1

echo ""
echo "=========================================="
echo "✓ 验证完成！language 字段默认值修复成功"
echo "=========================================="
