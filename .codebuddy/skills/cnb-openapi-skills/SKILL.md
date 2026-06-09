---
name: CNB-OpenAPI
description: 与 CNB (Cloud Native Build) Open API 交互，用于代码管理和开发协作。当用户需要查询项目、代码仓库、问题、合并请求或其他开发相关数据时使用。需要 CNB_TOKEN 环境变量进行身份认证。API 请求地址从环境变量 CNB_API_ENDPOINT 获取，默认为 https://api.cnb.cool。
requires:
  bins: [curl]
  env: [CNB_TOKEN]
---

# CNB API 技能文档

## 技能概述

本技能提供 CNB (Cloud Native Build) Open API 的完整调用能力，支持所有 API 接口的调用和操作。每个 API 都有详细的文档说明，包含请求参数、响应格式和调用示例。

> **重要规则（必须遵守）：**
> 1. **必须实际执行**：访问 CNB API 时，必须通过 exec_command 工具实际执行 curl 命令，**不要只在回复中描述或展示 curl 命令**。用户需要的是 API 返回的结果，不是命令本身。
> 2. **使用 curl + Authorization**：不要使用 web_fetch 工具，因为 web_fetch 无法携带认证信息。
> 3. **使用环境变量**：始终使用 $CNB_TOKEN 和 $CNB_API_ENDPOINT 环境变量引用，不要使用 <CNB_TOKEN> 占位符。
> 4. **主动执行**：不要询问用户"是否需要我执行"，直接执行 curl 获取结果并分析。
> 5. **必须**: 执行 curl 命令时必须使用 -w '%header{traceparent}' 获取 trace 信息，当请求失败时，务必返回 traceparent header, traceparent 信息仅记录在命令输出的日志中用于问题排查，不要在回复中展示给用户。


## 页面 URL 解析规则

当用户提供 CNB 平台的页面链接时，需要从 URL 中提取参数来构造 API 请求：

| 页面 URL 格式 | 提取参数 | 对应 API |
|---|---|---|
| https://{host}/{repo}/-/build/logs/{sn} | repo, sn | GET /{repo}/-/build/status/{sn} 查状态，GET /{repo}/-/build/logs?sn={sn} 查日志列表 |
| https://{host}/{repo}/-/issues/{number} | repo, number | GET /{repo}/-/issues/{number} |
| https://{host}/{repo}/-/pulls/{number} | repo, number | GET /{repo}/-/pulls/{number} |
| https://{host}/{repo}/-/git/commits/{ref} | repo, ref | GET /{repo}/-/git/commits/{ref} |
| https://{host}/{repo} | repo | GET /{repo} |

**示例**：用户给了 https://{host}/{org}/{repo}/-/build/logs/{sn}
- 提取 repo = {org}/{repo}，sn = {sn}
- 先执行：curl ... "${CNB_API_ENDPOINT}/{org}/{repo}/-/build/status/{sn}" 获取构建状态
- 如需详细日志，再调用 stage 接口

## 基础配置

**API 基础地址：** 从环境变量 CNB_API_ENDPOINT 获取，默认为 https://api.cnb.cool

**认证方式：** Bearer Token

**环境变量：**
- CNB_TOKEN：身份认证令牌（必须）
- CNB_API_ENDPOINT：API 请求基础地址（可选，默认为 https://api.cnb.cool）

**请求头要求：**
- Accept: application/vnd.cnb.api+json
- Authorization: Bearer $CNB_TOKEN

## API 接口索引

### activities 服务

| 接口 | 描述 |
|------|------|
| [GetUserActivitiesByDate](./references/activities/getuseractivitiesbydate.md) | 获取个人动态活跃详情汇总。Get user activities by date. |
| [GetUserRepoActivityDetails](./references/activities/getuserrepoactivitydetails.md) | 个人仓库动态详情列表。List of personal repository activity details. |
| [TopContributors](./references/activities/topcontributors.md) | 获取仓库 top 活跃用户。List the top active users |
### ai 服务

| 接口 | 描述 |
|------|------|
| [AiChatCompletions](./references/ai/aichatcompletions.md) | AI 对话。AI chat completions. |
### assets 服务

| 接口 | 描述 |
|------|------|
| [DeleteAsset](./references/assets/deleteasset.md) | 通过 asset 记录 id 删除一个 asset |
| [GetFiles](./references/assets/getfiles.md) | 获取 issue 文件或合并请求文件的请求，返回文件二进制内容。Request to retrieve file of issues and pull requests, returns binary content. |
| [GetImgs](./references/assets/getimgs.md) | 获取 issue 图片或合并请求图片的请求，返回图片二进制内容。Request to retrieve image of issues and pull requests, returns binary content. |
| [ListAssets](./references/assets/listassets.md) | 仓库的 asset 记录列表 |
### badge 服务

| 接口 | 描述 |
|------|------|
| [GetBadge](./references/badge/getbadge.md) | 获取徽章 svg 或 JSON 数据。Get badge svg or JSON data. |
| [ListBadge](./references/badge/listbadge.md) | 获取徽章列表数据。List badge data |
| [UploadBadge](./references/badge/uploadbadge.md) | 上传徽章数据。Upload badge data |
### build 服务

| 接口 | 描述 |
|------|------|
| [BuildCrontabSync](./references/build/buildcrontabsync.md) | 同步仓库分支下的定时任务。 Synchronize the content under the repository branch. |
| [BuildLogsDelete](./references/build/buildlogsdelete.md) | 删除流水线日志内容。Delete pipeline logs content. |
| [BuildRunnerDownloadLog](./references/build/buildrunnerdownloadlog.md) | 流水线runner日志下载。Pipeline runner log download. |
| [GetBuildLogs](./references/build/getbuildlogs.md) | 查询流水线构建列表。List pipeline builds. |
| [GetBuildStage](./references/build/getbuildstage.md) | 查询流水线Stage详情。Get pipeline build stage detail. |
| [GetBuildStatus](./references/build/getbuildstatus.md) | 查询流水线构建状态。Get pipeline build status. |
| [StartBuild](./references/build/startbuild.md) | 开始一个构建。Start a build. |
| [StopBuild](./references/build/stopbuild.md) | 停止一个构建。 Stop a build. |
### charge 服务

| 接口 | 描述 |
|------|------|
| [GetQuota](./references/charge/getquota.md) | 获取组织各资源类型当月额度 |
| [GetReposVolume](./references/charge/getreposvolume.md) | 分页获取组织仓库用量 |
| [GetSpecialAmount](./references/charge/getspecialamount.md) | 查看特权额度 |
| [GetVolume](./references/charge/getvolume.md) | 获取根组织各资源用量 |
### event 服务

| 接口 | 描述 |
|------|------|
| [GetEvents](./references/event/getevents.md) | 获取仓库动态预签名地址，并返回内容。Get events pre-signed URL and return content. |
### followers 服务

| 接口 | 描述 |
|------|------|
| [GetFollowersByUserID](./references/followers/getfollowersbyuserid.md) | 获取指定用户的粉丝列表。Get the followers list of specified user. |
| [GetFollowingByUserID](./references/followers/getfollowingbyuserid.md) | 获取指定用户的关注人列表。Get the list of users that the specified user is following. |
### git 服务

| 接口 | 描述 |
|------|------|
| [CreateBlob](./references/git/createblob.md) | 创建一个 blob。Create a blob. |
| [CreateBranch](./references/git/createbranch.md) | 创建新分支。Create a new branch based on a start point. |
| [CreateBranchLock](./references/git/createbranchlock.md) | 锁定分支 |
| [CreateTag](./references/git/createtag.md) | 创建一个 tag。Create a tag. |
| [DeleteBranch](./references/git/deletebranch.md) | 删除指定分支。Delete the specified branch. |
| [DeleteBranchLock](./references/git/deletebranchlock.md) | 解除锁定分支 |
| [DeleteCommitAnnotation](./references/git/deletecommitannotation.md) | 删除指定 commit 的元数据。Delete commit annotation. |
| [DeleteCommitAsset](./references/git/deletecommitasset.md) | 删除指定 commit 的附件。Delete commit asset. |
| [DeleteTag](./references/git/deletetag.md) | 删除指定 tag。Delete the specified tag. |
| [DeleteTagAnnotation](./references/git/deletetagannotation.md) | 删除指定 tag 的元数据。Delete the metadata of the specified tag. |
| [GetArchive](./references/git/getarchive.md) | 下载仓库内容 |
| [GetArchiveCommitChangedFiles](./references/git/getarchivecommitchangedfiles.md) | 打包下载 commit 变更文件。Download archive of changed files for a commit. |
| [GetArchiveCompareChangedFiles](./references/git/getarchivecomparechangedfiles.md) | 打包下载两次 ref 之间的变更文件。Download archive of changed files for a compare. |
| [GetBranch](./references/git/getbranch.md) | 查询指定分支。Get a branch. |
| [GetCommit](./references/git/getcommit.md) | 查询指定 commit。Get a commit. |
| [GetCommitAnnotations](./references/git/getcommitannotations.md) | 查询指定 commit 的元数据。Get commit annotations. |
| [GetCommitAnnotationsInBatch](./references/git/getcommitannotationsinbatch.md) | 查询指定 commit 的元数据。Get commit annotations in batch. |
| [GetCommitAssets](./references/git/getcommitassets.md) | 发起一个获取 commits 附件的请求， 302到有一定效期的下载地址。Get a request to fetch a commit assets and returns 302 redirect to the assets URL with specific valid time. |
| [GetCommitAssetsBySha](./references/git/getcommitassetsbysha.md) | 查询指定 commit 的附件。List commit assets. |
| [GetCommitStatuses](./references/git/getcommitstatuses.md) | 查询指定 commit 的提交状态。List commit check statuses. |
| [GetCompareCommits](./references/git/getcomparecommits.md) | 比较两个提交、分支或标签之间差异的接口。Compare two commits, branches, or tags. |
| [GetContent](./references/git/getcontent.md) | 查询仓库文件列表或文件。List repository files or file. |
| [GetContentWithoutPath](./references/git/getcontentwithoutpath.md) | 查询仓库文件和目录内容。List repository files and directories. |
| [GetHead](./references/git/gethead.md) | 获取仓库默认分支。Get the default branch of the repository. |
| [GetPresignedLFSDownloadLink](./references/git/getpresignedlfsdownloadlink.md) | 获取 git lfs 文件下载链接 |
| [GetRaw](./references/git/getraw.md) | 获得仓库指定文件内容 |
| [GetTag](./references/git/gettag.md) | 查询指定 tag。Get a tag. |
| [GetTagAnnotations](./references/git/gettagannotations.md) | 查询指定 tag 的元数据。Query the metadata of the specified tag. |
| [ListBranches](./references/git/listbranches.md) | 查询分支列表。List branches. |
| [ListCommits](./references/git/listcommits.md) | 查询 commit 列表。List commits. |
| [ListTags](./references/git/listtags.md) | 查询 tag 列表。List tags. |
| [PostCommitAssetUploadConfirmation](./references/git/postcommitassetuploadconfirmation.md) | 确认 commit 附件上传完成。Confirm commit asset upload. |
| [PostCommitAssetUploadURL](./references/git/postcommitassetuploadurl.md) | 新增一个 commit 附件。Create a commit asset. |
| [PutCommitAnnotations](./references/git/putcommitannotations.md) | 设定指定 commit 的元数据。Put commit annotations. |
| [PutTagAnnotations](./references/git/puttagannotations.md) | 设定指定 tag 的元数据。Set the metadata of the specified tag. |
### gitsettings 服务

| 接口 | 描述 |
|------|------|
| [DeleteBranchProtection](./references/gitsettings/deletebranchprotection.md) | 删除仓库保护分支规则。 Delete branch protection rule. |
| [GetBranchProtection](./references/gitsettings/getbranchprotection.md) | 查询仓库保护分支规则。Get branch protection rule. |
| [GetPipelineSettings](./references/gitsettings/getpipelinesettings.md) | 查询仓库云原生构建设置。List pipeline settings. |
| [GetPullRequestSettings](./references/gitsettings/getpullrequestsettings.md) | 查询仓库合并请求设置。List pull request settings. |
| [GetPushLimitSettings](./references/gitsettings/getpushlimitsettings.md) | 查询仓库推送设置。List push limit settings. |
| [ListBranchProtections](./references/gitsettings/listbranchprotections.md) | 查询仓库保护分支规则列表。List branch protection rules. |
| [PatchBranchProtection](./references/gitsettings/patchbranchprotection.md) | 更新仓库保护分支规则。Update branch protection rule. |
| [PostBranchProtection](./references/gitsettings/postbranchprotection.md) | 新增仓库保护分支规则。Create branch protection rule. |
| [PutPipelineSettings](./references/gitsettings/putpipelinesettings.md) | 更新仓库云原生构建设置。Update pipeline settings. |
| [PutPullRequestSettings](./references/gitsettings/putpullrequestsettings.md) | 更新仓库合并请求设置。Set pull request settings. |
| [PutPushLimitSettings](./references/gitsettings/putpushlimitsettings.md) | 设置仓库推送设置。Set push limit settings. |
### issues 服务

| 接口 | 描述 |
|------|------|
| [CanUserBeAssignedToIssue](./references/issues/canuserbeassignedtoissue.md) | 检查用户是否可以被添加到 issue 的处理人中。 Checks if a user can be assigned to an issue. |
| [CreateIssue](./references/issues/createissue.md) | 创建一个 Issue。Create an issue. |
| [DeleteIssueAssignees](./references/issues/deleteissueassignees.md) | 删除 issue 中的处理人。 Removes one or more assignees from an issue. |
| [DeleteIssueLabel](./references/issues/deleteissuelabel.md) | 删除 issue 标签。Remove a label from an issue. |
| [DeleteIssueLabels](./references/issues/deleteissuelabels.md) | 清空 issue 标签。Remove all labels from an issue. |
| [GetIssue](./references/issues/getissue.md) | 查询指定的 Issues。Get an issue. |
| [GetIssueComment](./references/issues/getissuecomment.md) | 获取指定 issue 评论。Get an issue comment. |
| [GetIssueFiles](./references/issues/getissuefiles.md) | 获取 Issue 文件，返回文件二进制内容。Request to retrieve file of issues, returns binary content. |
| [GetIssueImgs](./references/issues/getissueimgs.md) | 获取 Issue 图片，返回图片二进制内容。Request to retrieve image of issues, returns binary content. |
| [GetIssueProperties](./references/issues/getissueproperties.md) | 查询指定Issue的自定义属性列表。Get issue custom properties. |
| [ListIssueActivities](./references/issues/listissueactivities.md) | 查询指定 Issue 的 Timeline Activity |
| [ListIssueActivitiesLatest](./references/issues/listissueactivitieslatest.md) | 查询某一动态之后的 Issue Activity |
| [ListIssueAssignees](./references/issues/listissueassignees.md) | 查询指定 issue 的处理人。 List repository issue assignees. |
| [ListIssueComments](./references/issues/listissuecomments.md) | 查询仓库的 issue 评论列表。List repository issue comments. |
| [ListIssueLabels](./references/issues/listissuelabels.md) | 查询 issue 的标签列表。List labels for an issue. |
| [ListIssues](./references/issues/listissues.md) | 查询仓库的 Issues。List issues. |
| [ListRepoInvisibleProperties](./references/issues/listrepoinvisibleproperties.md) | 查询仓库不可见的自定义属性列表。List repository invisible custom properties. |
| [ListRepoVisibleProperties](./references/issues/listrepovisibleproperties.md) | 查询仓库可见的自定义属性列表。List repository visible custom properties. |
| [ListUserIssues](./references/issues/listuserissues.md) | 查询当前用户相关的 Issues。List issues for the authenticated user across all repositories. |
| [PatchIssueAssignees](./references/issues/patchissueassignees.md) | 更新 issue 中的处理人。 Updates the assignees of an issue. |
| [PatchIssueComment](./references/issues/patchissuecomment.md) | 修改一个 issue 评论。Update an issue comment. |
| [PostAssetGroup](./references/issues/postassetgroup.md) | 创建一个 Issue 附件组用于上传新建 Issue 的附件。一次可以获取 16 个附件上传 url。需要上传更多附件可以使用 /{repo}/-/issues/asset-groups/{asset_group_id}/image-asset-upload-url 和 /{repo}/-/issues/asset-groups/{asset_group_id}/file-asset-upload-url 接口获取更多的上传 url。 |
| [PostIssueAssignees](./references/issues/postissueassignees.md) | 添加处理人到指定的 issue。  Adds up to assignees to a issue, Users already assigned to an issue are not replaced. |
| [PostIssueComment](./references/issues/postissuecomment.md) | 创建一个 issue 评论。Create an issue comment. |
| [PostIssueCommentFileAssetUploadURL](./references/issues/postissuecommentfileassetuploadurl.md) | 创建一个 Issue 评论的文件上传 url。请使用 put 发起流式上传到 upload_url 地址。上传完成后将 asset_link 添加到创建评论请求的 body 中。 Create a file upload URL for an Issue comment. Please use put to initiate a stream upload to the upload_url address. After uploading, add the asset_link to the body of the create comment request. |
| [PostIssueCommentImageAssetUploadURL](./references/issues/postissuecommentimageassetuploadurl.md) | 创建一个 Issue 评论的图片上传 url。请使用 put 发起流式上传到 upload_url 地址。上传完成后将 asset_link 添加到创建评论请求的 body 中。Create an image upload URL for an Issue comment. Please use put to initiate a stream upload to the upload_url address. After uploading, add the asset_link to the body of the create comment request. |
| [PostIssueFileAssetUploadURL](./references/issues/postissuefileassetuploadurl.md) | 新增一个 Issue 文件附件的上传 url 用于上传新建 Issue 的文件附件。 |
| [PostIssueImageAssetUploadURL](./references/issues/postissueimageassetuploadurl.md) | 新增一个 Issue 图片的上传 url 用于上传新建 Issue 的图片。 |
| [PostIssueLabels](./references/issues/postissuelabels.md) | 新增 issue 标签。Add labels to an issue. |
| [PutIssueLabels](./references/issues/putissuelabels.md) | 设置 issue 标签。 Set the new labels for an issue. |
| [UpdateIssue](./references/issues/updateissue.md) | 更新一个 Issue。Update an issue. |
| [UpdateIssueProperties](./references/issues/updateissueproperties.md) | 批量更新Issue自定义属性值 |
### knowledgebase 服务

| 接口 | 描述 |
|------|------|
| [DeleteKnowledgeBase](./references/knowledgebase/deleteknowledgebase.md) | 删除知识库 |
| [GetKnowledgeBaseInfo](./references/knowledgebase/getknowledgebaseinfo.md) | 获取知识库信息 |
| [GetModels](./references/knowledgebase/getmodels.md) | 获取当前支持的 Embedding 模型列表 |
| [QueryKnowledgeBase](./references/knowledgebase/queryknowledgebase.md) | 查询知识库 [POST 接口已废弃，请使用 GET 替代] |
| [QueryKnowledgeBaseGet](./references/knowledgebase/queryknowledgebaseget.md) | 查询知识库，使用文档：https://docs.cnb.cool/zh/ai/knowledge-base.html |
| [SearchNpc](./references/knowledgebase/searchnpc.md) | 全局语义搜索 NPC 角色 |
### members 服务

| 接口 | 描述 |
|------|------|
| [AddMembersOfGroup](./references/members/addmembersofgroup.md) | 添加成员。Add members. |
| [AddMembersOfMission](./references/members/addmembersofmission.md) | 添加成员。Add members. |
| [AddMembersOfRegistry](./references/members/addmembersofregistry.md) | 添加成员。Add members. |
| [AddMembersOfRepo](./references/members/addmembersofrepo.md) | 添加成员。Add members. |
| [DeleteMembersOfGroup](./references/members/deletemembersofgroup.md) | 删除指定组织的直接成员。Remove direct members from specified organization. |
| [DeleteMembersOfRepo](./references/members/deletemembersofrepo.md) | 删除指定仓库的直接成员。Remove direct members from specified repository. |
| [DeleteOutsideCollaborators](./references/members/deleteoutsidecollaborators.md) | 删除指定仓库的外部贡献者。Removes external contributors from specified repository. |
| [GetMemberAccessLevelOfGroup](./references/members/getmemberaccesslevelofgroup.md) | 获取指定组织内, 访问成员在当前层级内的权限信息。Get permission information for accessing members at current level. |
| [GetMemberAccessLevelOfRepo](./references/members/getmemberaccesslevelofrepo.md) | 获取指定仓库内, 访问成员在当前层级内的权限信息。Get permission information for accessing members at current level. |
| [ListAllMembers](./references/members/listallmembers.md) | 获取指定仓库内的有效成员列表，包含继承成员。List active members in specified repository including inherited members. |
| [ListInheritMembersOfGroup](./references/members/listinheritmembersofgroup.md) | 获取指定组织的继承成员。List inherited members within specified organization |
| [ListInheritMembersOfRepo](./references/members/listinheritmembersofrepo.md) | 获取指定仓库内的继承成员。List inherited members within specified repository。 |
| [ListMemberAccessLevelOfGroup](./references/members/listmemberaccesslevelofgroup.md) | 获取指定组织内指定成员的权限信息, 结果按组织层级来展示, 包含上层组织的权限继承信息。Get specified member's permissions with organizational hierarchy. |
| [ListMemberAccessLevelOfRepo](./references/members/listmemberaccesslevelofrepo.md) | 获取指定仓库内指定成员的权限信息, 结果按组织层级来展示, 包含上层组织的权限继承信息。Get specified member's permissions with organizational hierarchy. |
| [ListMembersOfGroup](./references/members/listmembersofgroup.md) | 获取指定组织内的所有直接成员。List all direct members within specified organization. |
| [ListMembersOfRepo](./references/members/listmembersofrepo.md) | 获取指定仓库内的所有直接成员。List all direct members within specified repository. |
| [ListOutsideCollaborators](./references/members/listoutsidecollaborators.md) | 获取指定仓库内的外部贡献者。List external contributors in specified repository. |
| [UpdateMembersOfGroup](./references/members/updatemembersofgroup.md) | 更新指定组织的直接成员权限信息。Update permission information for direct members in specified organization. |
| [UpdateMembersOfRepo](./references/members/updatemembersofrepo.md) | 更新指定仓库内的直接成员权限信息。Update permission information for direct members in specified repository. |
| [UpdateOutsideCollaborators](./references/members/updateoutsidecollaborators.md) | 更新指定仓库的外部贡献者权限信息。 Update permission information for external contributors in specified repository. |
### missions 服务

| 接口 | 描述 |
|------|------|
| [CreateMission](./references/missions/createmission.md) | 创建任务集。Create a mission. |
| [DeleteMission](./references/missions/deletemission.md) | 删除指定任务集。Delete the specified mission. |
| [GetGroupSubMissions](./references/missions/getgroupsubmissions.md) | 查询组织下面用户有权限查看到的任务集。Query all missions that the user has permission to see under the specific organization. |
| [GetMissionViewConfig](./references/missions/getmissionviewconfig.md) | 查询任务集视图配置信息。Get mission view config. |
| [GetMissionViewList](./references/missions/getmissionviewlist.md) | 获取任务集视图列表。Get view list of a mission. |
| [PostMissionViewConfig](./references/missions/postmissionviewconfig.md) | 设置任务集视图配置信息。Set mission view config. |
| [PostMissionViewList](./references/missions/postmissionviewlist.md) | 排序任务集视图。Sort mission view list. |
| [PutMissionViewList](./references/missions/putmissionviewlist.md) | 添加、修改任务集视图。Update a mission view or add a new one. |
### organizations 服务

| 接口 | 描述 |
|------|------|
| [CreateOrganization](./references/organizations/createorganization.md) | 创建新组织。Create new organization. |
| [DeleteOrganization](./references/organizations/deleteorganization.md) | 删除指定组织。Delete the specified organization. |
| [GetGroup](./references/organizations/getgroup.md) | 获取指定组织信息。Get information for the specified organization. |
| [GetGroupSetting](./references/organizations/getgroupsetting.md) | 获取指定组织的配置详情。Get the configuration details for the specified organization. |
| [GetGroupsByUserID](./references/organizations/getgroupsbyuserid.md) | 获取指定用户拥有权限的顶层组织列表。 Get a list of top-level organizations that the specified user has permissions to access. |
| [ListGroups](./references/organizations/listgroups.md) | 查询当前用户在指定组织下拥有指定权限的子组织列表。Get the list of sub-organizations that the current user has access to in the specified organization. |
| [ListSubgroups](./references/organizations/listsubgroups.md) | 获取指定组织下的子组织列表。Get the list of sub-organizations under the specified organization. |
| [ListTopGroups](./references/organizations/listtopgroups.md) | 获取当前用户拥有权限的顶层组织列表。Get top-level organizations list that the current user has access to. |
| [TransferGroup](./references/organizations/transfergroup.md) | 转移组织。Transfer an organization. |
| [UpdateGroupSetting](./references/organizations/updategroupsetting.md) | 更新指定组织的配置。Updates the configuration for the specified organization. |
| [UpdateOrganization](./references/organizations/updateorganization.md) | 更新组织信息, 可更新的内容为: 组织描述, 组织展示名称, 组织网站, 组织联系邮箱。Updates organization information including: description, display name, website URL and contact email. |
| [UploadLogos](./references/organizations/uploadlogos.md) | 发起一个上传 logo 的请求，返回上传文件的url，请使用 put 发起流式上传。Initiate a request to upload logo,returns upload URL.Use PUT to initiate a stream upload. |
### pulls 服务

| 接口 | 描述 |
|------|------|
| [CanUserBeAssignedToPull](./references/pulls/canuserbeassignedtopull.md) | 检查用户是否可以被添加到合并请求的处理人中。 Checks if a user can be assigned to a pull request. |
| [DeletePullAssignees](./references/pulls/deletepullassignees.md) | 删除合并请求中的处理人 Removes one or more assignees from a pull request. |
| [DeletePullLabel](./references/pulls/deletepulllabel.md) | 删除合并请求标签。Remove a label from a pull. |
| [DeletePullLabels](./references/pulls/deletepulllabels.md) | 清空合并请求标签。Remove all labels from a pull. |
| [DeletePullReviewers](./references/pulls/deletepullreviewers.md) | 删除合并请求中的评审人。Removes one or more reviewers from a pull request. |
| [DeleteRepoFiles](./references/pulls/deleterepofiles.md) | 删除 UploadFiles 上传的附件 |
| [DeleteRepoImgs](./references/pulls/deleterepoimgs.md) | 删除 UploadImgs 上传的图片 |
| [GetPrFiles](./references/pulls/getprfiles.md) | 获取合并请求文件，返回文件二进制内容。Request to retrieve file of pull requests, returns binary content. |
| [GetPrImgs](./references/pulls/getprimgs.md) | 获取合并请求图片，返回图片二进制内容。Request to retrieve image of pull requests, returns binary content. |
| [GetPull](./references/pulls/getpull.md) | 查询指定合并请求。Get a pull request. |
| [GetPullComment](./references/pulls/getpullcomment.md) | 获取一个合并请求评论。Get a pull comment. |
| [ListPullAssignees](./references/pulls/listpullassignees.md) | 查询指定合并请求的处理人。List repository pull request assignees. |
| [ListPullComments](./references/pulls/listpullcomments.md) | 查询合并请求评论列表。List pull comments requests. |
| [ListPullCommitStatuses](./references/pulls/listpullcommitstatuses.md) | 查询 Pull Request 的状态检查 |
| [ListPullCommits](./references/pulls/listpullcommits.md) | 查询指定合并请求的提交列表。Lists the commits in a specified pull request. |
| [ListPullFiles](./references/pulls/listpullfiles.md) | 查询指定合并请求的文件列表。Lists the files in a specified pull request. |
| [ListPullLabels](./references/pulls/listpulllabels.md) | 查询指定合并请求的标签列表。List labels for a pull. |
| [ListPullReviewComments](./references/pulls/listpullreviewcomments.md) | 查询指定合并请求评审评论列表。List pull review comments. |
| [ListPullReviews](./references/pulls/listpullreviews.md) | 查询特定合并请求的评审列表。List pull reviews. |
| [ListPulls](./references/pulls/listpulls.md) | 查询合并请求列表。List pull requests. |
| [ListPullsByNumbers](./references/pulls/listpullsbynumbers.md) | 根据 number 列表查询合并请求列表。List pull requests by numbers. |
| [MergePull](./references/pulls/mergepull.md) | 合并一个合并请求。Merge a pull request. |
| [PatchPull](./references/pulls/patchpull.md) | 更新一个合并请求。Update a pull request. |
| [PatchPullComment](./references/pulls/patchpullcomment.md) | 更新一个合并请求评论。Update a pull comment. |
| [PostPull](./references/pulls/postpull.md) | 新增一个合并请求。Create a pull request. |
| [PostPullAssignees](./references/pulls/postpullassignees.md) | 添加处理人到指定的合并请求。 Adds up to assignees to a pull request. Users already assigned to an issue are not replaced. |
| [PostPullComment](./references/pulls/postpullcomment.md) | 新增一个合并请求评论。Create a pull comment. |
| [PostPullLabels](./references/pulls/postpulllabels.md) | 新增合并请求标签。Add labels to a pull. |
| [PostPullRequestReviewReply](./references/pulls/postpullrequestreviewreply.md) | 回复一个 review 评审 |
| [PostPullReview](./references/pulls/postpullreview.md) | 新增一次合并请求评审。Create a pull review. |
| [PostPullReviewers](./references/pulls/postpullreviewers.md) | 添加评审人到指定的合并请求。Adds up to reviewers to a pull request. Users already added as reviewers are not replaced. |
| [PutPullLabels](./references/pulls/putpulllabels.md) | 设置合并请求标签。Set the new labels for a pull. |
| [UploadFiles](./references/pulls/uploadfiles.md) | 发起一个上传 files 的请求，返回上传文件的url，请使用 put 发起流式上传。Initiate a request to upload files,returns upload URL.Use PUT to initiate a stream upload. |
| [UploadImgs](./references/pulls/uploadimgs.md) | 发起一个上传 imgs 的请求，返回上传文件的url，请使用 put 发起流式上传。Initiate a request to upload images,returns upload URL.Use PUT to initiate a stream upload. |
### rank 服务

| 接口 | 描述 |
|------|------|
| [GetLanguageList](./references/rank/getlanguagelist.md) | 获取排行榜语言 |
| [GetRepoAnnualRank](./references/rank/getrepoannualrank.md) | 获取公仓年榜 |
| [GetRepoDailyRank](./references/rank/getrepodailyrank.md) | 获取公仓日榜 |
| [GetRepoMonthlyRank](./references/rank/getrepomonthlyrank.md) | 获取公仓月榜 |
| [GetRepoWeeklyRank](./references/rank/getrepoweeklyrank.md) | 获取公仓周榜 |
### registries 服务

| 接口 | 描述 |
|------|------|
| [DeletePackage](./references/registries/deletepackage.md) | 删除制品。 Delete the specific package. |
| [DeletePackageTag](./references/registries/deletepackagetag.md) | 删除制品标签。 Delete the specific tag under specific package |
| [DeleteRegistry](./references/registries/deleteregistry.md) | 删除制品库。Delete the registry. |
| [GetGroupSubRegistries](./references/registries/getgroupsubregistries.md) | 查询组织下面用户有权限查看到的制品仓库。Query all registries that the user has permission to see under specific organization. |
| [GetPackage](./references/registries/getpackage.md) | 获取指定制品的详细信息。 Get the package detail. |
| [GetPackageTagDetail](./references/registries/getpackagetagdetail.md) | 获取制品标签详情。 Get the specific tag under specific package. |
| [GetPackageTagProvenance](./references/registries/getpackagetagprovenance.md) | 获取制品标签的出生证明。 Get the specific tag provenance under specific package. |
| [ListPackageTags](./references/registries/listpackagetags.md) | 查询制品标签列表。 List all tags under specific package. |
| [ListPackages](./references/registries/listpackages.md) | 查询制品列表。 List all packages. |
| [UpdatePackageDescription](./references/registries/updatepackagedescription.md) | 更新制品描述。Update the description of the specific package. |
### releases 服务

| 接口 | 描述 |
|------|------|
| [DeleteRelease](./references/releases/deleterelease.md) | 删除指定的 release。Delete a release. |
| [DeleteReleaseAsset](./references/releases/deletereleaseasset.md) | 删除指定的 release 附件 the specified release asset. |
| [GetLatestRelease](./references/releases/getlatestrelease.md) | 查询最新的 release。Query the latest release. |
| [GetReleaseAsset](./references/releases/getreleaseasset.md) | 查询指定的 release 附件 the specified release asset. |
| [GetReleaseByID](./references/releases/getreleasebyid.md) | 根据 id	查询指定 release, 包含附件信息。Get a release by id, include assets information. |
| [GetReleaseByTag](./references/releases/getreleasebytag.md) | 通过 tag 查询指定 release,包含附件信息。Get a release by tag, include assets information. |
| [GetReleasesAsset](./references/releases/getreleasesasset.md) | 发起一个获取 release 附件的请求， 302到有一定效期的下载地址。Get a request to fetch a release assets and returns 302 redirect to the assets URL with specific valid time. |
| [ListReleases](./references/releases/listreleases.md) | 查询 release 列表。List releases. |
| [PatchRelease](./references/releases/patchrelease.md) | 更新 release。Update a release. |
| [PostRelease](./references/releases/postrelease.md) | 新增一个 release。Create a release. |
| [PostReleaseAssetUploadConfirmation](./references/releases/postreleaseassetuploadconfirmation.md) | 确认  release 附件上传完成。Confirm release asset upload. |
| [PostReleaseAssetUploadURL](./references/releases/postreleaseassetuploadurl.md) | 新增一个 release 附件。Create a release asset. |
### repocodeissue 服务

| 接口 | 描述 |
|------|------|
| [GetCodeIssueDetailOpenAPI](./references/repocodeissue/getcodeissuedetailopenapi.md) | 获取源码扫描问题详情 |
| [ListCodeIssueOpenAPI](./references/repocodeissue/listcodeissueopenapi.md) | 获取源码扫描问题列表 |
### repocontributor 服务

| 接口 | 描述 |
|------|------|
| [GetRepoContributorTrend](./references/repocontributor/getrepocontributortrend.md) | 查询仓库贡献者前 100 名的详细趋势数据。Query detailed trend data for top 100 contributors of the repository. |
### repolabels 服务

| 接口 | 描述 |
|------|------|
| [DeleteLabel](./references/repolabels/deletelabel.md) | 删除指定的仓库标签。Delete the specified repository label. |
| [ListLabels](./references/repolabels/listlabels.md) | 查询仓库的标签列表。List repository labels. |
| [PatchLabel](./references/repolabels/patchlabel.md) | 更新标签信息。Update label information. |
| [PostLabel](./references/repolabels/postlabel.md) | 创建一个 标签。Create a label. |
### repositories 服务

| 接口 | 描述 |
|------|------|
| [ArchiveRepo](./references/repositories/archiverepo.md) | 仓库归档。Archive a repository. |
| [CreateRepo](./references/repositories/createrepo.md) | 创建仓库。Create repositories. |
| [DeleteRepo](./references/repositories/deleterepo.md) | 删除指定仓库。Delete the specified repository. |
| [GetByID](./references/repositories/getbyid.md) | 获取指定仓库信息。Get information for the specified repository. |
| [GetGroupSubRepos](./references/repositories/getgroupsubrepos.md) | 查询组织下访问用户有权限查看到仓库。List the repositories that the user has access to. |
| [GetPinnedRepoByGroup](./references/repositories/getpinnedrepobygroup.md) | 获取指定组织的仓库墙列表。List the pinned repositories of a group. |
| [GetPinnedRepoByID](./references/repositories/getpinnedrepobyid.md) | 获取指定用户的用户仓库墙。 Get a list of repositories that the specified user has pinned. |
| [GetRepos](./references/repositories/getrepos.md) | 获取当前用户拥有指定权限及其以上权限的仓库。List repositories owned by the current user with the specified permissions or higher. |
| [GetReposByUserName](./references/repositories/getreposbyusername.md) | 获取指定用户有指定以上权限并且客人态可见的仓库。List repositories where the specified user has the specified permission level or higher and are visible to guests. |
| [ListForksRepos](./references/repositories/listforksrepos.md) | 获取指定仓库的 fork 列表。Get fork list for specified repository. |
| [SetPinnedRepoByGroup](./references/repositories/setpinnedrepobygroup.md) | 更新指定组织仓库墙。Update the pinned repositories of a group. |
| [TransferRepo](./references/repositories/transferrepo.md) | 转移仓库。Transfer a repository. |
| [UnArchiveRepo](./references/repositories/unarchiverepo.md) | 解除仓库归档。Unarchive a repository. |
| [UpdateRepo](./references/repositories/updaterepo.md) | 更新仓库信息, 可更新的内容为: 仓库简介, 仓库站点, 仓库主题, 开源许可证。updates repository details including description, website URL,topics and license type. |
### search 服务

| 接口 | 描述 |
|------|------|
| [ListPublicRepos](./references/search/listpublicrepos.md) | Search resource with the key |
### security 服务

| 接口 | 描述 |
|------|------|
| [GetRepoSecurityOverview](./references/security/getreposecurityoverview.md) | 查询仓库安全模块概览数据。Query the security overview data of a repository |
### starring 服务

| 接口 | 描述 |
|------|------|
| [GetUserAllStaredRepos](./references/starring/getuserallstaredrepos.md) | 获取当前用户 star 的仓库列表。List all stared repositories. |
| [GetUserStaredRepos](./references/starring/getuserstaredrepos.md) | 获取指定用户的 star 仓库列表。Get the list of repositories starred by the specified user. |
| [ListStarUsers](./references/starring/liststarusers.md) | 获取指定仓库的star用户列表。Get the list of users who starred the specified repository. |
### users 服务

| 接口 | 描述 |
|------|------|
| [AutoCompleteSource](./references/users/autocompletesource.md) | 查询当前用户用户拥有指定权限的所有资源列表。List resources that the current user has specified permissions for. |
| [GetUserInfo](./references/users/getuserinfo.md) | 获取指定用户的详情信息。Get detailed information for a specified user. |
| [GetUserInfoByName](./references/users/getuserinfobyname.md) | 获取指定用户的详情信息。Get detailed information for a specified user. |
| [ListEmail](./references/users/listemail.md) | 获取用户邮箱列表 |
| [ListGPGKeys](./references/users/listgpgkeys.md) | 获取用户 GPG keys 列表。List GPG Keys. |
| [UpdateUserInfo](./references/users/updateuserinfo.md) | 更新指定用户的详情信息。Updates the specified user's profile information. |
### workspace 服务

| 接口 | 描述 |
|------|------|
| [DeleteWorkspace](./references/workspace/deleteworkspace.md) | 删除我的云原生开发环境。Delete my workspace. |
| [GetWorkspaceDetail](./references/workspace/getworkspacedetail.md) | 根据流水线sn查询云原生开发访问地址。Query cloud-native development access address by pipeline SN. |
| [ListWorkspaces](./references/workspace/listworkspaces.md) | 获取我的云原生开发环境列表。List my workspaces. |
| [StartWorkspace](./references/workspace/startworkspace.md) | 启动云原生开发环境，已存在环境则直接打开，否则重新创建开发环境。Start cloud-native dev. Opens existing env or creates a new one. |
| [WorkspaceStop](./references/workspace/workspacestop.md) | 停止/关闭我的云原生开发环境。Stop/close my workspace. |
## 使用指南

### 1. 获取访问令牌

在调用任何 API 之前，需要先获取有效的 CNB_TOKEN。

### 2. 构造请求

所有请求都需要包含以下基础头信息：

```
Accept: application/vnd.cnb.api+json
Authorization: Bearer $CNB_TOKEN
```

### 3. 处理响应

API 返回标准的 JSON 格式响应。请根据 HTTP 状态码判断请求是否成功：

- 200: 请求成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

## 调用示例

### 基础 GET 请求示例

```bash
curl -w '%header{traceparent}' -X GET \
  "${CNB_API_ENDPOINT}/api/endpoint" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN"
```

### 带参数的 POST 请求示例

```bash
curl -w '%header{traceparent}' -X POST \
  "${CNB_API_ENDPOINT}/api/endpoint" \
  -H "Accept: application/vnd.cnb.api+json" \
  -H "Authorization: Bearer $CNB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

## 注意事项

1. 所有 API 调用都需要有效的认证令牌
2. 请求和响应数据格式为 JSON
3. 请根据具体 API 文档中的参数要求构造请求
4. 建议在生产环境中添加适当的错误处理和重试机制

---

*本文档基于 Swagger 文件自动生成：https://api.cnb.cool/swagger.json*
