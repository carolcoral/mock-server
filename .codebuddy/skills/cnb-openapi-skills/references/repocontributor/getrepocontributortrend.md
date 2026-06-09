# GetRepoContributorTrend

## 原始 Swagger
https://api.cnb.cool/#/operations/GetRepoContributorTrend

## 接口描述
查询仓库贡献者前 100 名的详细趋势数据。Query detailed trend data for top 100 contributors of the repository.
## 接口权限
repo-code:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/contributor/trend

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | 仓库标识符。格式：`组织名称/仓库名称`|

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| limit | 整数 | 否| 查询结果数量限制，当limit小于0时，默认为15。格式：[0,100]|
| exclude_external_users | 布尔值 | 否| 是否排除外部用户。示例值：`true`,`false`|
## 响应信息


**响应类型：** api.RepoContribTrend

**响应结构：**
```json
{
  "meta": {
    "gen_branch": "string", // 生成数据的分支名称。
    "gen_hash": "string", // 生成数据的提交哈希。
    "updated_at": "string" // 数据更新时间戳。
  }, // 元数据信息，包含生成分支、哈希和时间戳。
  "repo_data": [{
    "a": 0, // 每周增加的行数。
    "c": 0, // 每周的提交数量。
    "d": 0, // 每周删除的行数。
    "w": 0 // 周的时间戳。
  }], // 仓库级别的周度统计数据。
  "user_total": 0, // 贡献者总数。
  "users_data": [{
    "author": {
      "email": "string", // 用户邮箱。
      "user_name": "string" // 用户名。
    }, // 贡献者信息。
    "commit_count": 0, // 贡献者的总提交数。
    "weeks": [{
      "a": 0, // 每周增加的行数。
      "c": 0, // 每周的提交数量。
      "d": 0, // 每周删除的行数。
      "w": 0 // 周的时间戳。
    }] // 贡献者以周为单位的提交趋势数据。
  }], // 贡献者级别的趋势数据，按提交数排序。
  "week_total": 0, // 统计的周总数。
  "with_line_counts": false // 是否统计增删的行数，默认总提交超过10000的仓库不统计。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/contributor/trend" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "limit=<limit>" \
-d "exclude_external_users=<exclude_external_users>" \
```


