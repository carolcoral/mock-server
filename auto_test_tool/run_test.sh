#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
VENV_PYTHON="${SCRIPT_DIR}/.venv/bin/python"
if [ ! -f "$VENV_PYTHON" ]; then
    echo "错误: 未找到虚拟环境，请先运行 ./setup.sh"
    exit 1
fi
cd "$SCRIPT_DIR"
exec "$VENV_PYTHON" main.py "$@"
