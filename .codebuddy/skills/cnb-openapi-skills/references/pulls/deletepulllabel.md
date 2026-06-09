# DeletePullLabel

## 原始 Swagger
https://api.cnb.cool/#/operations/DeletePullLabel

## 接口描述
删除合并请求标签。Remove a label from a pull.
## 接口权限
repo-pr:rw
## 请求信息

**请求方法：** DELETE

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/pulls/{number}/labels/{name}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|
| number | 字符串 | 是 | Pull唯一标识编号。|
| name | 字符串 | 是 | 标签名称。|
## 响应信息


**响应类型：** api.Label

**响应结构：**
```json
{
  "color": "string", // 标签颜色。
  "description": "string", // 标签描述。
  "id": "string", // 标签ID。
  "name": "string" // 标签名称。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X DELETE \
  "${CNB_API_ENDPOINT}/{repo}/-/pulls/{number}/labels/{name}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


