/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * Mock请求DTO
 *
 * @author carolcoral
 */
@Schema(description = "Mock请求")
@Data
public class MockRequest {

    @Schema(description = "请求路径", example = "/api/user/login")
    private String path;

    @Schema(description = "请求方法", example = "POST")
    private String method;

    @Schema(description = "请求头")
    private Map<String, String> headers;

    @Schema(description = "请求参数")
    private Map<String, Object> params;

    @Schema(description = "请求体")
    private Object body;

    @Schema(description = "项目编码", example = "ecmall")
    private String projectCode;
}
