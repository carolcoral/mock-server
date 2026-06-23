/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 功能控制器
 *
 * @author carolcoral
 * @since 2026-06-23
 */
@Tag(name = "AI 功能", description = "AI 智能生成等接口")
@RestController
@RequestMapping("/api/ai")
@PreAuthorize("isAuthenticated()")
public class AiMockController {

    @Autowired
    private AiService aiService;

    /**
     * AI 生成 Mock 响应数据
     *
     * @param params 包含 apiMethod, apiPath, apiName, description, count
     * @return 生成的响应数据列表
     */
    @Operation(summary = "AI 智能生成 Mock 响应数据")
    @PostMapping("/generate-response")
    public ApiResponse<List<Map<String, Object>>> generateMockResponse(@RequestBody Map<String, Object> params) {
        String apiMethod = (String) params.get("apiMethod");
        String apiPath = (String) params.get("apiPath");
        String apiName = (String) params.get("apiName");
        String description = (String) params.get("description");
        Integer count = params.get("count") != null ? ((Number) params.get("count")).intValue() : 3;

        if (apiMethod == null || apiMethod.isBlank()) {
            return ApiResponse.error("请提供接口请求方法 (apiMethod)");
        }
        if (apiPath == null || apiPath.isBlank()) {
            return ApiResponse.error("请提供接口路径 (apiPath)");
        }

        try {
            List<Map<String, Object>> responses = aiService.generateMockResponse(
                    apiMethod, apiPath, apiName, description, count);
            return ApiResponse.success(responses);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * AI 生成接口描述
     */
    @Operation(summary = "AI 智能生成接口描述")
    @PostMapping("/generate-description")
    public ApiResponse<String> generateDescription(@RequestBody Map<String, String> params) {
        String apiMethod = params.get("apiMethod");
        String apiPath = params.get("apiPath");
        String apiName = params.get("apiName");

        if (apiMethod == null || apiMethod.isBlank()) {
            return ApiResponse.error("请提供接口请求方法 (apiMethod)");
        }
        if (apiPath == null || apiPath.isBlank()) {
            return ApiResponse.error("请提供接口路径 (apiPath)");
        }

        try {
            String description = aiService.generateApiDescription(apiMethod, apiPath, apiName);
            return ApiResponse.success(description);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
