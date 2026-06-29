#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
VENV_DIR="${SCRIPT_DIR}/.venv"
if [ ! -f "${VENV_DIR}/bin/activate" ]; then
    echo "错误: 未找到虚拟环境，请先运行 ./setup.sh"
    exit 1
fi
source "${VENV_DIR}/bin/activate"
echo "虚拟环境已激活"
echo "运行测试: python main.py --help"
