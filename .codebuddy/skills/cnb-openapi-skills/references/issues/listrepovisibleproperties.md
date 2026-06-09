# ListRepoVisibleProperties

## 原始 Swagger
https://api.cnb.cool/#/operations/ListRepoVisibleProperties

## 接口描述
查询仓库可见的自定义属性列表。List repository visible custom properties.
## 接口权限
repo-issue:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/property

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|
## 响应信息


**响应类型：** 数组[api.RepoProperty]

**响应结构（数组元素）：**
```json
[
{
    "key": "string", // 自定义属性键名（唯一）。
    "name": "string", // 自定义属性显示名称。
    "type": "string", // 自定义属性类型。枚举值：`string`,`date`,`number`
    "visible": false // 是否可见。
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/property" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


