/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.AiConfig;
import com.carolcoral.mockserver.service.AiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 配置控制器
 *
 * @author carolcoral
 * @since 2026-06-23
 */
@Tag(name = "AI配置", description = "AI 服务商配置管理接口")
@RestController
@RequestMapping("/api/ai-config")
@PreAuthorize("hasRole('ADMIN')")
public class AiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    @Operation(summary = "获取预设服务商列表")
    @GetMapping("/preset-providers")
    public ApiResponse<Map<String, Map<String, String>>> getPresetProviders() {
        return ApiResponse.success(aiConfigService.getPresetProviders());
    }

    @Operation(summary = "获取所有 AI 配置")
    @GetMapping
    public ApiResponse<java.util.List<AiConfig>> getAllConfigs() {
        return ApiResponse.success(aiConfigService.getAllConfigs());
    }

    @Operation(summary = "保存或更新 AI 配置")
    @PostMapping
    public ApiResponse<AiConfig> saveConfig(@RequestBody AiConfig config) {
        return ApiResponse.success(aiConfigService.saveConfig(config));
    }

    @Operation(summary = "切换启用状态")
    @PutMapping("/{id}/toggle")
    public ApiResponse<AiConfig> toggleEnabled(@PathVariable Long id) {
        AiConfig config = aiConfigService.getById(id);
        return ApiResponse.success(aiConfigService.toggleEnabled(id, !config.getEnabled()));
    }

    @Operation(summary = "按服务商标识获取配置")
    @GetMapping("/{provider}")
    public ApiResponse<AiConfig> getByProvider(@PathVariable String provider) {
        AiConfig config = aiConfigService.getByProvider(provider);
        if (config == null) {
            return ApiResponse.error("未找到该服务商配置");
        }
        return ApiResponse.success(config);
    }

    @Operation(summary = "连通性验证 - 测试 AI 服务商配置是否可用")
    @PostMapping("/test-connectivity")
    public ApiResponse<Map<String, Object>> testConnectivity(@RequestBody Map<String, String> params) {
        String apiUrl = params.get("apiUrl");
        String apiKey = params.get("apiKey");
        String model = params.get("defaultModel");

        if (apiUrl == null || apiUrl.isBlank()) {
            return ApiResponse.error("API 地址不能为空");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return ApiResponse.error("API Key 不能为空");
        }

        Map<String, Object> result = aiConfigService.testConnectivity(apiUrl, apiKey, model);
        return ApiResponse.success(result);
    }
}
