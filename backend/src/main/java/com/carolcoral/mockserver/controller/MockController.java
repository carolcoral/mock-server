/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.MockRequest;
import com.carolcoral.mockserver.dto.MockResponseDTO;
import com.carolcoral.mockserver.service.MockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock请求控制器 - 核心控制器
 * 处理所有Mock请求，不需要认证
 *
 * @author carolcoral
 */
@Tag(name = "Mock请求", description = "Mock请求处理接口（无需认证）")
@Slf4j
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
@Validated
public class MockController {

    private final MockService mockService;

    // 最大响应延迟时间（毫秒）
    private static final int MAX_RESPONSE_DELAY = 5000;

    /**
     * 处理所有Mock请求
     *
     * @param projectCode 项目编码
     * @param request     HTTP请求
     * @return Mock响应
     */
    @Operation(summary = "处理Mock请求", description = "处理所有Mock请求，支持所有HTTP方法和路径（无需认证）")
    @RequestMapping(value = "/{projectCode}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
    public ResponseEntity<Object> handleMockRequest(
            @Parameter(description = "项目编码", example = "ecmall")
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "项目编码只能包含字母、数字、下划线和连字符") String projectCode,
            HttpServletRequest request) {
        
        try {
            // 获取请求路径（去除项目编码前缀）
            String fullPath = request.getRequestURI();
            String path = fullPath.replace("/api/mock/" + projectCode, "");
            if (path.isEmpty()) {
                path = "/";
            }

            String method = request.getMethod();
            
            log.info("Mock请求: {} {} 项目: {}", method, path, projectCode);

            // 构建Mock请求
            MockRequest mockRequest = buildMockRequest(projectCode, path, method, request);

            // 处理请求
            MockResponseDTO mockResponse = mockService.handleMockRequest(mockRequest);

            // 构建HTTP响应
            return buildHttpResponse(mockResponse);

        } catch (Exception e) {
            log.error("处理Mock请求失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorBody(500, "服务器内部错误"));
        }
    }

    /**
     * 构建Mock请求
     *
     * @param projectCode 项目编码
     * @param path        请求路径
     * @param method      请求方法
     * @param request     HTTP请求
     * @return Mock请求
     */
    private MockRequest buildMockRequest(String projectCode, String path, String method, HttpServletRequest request) {
        MockRequest mockRequest = new MockRequest();
        mockRequest.setProjectCode(projectCode);
        mockRequest.setPath(path);
        mockRequest.setMethod(method);

        // 获取请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        mockRequest.setHeaders(headers);

        // 获取请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values.length == 1) {
                params.put(entry.getKey(), values[0]);
            } else {
                params.put(entry.getKey(), values);
            }
        }
        mockRequest.setParams(params);

        // TODO: 获取请求体（需要读取request的inputStream）
        // 这里简化处理，实际需要配置HttpServletRequest的包装类来多次读取body

        return mockRequest;
    }

    /**
     * 构建HTTP响应
     *
     * @param mockResponse Mock响应
     * @return HTTP响应
     */
    private ResponseEntity<Object> buildHttpResponse(MockResponseDTO mockResponse) {
        // 设置延迟（如果配置了）
        if (mockResponse.getDelay() != null && mockResponse.getDelay() > 0) {
            try {
                // 限制最大延迟时间，防止DoS攻击
                long delay = Math.min(mockResponse.getDelay(), MAX_RESPONSE_DELAY);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("响应延迟被中断");
            }
        }

        // 构建响应头
        HttpHeaders headers = new HttpHeaders();
        if (mockResponse.getHeaders() != null) {
            mockResponse.getHeaders().forEach(headers::add);
        }

        // 默认Content-Type
        if (!headers.containsKey("Content-Type")) {
            headers.add("Content-Type", "application/json;charset=UTF-8");
        }

        // 构建响应
        return ResponseEntity.status(mockResponse.getStatusCode())
                .headers(headers)
                .body(mockResponse.getBody());
    }

    /**
     * 创建错误响应体
     *
     * @param statusCode 状态码
     * @param message    错误消息
     * @return 错误响应体
     */
    private Map<String, Object> createErrorBody(int statusCode, String message) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", statusCode);
        errorBody.put("message", message);
        errorBody.put("timestamp", System.currentTimeMillis());
        return errorBody;
    }

    /**
     * 处理GET请求
     */
    @Operation(summary = "处理GET Mock请求", description = "处理GET方法的Mock请求（无需认证）")
    @GetMapping("/{projectCode}/**")
    public ResponseEntity<Object> handleGetRequest(
            @Parameter(description = "项目编码", example = "ecmall") @PathVariable String projectCode,
            HttpServletRequest request) {
        return handleMockRequest(projectCode, request);
    }

    /**
     * 处理POST请求
     */
    @Operation(summary = "处理POST Mock请求", description = "处理POST方法的Mock请求（无需认证）")
    @PostMapping("/{projectCode}/**")
    public ResponseEntity<Object> handlePostRequest(
            @Parameter(description = "项目编码", example = "ecmall") @PathVariable String projectCode,
            HttpServletRequest request) {
        return handleMockRequest(projectCode, request);
    }

    /**
     * 处理PUT请求
     */
    @Operation(summary = "处理PUT Mock请求", description = "处理PUT方法的Mock请求（无需认证）")
    @PutMapping("/{projectCode}/**")
    public ResponseEntity<Object> handlePutRequest(
            @Parameter(description = "项目编码", example = "ecmall") @PathVariable String projectCode,
            HttpServletRequest request) {
        return handleMockRequest(projectCode, request);
    }

    /**
     * 处理DELETE请求
     */
    @Operation(summary = "处理DELETE Mock请求", description = "处理DELETE方法的Mock请求（无需认证）")
    @DeleteMapping("/{projectCode}/**")
    public ResponseEntity<Object> handleDeleteRequest(
            @Parameter(description = "项目编码", example = "ecmall") @PathVariable String projectCode,
            HttpServletRequest request) {
        return handleMockRequest(projectCode, request);
    }

    /**
     * 处理PATCH请求
     */
    @Operation(summary = "处理PATCH Mock请求", description = "处理PATCH方法的Mock请求（无需认证）")
    @PatchMapping("/{projectCode}/**")
    public ResponseEntity<Object> handlePatchRequest(
            @Parameter(description = "项目编码", example = "ecmall") @PathVariable String projectCode,
            HttpServletRequest request) {
        return handleMockRequest(projectCode, request);
    }
}
