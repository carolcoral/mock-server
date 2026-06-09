# GetLanguageList

## 原始 Swagger
https://api.cnb.cool/#/operations/GetLanguageList

## 接口描述
获取排行榜语言
## 接口权限
repo-basic-info:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/ranks/repo/language-list

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| rankType | 字符串 | 是| 排行榜类型，枚举值：daily, weekly, monthly, annual|
| date | 字符串 | 否| 日期|
## 响应信息


**响应类型：** dto.RankLanguageList

**响应结构：**
```json
{
  "date": "string",
  "language": ["string"],
  "rank_type": "string"
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/ranks/repo/language-list" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "rankType=<rankType>" \
-d "date=<date>" \
```


