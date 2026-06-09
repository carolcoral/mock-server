# GetPushLimitSettings

## 原始 Swagger
https://api.cnb.cool/#/operations/GetPushLimitSettings

## 接口描述
查询仓库推送设置。List push limit settings.
## 接口权限
repo-manage:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/settings/push-limit

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


**响应类型：** api.PushLimitSettings

**响应结构：**
```json
{
  "allow_single_push_number": 0, // 允许单次推送最多允许更新分支和标签的个数数量。
  "check_single_push_number": false, // 是否开启单次更新分支和标签的个数限制。
  "commit_must_be_signatured": false, // 强制要求提交必须有签名。
  "only_master_can_push_tag": false, // 是否仅允许负责人和管理员推送或删除标签、创建或删除版本。
  "push_commit_must_be": "string" // 推送提交到仓库，对提交作者和提交人进行检查。可选值：`any`,`registered`,`pusher`
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/settings/push-limit" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


