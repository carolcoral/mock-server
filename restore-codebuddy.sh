#!/bin/bash
# CodeBuddy 配置恢复脚本
# 将 .codebuddy 目录内容恢复到 ~/.codebuddy/

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SOURCE_DIR="$SCRIPT_DIR/.codebuddy"
TARGET_DIR="$HOME/.codebuddy"

echo "🔄 正在恢复 CodeBuddy 配置..."
echo "📂 源目录: $SOURCE_DIR"
echo "📂 目标目录: $TARGET_DIR"

# 创建目标目录（如果不存在）
mkdir -p "$TARGET_DIR"

# 复制目录结构
if [ -d "$SOURCE_DIR/skills" ]; then
    cp -r "$SOURCE_DIR/skills" "$TARGET_DIR/"
    echo "✅ 技能包已恢复"
fi

if [ -d "$SOURCE_DIR/plugins" ]; then
    cp -r "$SOURCE_DIR/plugins" "$TARGET_DIR/"
    echo "✅ 插件配置已恢复"
fi

# 复制配置文件
[ -f "$SOURCE_DIR/expert-history.json" ] && cp "$SOURCE_DIR/expert-history.json" "$TARGET_DIR/"
[ -f "$SOURCE_DIR/mcp.json" ] && cp "$SOURCE_DIR/mcp.json" "$TARGET_DIR/"

echo "✅ CodeBuddy 配置恢复完成！"
echo "📋 请重启 CodeBuddy 以加载配置"
