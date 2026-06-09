# SearchNpc

## 原始 Swagger
https://api.cnb.cool/#/operations/SearchNpc

## 接口描述
全局语义搜索 NPC 角色
## 接口权限
repo-basic-info:r,repo-code:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/search/npc

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| key | 字符串 | 是| 搜索关键词|
| top_n | 整数 | 否| 返回结果数量，最大20|
## 响应信息


**响应类型：** 数组[dto.SearchNpcRes]

**响应结构（数组元素）：**
```json
[
{
    "avatar": "string", // NPC 头像
    "built_count": 0, // NPC 触发的构建数量
    "name": "string", // NPC 名称
    "pull_request_created_count": 0, // NPC 创建的 PR 数量
    "pull_request_merged_count": 0, // NPC 合并的 PR 数量
    "score": 0.0, // 检索匹配分数
    "slogan": "string", // NPC 口号
    "slug": "string" // NPC 所属仓库的全路径
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/search/npc" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "key=<key>" \
-d "top_n=<top_n>" \
```


