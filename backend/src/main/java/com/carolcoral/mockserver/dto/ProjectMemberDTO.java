/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import com.carolcoral.mockserver.entity.ProjectMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员DTO
 * 用于返回包含用户信息的项目成员数据
 *
 * @author carolcoral
 */
@Schema(description = "项目成员DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {

    @Schema(description = "成员ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "成员角色")
    private ProjectMember.MemberRole role;

    @Schema(description = "加入时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
