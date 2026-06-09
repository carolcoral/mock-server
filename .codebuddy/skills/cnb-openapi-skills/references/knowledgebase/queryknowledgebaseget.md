# QueryKnowledgeBaseGet

## 原始 Swagger
https://api.cnb.cool/#/operations/QueryKnowledgeBaseGet

## 接口描述
查询知识库，使用文档：https://docs.cnb.cool/zh/ai/knowledge-base.html
## 接口权限
repo-code:r
## 请求信息

**请求方法：** GET

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

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| query | 字符串 | 是| 查询语句|
| top_k | 整数 | 否| 返回结果数量，默认 5|
| score_threshold | 浮点数 | 否| 分数阈值，默认 0|
| metadata_filtering_conditions | 字符串 | 否| 元数据过滤条件，JSON 字符串|
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
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/knowledge/base/query" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "query=<query>" \
-d "top_k=<top_k>" \
-d "score_threshold=<score_threshold>" \
-d "metadata_filtering_conditions=<metadata_filtering_conditions>" \
```


