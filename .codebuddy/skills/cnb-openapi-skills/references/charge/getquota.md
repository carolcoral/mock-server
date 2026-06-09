# GetQuota

## 原始 Swagger
https://api.cnb.cool/#/operations/GetQuota

## 接口描述
获取组织各资源类型当月额度
## 接口权限
group-resource:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/charge/quota

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | group slug|
## 响应信息


**响应类型：** dto.VolumeQuotaResp

**响应结构：**
```json
{
  "ci_gpu_in_sec": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // 云原生构建 GPU 限额，单位核秒
  "ci_in_sec": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // 云原生构建 限额，单位核秒
  "credit_in_milli": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // Credit 限额，单位 milli_credit，1000 milli_credits = 1 credit
  "dev_gpu_in_sec": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // 云原生开发 GPU 限额，单位核秒
  "dev_in_sec": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // 云原生开发 限额，单位核秒
  "git_in_byte": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  }, // Git 限额，单位字节，git 仓库除了 lfs 文件的限额
  "object_in_byte": {
    "free": 0, // 免费额度（仅免费额度）
    "total": 0 // 总额度（含免费额度、付费、特权额度等）
  } // 对象存储 限额，单位字节，包括 git lfs、制品、release 和 commit 附件等
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/charge/quota" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


