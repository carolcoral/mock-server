/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Mock响应DTO
 *
 * @author carolcoral
 */
@Schema(description = "Mock响应")
public class MockResponseDTO {

    @Schema(description = "HTTP状态码", example = "200")
    private Integer statusCode;

    @Schema(description = "响应头")
    private Map<String, String> headers;

    @Schema(description = "响应体")
    private Object body;

    @Schema(description = "响应延迟（毫秒）", example = "100")
    private Integer delay;

    /**
     * 默认构造器
     */
    public MockResponseDTO() {
    }

    /**
     * 全参构造器
     */
    public MockResponseDTO(Integer statusCode, Map<String, String> headers, Object body, Integer delay) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.delay = delay;
    }

    // Getters and Setters
    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    /**
     * Builder方法
     */
    public static MockResponseDTOBuilder builder() {
        return new MockResponseDTOBuilder();
    }

    public static class MockResponseDTOBuilder {
        private Integer statusCode;
        private Map<String, String> headers;
        private Object body;
        private Integer delay;

        public MockResponseDTOBuilder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public MockResponseDTOBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public MockResponseDTOBuilder body(Object body) {
            this.body = body;
            return this;
        }

        public MockResponseDTOBuilder delay(Integer delay) {
            this.delay = delay;
            return this;
        }

        public MockResponseDTO build() {
            return new MockResponseDTO(statusCode, headers, body, delay);
        }
    }
}
