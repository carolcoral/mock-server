# ListPackages

## 原始 Swagger
https://api.cnb.cool/#/operations/ListPackages

## 接口描述
查询制品列表。 List all packages.
## 接口权限
registry-package:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/packages

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | 资源路径。Slug.|

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| type | 字符串 | 是| 制品类型。Type.，枚举值：all, docker, helm, dockermodel, maven, npm, ohpm, pypi, nuget, composer, conan, cargo|
| page | 整数 | 否| 页码。Pagination page number.|
| page_size | 整数 | 否| 页数。Pagination page size.|
| ordering | 字符串 | 否| 顺序类型。Ordering type.，枚举值：pull_count, last_push_at, name_ascend, name_descend|
| name | 字符串 | 否| 关键字。Key word to search package name.|
## 响应信息


**响应类型：** 数组[dto.Package]

**响应结构（数组元素）：**
```json
[
{
    "count": 0,
    "description": "string",
    "is_dir": false,
    "labels": ["string"],
    "last_artifact_name": "string",
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    },
    "name": "string",
    "package": "string",
    "package_type": {
    },
    "pull_count": 0,
    "recent_pull_count": 0
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/packages" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "type=<type>" \
-d "page=<page>" \
-d "page_size=<page_size>" \
-d "ordering=<ordering>" \
-d "name=<name>" \
```


