/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统公告DTO
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Schema(description = "系统公告DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAnnouncementDTO {

    @Schema(description = "公告ID", example = "1")
    private Long id;

    @Schema(description = "公告标题", example = "系统升级通知", required = true)
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "公告内容（支持Markdown）", example = "## 升级内容\n1. 新增XX功能", required = true)
    @NotBlank(message = "公告内容不能为空")
    private String content;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "优先级", example = "NORMAL")
    private String priority;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
