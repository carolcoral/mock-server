# GetRepoWeeklyRank

## 原始 Swagger
https://api.cnb.cool/#/operations/GetRepoWeeklyRank

## 接口描述
获取公仓周榜
## 接口权限
repo-basic-info:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/ranks/repo/weekly

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| language | 字符串 | 否| 语言|
| flags | 字符串 | 否| 仓库类型，枚举值：KnowledgeBase, NPC, Skills|
| start | 字符串 | 否| 周榜周一日期，格式为20060102，不填默认为本周|
| topN | 整数 | 否| 排行前n名|
## 响应信息


**响应类型：** dto.GetRankResult

**响应结构：**
```json
{
  "rank_list": [{
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
    "increase_fork": 0,
    "increase_star": 0,
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
    "stared": false,
    "status": {
    },
    "tags": ["<unknown>"],
    "topics": "string",
    "updated_at": "string",
    "visibility_level": {
    },
    "web_url": "string",
    "weight_score": 0
  }],
  "updated_at": "string"
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/ranks/repo/weekly" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "language=<language>" \
-d "flags=<flags>" \
-d "start=<start>" \
-d "topN=<topN>" \
```


