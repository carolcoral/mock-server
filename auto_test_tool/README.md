# 自动化测试工具

Mock Server 系统全功能自动化测试工具，支持页面访问测试、功能测试、RBAC 权限测试、AI 功能测试等。

## 目录结构

```
auto_test_tool/
├── config/
│   └── auto_test.config      # 统一配置文件
├── core/
│   ├── __init__.py
│   ├── config_loader.py      # 配置加载器
│   ├── http_client.py        # HTTP 客户端封装
│   ├── auth_manager.py       # 认证管理器（JWT Token 管理）
│   ├── test_runner.py        # 测试运行器
│   └── report_generator.py   # 报告生成器
├── tests/
│   ├── __init__.py
│   ├── page_access_tests.py  # 页面访问测试
│   ├── page_feature_tests.py # 页面功能测试（CRUD）
│   ├── rbac_tests.py         # RBAC 权限控制测试
│   ├── ai_tests.py           # AI 功能测试
│   └── security_tests.py     # 安全特性测试
├── models/
│   ├── __init__.py
│   └── ai_model_manager.py   # AI 模型管理器（自动切换、告警）
├── utils/
│   ├── __init__.py
│   └── helpers.py            # 工具函数
├── reports/                  # 测试报告输出目录
├── main.py                   # 主入口
├── requirements.txt          # Python 依赖
└── README.md                 # 说明文档
```

## 快速开始

```bash
# 安装依赖
pip install -r requirements.txt

# 确保 Mock Server 已启动
cd /workspace && ./run.sh

# 运行全部测试
python main.py

# 运行指定测试
python main.py --test page_access    # 页面访问测试
python main.py --test page_features  # 页面功能测试
python main.py --test rbac           # RBAC 权限测试
python main.py --test ai             # AI 功能测试
python main.py --test security       # 安全特性测试

# 跳过某些测试
python main.py --skip-ai --skip-rbac

# 仅生成报告（不运行测试）
python main.py --report-only
```
