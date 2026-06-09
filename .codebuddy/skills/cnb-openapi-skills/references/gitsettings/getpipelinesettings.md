# GetPipelineSettings

## 原始 Swagger
https://api.cnb.cool/#/operations/GetPipelineSettings

## 接口描述
查询仓库云原生构建设置。List pipeline settings.
## 接口权限
repo-manage:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/settings/cloud-native-build

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


**响应类型：** api.PipelineSettings

**响应结构：**
```json
{
  "auto_trigger": false, // 是否允许仓库按照.cnb.yml配置自动触发云原生构建。
  "cron_auto_trigger": false, // 是否允许仓库按照.cnb.yml配置自动触发定时任务。
  "forked_repo_auto_trigger": false // 是否允许本仓Fork出来的仓库按照.cnb.yml配置自动触发云原生构建。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/settings/cloud-native-build" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


