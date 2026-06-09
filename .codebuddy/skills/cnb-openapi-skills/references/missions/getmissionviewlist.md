# GetMissionViewList

## 原始 Swagger
https://api.cnb.cool/#/operations/GetMissionViewList

## 接口描述
获取任务集视图列表。Get view list of a mission.
## 接口权限
mission-manage:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{mission}/-/mission/view-list

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| mission | 字符串 | 是 | mission|
## 响应信息


**响应类型：** 数组[dto.MissionView]

**响应结构（数组元素）：**
```json
[
{
    "id": "string",
    "name": "string",
    "type": {
    }
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{mission}/-/mission/view-list" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


