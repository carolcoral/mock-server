# UpdateOutsideCollaborators

## 原始 Swagger
https://api.cnb.cool/#/operations/UpdateOutsideCollaborators

## 接口描述
更新指定仓库的外部贡献者权限信息。 Update permission information for external contributors in specified repository.
## 接口权限
repo-manage:rw
## 请求信息

**请求方法：** PUT

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/outside-collaborators/{username}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | slug|
| username | 字符串 | 是 | username|

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| role | 字符串 | 是| Role，枚举值：Guest, Reporter, Developer|
## 响应信息


**响应类型：** 无特定格式
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X PUT \
  "${CNB_API_ENDPOINT}/{slug}/-/outside-collaborators/{username}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "role=<role>" \
```


