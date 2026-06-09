# GetImgs

## 原始 Swagger
https://api.cnb.cool/#/operations/GetImgs

## 接口描述
获取 issue 图片或合并请求图片的请求，返回图片二进制内容。Request to retrieve image of issues and pull requests, returns binary content.
## 接口权限
repo-contents:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/imgs/{imgPath}

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 仓库路径|
| imgPath | 字符串 | 是 | 图片路径，如果完整图片路径是 https://cnb.cool/cnb/feedback/-/imgs/to/img.png，则 imgPath 是 to/img.png|
## 响应信息


**响应类型：** 无特定格式
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{repo}/-/imgs/{imgPath}" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


