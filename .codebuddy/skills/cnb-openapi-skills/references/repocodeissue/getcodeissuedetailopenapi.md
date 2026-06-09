# GetCodeIssueDetailOpenAPI

## 原始 Swagger
https://api.cnb.cool/#/operations/GetCodeIssueDetailOpenAPI

## 接口描述
获取源码扫描问题详情
## 接口权限
repo-code:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/code/issues/{record_id}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|
| record_id | 字符串 | 是 | 源码扫描问题记录ID。|
## 响应信息


**响应类型：** api.CodeIssueDetail

**响应结构：**
```json
{
  "author_email": "string", // 责任人邮箱（git 原始信息）。
  "author_name": "string", // 责任人姓名（git 原始信息）。
  "created_at": "string", // 问题创建时间。
  "description": "string", // 问题描述。
  "display_name": "string", // 规则展示名称。
  "extra_msg": "string", // 额外信息。
  "file_path": "string", // 包含问题的文件路径。
  "id": "string", // 问题ID。
  "ignored_at": "string", // 忽略时间。
  "introduce": "string", // 问题介绍。
  "line_no": 0, // 行号。
  "occur_version": "string", // 引入问题的commit。
  "reopen_at": "string", // 重新开启时间。
  "repo_id": "string", // 仓库ID。
  "revision": "string", // 问题所在的commit。
  "risk_level": "string", // 严重级别。
  "rule": "string", // 问题规则。
  "rule_title": "string", // 规则名称。
  "state": "string", // 问题状态（open/ignored）。
  "tool": "string" // 扫描工具。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/code/issues/{record_id}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


