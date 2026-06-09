# GetPackageTagProvenance

## 原始 Swagger
https://api.cnb.cool/#/operations/GetPackageTagProvenance

## 接口描述
获取制品标签的出生证明。 Get the specific tag provenance under specific package.
## 接口权限
registry-package:r
## 请求信息

**请求方法：** GET

**请求地址：** ${CNB_API_ENDPOINT}/{slug}/-/packages/{type}/{name}/-/tag/{tag}/provenance

### 请求头

| 请求头 | 值 | 必填 | 描述 |
|--------|----|----|------|
| Accept | application/vnd.cnb.api+json | 是 | 指定接受的响应格式 |
| Authorization | Bearer $CNB_TOKEN | 是 | 身份认证令牌 |


### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| slug | 字符串 | 是 | Slug|
| t | 字符串 | 是 | Type|
| name | 字符串 | 是 | Name|
| tag | 字符串 | 是 | Tag|
## 响应信息


**响应类型：** dto.Provenance

**响应结构：**
```json
{
  "cargo": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "composer": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "conan": {
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sources": [{
      "digest": "string", // Digest 制品摘要.
      "git_address": "string", // GitAddress git仓库地址.
      "git_commit": "string", // GitCommit git提交记录.
      "package_id": "string",
      "package_revision": "string",
      "sn": "string" // SN 流水线构建唯一标识.
    }],
    "tag": "string" // Tag 制品标签名.
  },
  "docker": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "images": [{
      "arch": "string",
      "digest": "string",
      "layers": [{
        "instruction": "string",
        "size": 0
      }],
      "os": "string",
      "size": 0
    }],
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "dockermodel": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "generic": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "helm": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "maven": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "npm": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "nuget": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "ohpm": {
    "digest": "string", // Digest 制品摘要.
    "git_address": "string", // GitAddress git仓库地址.
    "git_commit": "string", // GitCommit git提交记录.
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sn": "string", // SN 流水线构建唯一标识.
    "tag": "string" // Tag 制品标签名.
  },
  "pypi": {
    "last_pusher": {
      "is_frozen": false,
      "is_lock": false,
      "name": "string",
      "nickname": "string",
      "push_at": "string"
    }, // LastPusher 最后推送人.
    "package": "string", // Package 制品名.
    "registry_address": "string", // RegistryAddress 制品库地址.
    "sources": [{
      "digest": "string", // Digest 制品摘要.
      "file_name": "string",
      "git_address": "string", // GitAddress git仓库地址.
      "git_commit": "string", // GitCommit git提交记录.
      "sn": "string" // SN 流水线构建唯一标识.
    }],
    "tag": "string" // Tag 制品标签名.
  }
}
```
## 请求示例

**必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。

### cURL 示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/{slug}/-/packages/{type}/{name}/-/tag/{tag}/provenance" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
```


