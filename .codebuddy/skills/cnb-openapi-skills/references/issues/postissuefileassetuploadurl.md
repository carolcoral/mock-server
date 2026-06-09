# PostIssueFileAssetUploadURL

## 原始 Swagger
https://api.cnb.cool/#/operations/PostIssueFileAssetUploadURL

## 接口描述
新增一个 Issue 文件附件的上传 url 用于上传新建 Issue 的文件附件。
## 接口权限
repo-issue:rw
## 请求信息

**请求方法：** POST

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/issues/asset-groups/{asset_group_id}/file-asset-upload-url

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|
| asset_group_id | 字符串 | 是 | asset group id|

### 请求体参数


**请求体结构：**

```json
{
  "content_type": "string", // 文件类型。
  "name": "string", // 文件名。必填。
  "size": 0 // 文件大小，单位：字节。必须大于 0。
}
```
## 响应信息


**响应类型：** api.IssueAssetUploadURL

**响应结构：**
```json
{
  "asset_link": "string", // 资源链接。添加到 body 中提交。
  "name": "string", // 文件名。
  "path": "string", // 资源路径。
  "upload_url": "string" // 上传地址。使用 HTTP Put 请求流试发送数据。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X POST \
  "${CNB_API_ENDPOINT}/{repo}/-/issues/asset-groups/{asset_group_id}/file-asset-upload-url" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json" \
  -d '{
  "content_type": "string",
  "name": "string",
  "size": 0
}'
```


