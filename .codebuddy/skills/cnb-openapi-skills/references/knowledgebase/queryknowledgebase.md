# QueryKnowledgeBase

## 原始 Swagger
https://api.cnb.cool/#/operations/QueryKnowledgeBase

## 接口描述
查询知识库 [POST 接口已废弃，请使用 GET 替代]
## 接口权限
repo-code:r
## 请求信息

**请求方法：** POST

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/knowledge/base/query

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | repo|

### 请求体参数


**请求体结构：**

```json
{
  "metadata_filtering_conditions": {
    "conditions": [{
      "comparison_operator": "string", // 运算符: "is", "is not", "contains", "not contains", "starts with", "ends with", "is empty", "is not empty"
      "name": "string", // 字段名称: "position", "path", "type"
      "value": "string" // 比较值（"is empty" 和 "is not empty" 时忽略此字段）
    }],
    "logical_operator": "string" // "and" 或 "or"，默认 "and"
  }, // 元数据过滤条件
  "query": "string", // 查询语句
  "score_threshold": 0.0, // 分数阈值
  "top_k": 0 // 返回结果的数量
}
```
## 响应信息


**响应类型：** 数组[dto.QueryKnowledgeBaseRes]

**响应结构（数组元素）：**
```json
[
{
    "chunk": "string",
    "metadata": {},
    "score": 0.0
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X POST \
  "${CNB_API_ENDPOINT}/{repo}/-/knowledge/base/query" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json" \
  -d '{
  "metadata_filtering_conditions": {
    "conditions": [{
      "comparison_operator": "string",
      "name": "string",
      "value": "string"
    }],
    "logical_operator": "string"
  },
  "query": "string",
  "score_threshold": 0.0,
  "top_k": 0
}'
```


