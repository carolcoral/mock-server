#!/usr/bin/env bash
# CNB 云开发环境：一键部署服务并运行自动化测试
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
exec bash setup.sh --deploy "$@"
