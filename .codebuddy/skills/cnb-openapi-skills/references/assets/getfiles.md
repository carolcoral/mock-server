# GetFiles

## 原始 Swagger
https://api.cnb.cool/#/operations/GetFiles

## 接口描述
获取 issue 文件或合并请求文件的请求，返回文件二进制内容。Request to retrieve file of issues and pull requests, returns binary content.
## 接口权限
repo-contents:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/files/{filePath}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 仓库路径|
| filePath | 字符串 | 是 | 文件路径，如果完整文件路径是 https://cnb.cool/cnb/feedback/-/files/path/to/file.txt，则 filePath 是 path/to/file.txt|
## 响应信息


**响应类型：** 无特定格式
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/files/{filePath}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


