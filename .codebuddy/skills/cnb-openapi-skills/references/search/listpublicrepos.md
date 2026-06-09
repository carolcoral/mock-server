# ListPublicRepos

## 原始 Swagger
https://api.cnb.cool/#/operations/ListPublicRepos

## 接口描述
Search resource with the key
## 接口权限
repo-basic-info:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/search/public-repos

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| key | 字符串 | 否| key|
| flags | 字符串 | 否| 仓库类型标记，逗号分隔。Repository type flags, comma separated，枚举值：KnowledgeBase, NPC, Skills|
| flags_match | 字符串 | 否| flags 多值匹配模式。Flags match mode when multiple flags provided，枚举值：intersection, union|
| order_by | 字符串 | 否| 排序类型，默认last_updated_at，枚举值：created_at, last_updated_at, stars, forks|
| desc | 布尔值 | 否| 排序顺序|
| topN | 整数 | 否| 排行前N位，默认10，最大值100|
## 响应信息


**响应类型：** 数组[dto.Repos4UserBase]

**响应结构（数组元素）：**
```json
[
{
    "created_at": "string",
    "description": "string",
    "display_module": {
      "activity": false, // 仓库动态
      "contributors": false, // 仓库贡献者
      "release": false // 仓库版本
    },
    "flags": {
    },
    "fork_count": 0,
    "forked_from_repo": {
      "created_at": "string",
      "freeze": false,
      "path": "string",
      "resource_id": 0,
      "resource_type": {
      },
      "root_freeze": false,
      "root_id": 0,
      "updated_at": "string"
    }, // 预留
    "freeze": false,
    "id": "string",
    "language": "string", // 仓库程序语言，预留
    "languages": {
      "color": "string",
      "language": "string"
    }, // 仓库语言
    "last_update_nickname": "string", // 最新代码更新人姓名
    "last_update_username": "string", // 最新代码更新人账户名
    "last_updated_at": {
      "time": "string",
      "valid": false // Valid is true if Time is not NULL
    }, // 最新代码更新时间
    "license": "string",
    "mark_count": 0,
    "name": "string",
    "npc_builded_count": 0,
    "npc_created_pull_count": 0,
    "npc_merged_pull_count": 0,
    "open_issue_count": 0, // 开启的issue数
    "open_pull_request_count": 0, // 开启的pull request数
    "path": "string", // 完整仓库路径
    "second_languages": {
      "color": "string",
      "language": "string"
    }, // 第二语言
    "site": "string",
    "star_count": 0,
    "status": {
    },
    "tags": ["<unknown>"],
    "topics": "string",
    "updated_at": "string",
    "visibility_level": {
    },
    "web_url": "string"
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/search/public-repos" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "key=<key>" \
-d "flags=<flags>" \
-d "flags_match=<flags_match>" \
-d "order_by=<order_by>" \
-d "desc=<desc>" \
-d "topN=<topN>" \
```


