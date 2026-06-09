# PostPull

## 原始 Swagger
https://api.cnb.cool/#/operations/PostPull

## 接口描述
新增一个合并请求。Create a pull request.
## 接口权限
repo-pr:rw
## 请求信息

**请求方法：** POST

**请求地址：** ${CNB_API_ENDPOINT}/{repo}/-/pulls

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| repo | 字符串 | 是 | 不带.git后缀的仓库名称。格式：`组织名称/仓库名称`|

### 请求体参数


**请求体结构：**

```json
{
  "base": "string", // 目标分支名称。
  "body": "string", // 合并请求内容。
  "head": "string", // 源分支名称。
  "head_repo": "string", // 源分支所在的仓库标识。示例值：`owner/repo`
  "title": "string" // 合并请求标题。
}
```
## 响应信息


**响应类型：** api.Pull

**响应结构：**
```json
{
  "author": {
    "avatar": "string", // 用户头像。
    "email": "string", // 用户邮箱。
    "freeze": false, // 是否冻结。
    "is_npc": false, // 是否是 NPC。
    "nickname": "string", // 昵称。
    "username": "string" // 用户名。
  }, // 作者信息。
  "base": {
    "ref": "string", // 分支引用名称。示例值：`refs/heads/main`
    "repo": {
      "id": "string", // 仓库的唯一标识符。
      "name": "string", // 仓库名称。
      "path": "string", // 仓库的路径标识符。
      "web_url": "string" // 仓库的Web访问地址。
    }, // 关联的仓库信息。
    "sha": "string" // 提交哈希值。
  }, // 目标分支信息。
  "blocked_on": "string", // 阻塞原因。枚举值：`no_merge_base`,`internal_error`,`code_conflict`,`status_check`,`waiting_review`
  "body": "string", // 合并请求内容。
  "head": {
    "ref": "string", // 分支引用名称。示例值：`refs/heads/main`
    "repo": {
      "id": "string", // 仓库的唯一标识符。
      "name": "string", // 仓库名称。
      "path": "string", // 仓库的路径标识符。
      "web_url": "string" // 仓库的Web访问地址。
    }, // 关联的仓库信息。
    "sha": "string" // 提交哈希值。
  }, // 源分支信息。
  "is_wip": false, // 是否为WIP状态，WIP状态表示合并请求是否是草稿阶段。
  "labels": [{
    "applied_by": {
      "avatar": "string", // 用户头像。
      "email": "string", // 用户邮箱。
      "freeze": false, // 是否冻结。
      "is_npc": false, // 是否是 NPC。
      "nickname": "string", // 昵称。
      "username": "string" // 用户名。
    }, // 贴标者信息。
    "color": "string", // 标签颜色。
    "creator": {
      "avatar": "string", // 用户头像。
      "email": "string", // 用户邮箱。
      "freeze": false, // 是否冻结。
      "is_npc": false, // 是否是 NPC。
      "nickname": "string", // 昵称。
      "username": "string" // 用户名。
    }, // 标签创建者信息。
    "description": "string", // 标签描述。
    "id": "string", // 标签ID。
    "name": "string" // 标签名称。
  }], // 关联的标签列表（含创建者和贴标者信息）。
  "mergeable_state": "string", // 可合并状态。枚举值：`checking`,`mergeable`,`merging`,`merged`,`conflict`,`no-merge-base`
  "merged_by": {
    "avatar": "string", // 用户头像。
    "email": "string", // 用户邮箱。
    "freeze": false, // 是否冻结。
    "is_npc": false, // 是否是 NPC。
    "nickname": "string", // 昵称。
    "username": "string" // 用户名。
  }, // 合并者信息。
  "number": "string", // 合并请求唯一标识符编号。
  "reviewers": [{
    "review_state": "string", // 评审状态。枚举值：`pending`,`commented`,`approved`,`changes_requested`,`dismissed`。
    "user": {
      "avatar": "string", // 用户头像。
      "email": "string", // 用户邮箱。
      "freeze": false, // 是否冻结。
      "is_npc": false, // 是否是 NPC。
      "nickname": "string", // 昵称。
      "username": "string" // 用户名。
    } // 评审人信息。
  }], // 评审人列表，包含评审状态信息。
  "state": "string", // 合并请求状态。枚举值：`open`,`closed`,`merged`
  "title": "string" // 合并请求标题。
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X POST \
  "${CNB_API_ENDPOINT}/{repo}/-/pulls" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
-H "Content-Type: application/json" \
  -d '{
  "base": "string",
  "body": "string",
  "head": "string",
  "head_repo": "string",
  "title": "string"
}'
```


