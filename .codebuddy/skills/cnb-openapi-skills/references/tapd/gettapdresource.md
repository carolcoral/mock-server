# GetTapdResource

## 接口描述

当用户提供 Tapd 链接时，识别资源类型和 ID，调用接口获取完整资源数据，供后续分析使用。

## 执行步骤

### 1. 识别 Tapd 资源链接

从用户输入中识别 Tapd 链接，支持以下格式：

`https://www.tapd.cn/tapd_fe/{workspace_id}/{resource_type}/detail/{resource_id}`

- resource_type 为资源类型。 可取值为 story、bug、task、iteration
- workspace_id 为项目 ID
- resource_id 为资源 ID

对于给出的链接，提取 workspace_id 和 resource_type，resource_id, 用于之后的请求资源。


### 2. 调用接口获取资源数据

**必须**: 执行 curl 命令时必须使用 -w '%he
{traceparent}' 获取 trace 信息，当请求失败
traceparent header, traceparent 信息仅记录
志中用于问题排查，不要在回复中展示给用户。

**重要**：请求前需替换 `{workspace_id}`, `{resource_type}`, `{resource_id}`

对每个识别到的资源，按照以下方式获取资源数据：

```bash
curl -w '%header{traceparent}' -X GET "${CNB_API_ENDPOINT}/tapd/reference/{workspace_id}/{resource_type}/{resource_id}" \
-H "Accept: application/vnd.cnb.api+json" \
-H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json"
```

**示例** https://www.tapd.cn/tapd_fe/64201929/story/detail/1164201929001000017, 其中 workspace_id 为 64201929，resource_type 为 story，id 为 1164201929001000017。
请求接口如下：
```bash
curl -w '%header{traceparent}' -X GET "${CNB_API_ENDPOINT}/tapd/reference/64201929/story/1164201929001000017" \
-H "Accept: application/vnd.cnb.api+json" \
-H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json"
```

### 3. 结果返回
API 返回标准的 JSON 格式响应。请根据 HTTP 状态码判断请求是否成功：

- 200: 请求成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

当本地调用返回的 `status` 在 200 ~ 299 之间，只需要返回 `data` 内容给用户。只有当 `status >= 300` 时，才将 `status` 和 `trace` 返回给用户。


### 4. 多资源处理

若用户提供多个 Tapd 链接，**逐个**调用接口获取，每次只请求一个资源：

1. 提取所有链接中的资源 ID 列表
2. 按顺序逐一调用接口，每次获取一个资源
3. 汇总所有资源数据后统一展示
