# ListCodeIssueOpenAPI

## 原始 Swagger
https://api.cnb.cool/#/operations/ListCodeIssueOpenAPI

## 接口描述
获取源码扫描问题列表
## 接口权限
repo-code:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/code/issues

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| issue_rule | 字符串 | 否| 问题规则，用于筛选特定类型的问题。示例值：`critical-risk`, `LIC_CRITICAL`, `VUL_CRITICAL`|
| risk_level | 字符串 | 否| 严重程度，用于筛选特定风险等级的问题。枚举值：`info`, `warning`, `error`, `fatal`, `all`|
| page | 整数 | 否| 分页页码。|
| page_size | 整数 | 否| 分页页大小。|
## 响应信息


**响应类型：** api.CodeIssueListData

**响应结构：**
```json
{
  "list": [{
    "author_email": "string", // 责任人邮箱（git 原始信息）。
    "author_name": "string", // 责任人姓名（git 原始信息）。
    "created_at": "string", // 问题创建时间。
    "display_name": "string", // 规则展示名称。
    "file_path": "string", // 包含问题的文件路径。
    "id": "string", // 问题ID。
    "line_no": 0, // 行号。
    "occur_version": "string", // 引入问题的commit。
    "repo_id": "string", // 仓库ID。
    "revision": "string", // 问题所在的commit。
    "risk_level": "string", // 严重级别。
    "rule": "string", // 问题规则。
    "rule_title": "string", // 规则名称。
    "tool": "string" // 扫描工具。
  }] // 问题列表。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/code/issues" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "issue_rule=<issue_rule>" \
-d "risk_level=<risk_level>" \
-d "page=<page>" \
-d "page_size=<page_size>" \
```


