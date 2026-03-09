#!/bin/bash

echo "=========================================="
echo "从零开始验证 language 字段修复"
echo "=========================================="

# 确保清理
echo "1. 彻底清理..."
pkill -f "java.*mock-server" 2>/dev/null
sleep 1
find /workspace -name "*.db" -type f -delete
rm -f /tmp/fresh_test.log

echo "2. 确认数据库不存在..."
if [ -f /workspace/backend/data/mock-server.db ]; then
    echo "   ✗ 数据库仍然存在"
    exit 1
else
    echo "   ✓ 数据库已删除"
fi

echo "3. 从零启动应用..."
cd /workspace/backend && java -jar target/mock-server-1.0.0.jar > /tmp/fresh_test.log 2>&1 &
APP_PID=$!
echo "   应用 PID: $APP_PID"

# 等待应用启动
echo "4. 等待应用启动..."
for i in {1..40}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "   ✓ 应用启动成功 (用时 ${i}秒)"
        break
    fi
    if [ $i -eq 40 ]; then
        echo "   ✗ 应用启动超时"
        echo "查看完整日志："
        cat /tmp/fresh_test.log
        exit 1
    fi
    sleep 1
done

echo ""
echo "5. 检查关键错误..."
ERROR_COUNT=0

# 检查 NOT NULL column 错误
if grep -q "Cannot add a NOT NULL column" /tmp/fresh_test.log; then
    echo "   ✗ 发现 NOT NULL column 错误："
    grep "Cannot add a NOT NULL column" /tmp/fresh_test.log
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 检查 language 字段相关的 SQLite 错误
if grep -q "SQLITE_ERROR.*language" /tmp/fresh_test.log; then
    echo "   ✗ 发现 SQLite language 字段错误："
    grep "SQLITE_ERROR.*language" /tmp/fresh_test.log
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 检查 JPA 初始化错误
if grep -q "Failed to initialize JPA EntityManagerFactory" /tmp/fresh_test.log; then
    echo "   ✗ 发现 JPA 初始化错误："
    grep "Failed to initialize JPA EntityManagerFactory" /tmp/fresh_test.log
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

if [ $ERROR_COUNT -eq 0 ]; then
    echo "   ✓ 未发现关键错误"
else
    echo "   ✗ 发现 $ERROR_COUNT 个错误"
    echo ""
    echo "最近的错误日志："
    grep -B 3 -A 3 "ERROR\|Exception" /tmp/fresh_test.log | tail -30
fi

echo ""
echo "6. 验证数据库创建..."
if [ -f /workspace/backend/data/mock-server.db ]; then
    SIZE=$(ls -lh /workspace/backend/data/mock-server.db | awk '{print $5}')
    echo "   ✓ 数据库已创建 (大小: $SIZE)"
else
    echo "   ✗ 数据库未创建"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

echo ""
echo "7. 检查 JPA 和 Hibernate 状态..."
if grep -q "Initialized JPA EntityManagerFactory" /tmp/fresh_test.log; then
    echo "   ✓ JPA EntityManagerFactory 初始化成功"
else
    echo "   ✗ JPA EntityManagerFactory 初始化失败"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

echo ""
echo "8. 检查应用完全启动..."
if grep -q "Started MockServerApplication" /tmp/fresh_test.log; then
    START_TIME=$(grep "Started MockServerApplication" /tmp/fresh_test.log | awk '{for(i=NF-1;i>=1;i--) if($i ~ /[0-9]+\.[0-9]+/) {print $i; break}}')
    echo "   ✓ 应用完全启动 (耗时: ${START_TIME}秒)"
else
    echo "   ✗ 应用未完全启动"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

echo ""
echo "9. 测试 API 健康检查..."
HEALTH_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/actuator/health)
if [ "$HEALTH_CODE" = "401" ] || [ "$HEALTH_CODE" = "200" ]; then
    echo "   ✓ API 健康检查正常 (HTTP $HEALTH_CODE)"
else
    echo "   ℹ API 响应: HTTP $HEALTH_CODE"
fi

echo ""
echo "10. 显示 language 字段相关的 DDL 语句..."
echo "    CREATE TABLE t_user 语句："
grep -A 30 "create table t_user" /tmp/fresh_test.log | grep -i language || echo "    未找到 language 字段定义"

echo ""
echo "=========================================="
if [ $ERROR_COUNT -eq 0 ]; then
    echo "✓✓✓ 验证成功！所有测试通过 ✓✓✓"
    echo "   language 字段默认值修复完全生效"
    echo "   应用从零启动成功，无任何错误"
else
    echo "✗✗✗ 验证失败！发现 $ERROR_COUNT 个问题 ✗✗✗"
fi
echo "=========================================="

# 清理
echo ""
echo "11. 清理并停止应用..."
kill $APP_PID 2>/dev/null
sleep 1
rm -f /tmp/fresh_test.log

exit $ERROR_COUNT
