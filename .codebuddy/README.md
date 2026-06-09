# CodeBuddy 配置持久化

此目录保存 CodeBuddy 相关配置，用于在新环境中恢复使用。

## 目录结构

```
.codebuddy/
├── expert-history.json     # AI 对话历史记录
├── mcp.json               # MCP 服务器配置
├── plugins/
│   └── known_marketplaces.json
└── skills/
    └── cnb-openapi-skills/ # CNB OpenAPI 技能包
```

## 恢复方法

在新环境中运行：

```bash
# 方法1: 使用脚本自动恢复
./restore-codebuddy.sh

# 方法2: 手动复制
cp -r .codebuddy/* ~/.codebuddy/
```

## 内容说明

- **expert-history.json**: CodeBuddy 对话历史记录
- **mcp.json**: MCP 服务器配置
- **skills/cnb-openapi-skills**: CNB (Cloud Native Build) Open API 交互技能
- **plugins/known_marketplaces.json**: 已知技能市场列表
