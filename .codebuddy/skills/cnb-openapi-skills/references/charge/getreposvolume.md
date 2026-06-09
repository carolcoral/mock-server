# GetReposVolume

## 原始 Swagger
https://api.cnb.cool/#/operations/GetReposVolume

## 接口描述
分页获取组织仓库用量
## 接口权限
group-resource:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/charge/repos-volume

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | group|

### 查询参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| type | 字符串 | 是| 资源类型，枚举值：charge_type_git, charge_type_object, charge_type_ci, charge_type_dev, charge_type_ci_gpu, charge_type_dev_gpu, charge_type_credit, charge_type_token|
| page | 整数 | 否| 页码|
| page_size | 整数 | 否| 每页数量|
## 响应信息


**响应类型：** 数组[dto.RepoVolume]

**响应结构（数组元素）：**
```json
[
{
    "is_deleted": false,
    "resource_id": "string",
    "resource_type": {
    },
    "slug": "string", // 仓库地址
    "volume": "string" // 用量，单位：存储类——byte；核时类——核秒；credit——milli_credit，1000 milli_credits = 1 credit
  }
]
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/charge/repos-volume" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-G \
-d "type=<type>" \
-d "page=<page>" \
-d "page_size=<page_size>" \
```


