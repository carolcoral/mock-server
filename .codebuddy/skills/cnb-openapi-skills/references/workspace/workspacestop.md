# WorkspaceStop

## 原始 Swagger
https://api.cnb.cool/#/operations/WorkspaceStop

## 接口描述
停止/关闭我的云原生开发环境。Stop/close my workspace.
## 接口权限
account-engage:rw
## 请求信息

**请求方法：** POST

**请求地址：** ${CNB_API_ENDPOINT}/workspace/stop

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 请求体参数


**请求体结构：**

```json
{
  "pipelineId": "string", // 表示要停止的开发环境的流水线 id，sn 和 pipelineId 二选一，优先使用 pipelineId
  "sn": "string" // 表示要停止的开发环境流水线构建号，sn 和 pipelineId 二选一，优先使用 pipelineId
}
```
## 响应信息


**响应类型：** dto.WorkspaceStopResult

**响应结构：**
```json
{
  "buildLogUrl": "string", // 表示停止的开发环境流水线日志地址
  "message": "string", // 表示操作结果提示信息
  "sn": "string" // 表示停止的开发环境流水线构建号
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X POST \
  "${CNB_API_ENDPOINT}/workspace/stop" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json" \
  -d '{
  "pipelineId": "string",
  "sn": "string"
}'
```


