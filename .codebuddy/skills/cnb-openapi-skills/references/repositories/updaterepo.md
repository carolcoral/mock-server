# UpdateRepo

## 原始 Swagger
https://api.cnb.cool/#/operations/UpdateRepo

## 接口描述
更新仓库信息, 可更新的内容为: 仓库简介, 仓库站点, 仓库主题, 开源许可证。updates repository details including description, website URL,topics and license type.
## 接口权限
repo-manage:rw
## 请求信息

**请求方法：** PATCH

**请求地址：** ${CNB_API_ENDPOINT}/{repo}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | repo path|

### 请求体参数


**请求体结构：**

```json
{
  "description": "string",
  "license": "string",
  "site": "string",
  "topics": ["string"]
}
```
## 响应信息


**响应类型：** 无特定格式
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X PATCH \
  "${CNB_API_ENDPOINT}/{repo}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json" \
  -d '{
  "description": "string",
  "license": "string",
  "site": "string",
  "topics": ["string"]
}'
```


