/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果DTO
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Schema(description = "分页结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "当前页码（从0开始）", example = "0")
    private int page;

    @Schema(description = "每页大小", example = "15")
    private int size;

    @Schema(description = "总记录数", example = "100")
    private long totalElements;

    @Schema(description = "总页数", example = "7")
    private int totalPages;

    @Schema(description = "是否为最后一页", example = "false")
    private boolean last;

    @Schema(description = "是否为第一页", example = "true")
    private boolean first;
}
