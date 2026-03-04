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

import java.util.Map;

/**
 * Mock响应DTO
 *
 * @author carolcoral
 */
@Schema(description = "Mock响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResponseDTO {

    @Schema(description = "HTTP状态码", example = "200")
    private Integer statusCode;

    @Schema(description = "响应头")
    private Map<String, String> headers;

    @Schema(description = "响应体")
    private Object body;

    @Schema(description = "响应延迟（毫秒）", example = "100")
    private Integer delay;
}
