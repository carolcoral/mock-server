/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Mock请求DTO
 *
 * @author carolcoral
 */
@Schema(description = "Mock请求")
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

    @Schema(description = "路径参数（RESTful风格）")
    private Map<String, String> pathParams;

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }
}
