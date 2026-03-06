/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.service.MockApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义接口控制器
 *
 * @author carolcoral
 */
@Tag(name = "接口管理", description = "自定义接口管理相关接口")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/mock-apis")
public class MockApiController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MockApiController.class);

    /**
     * 构造器
     */
    public MockApiController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    private final MockApiService mockApiService;

    /**
     * 创建接口
     *
     * @param mockApi 接口信息
     * @return 创建的接口
     */
    @Operation(summary = "创建接口", description = "创建新的自定义接口")
    @PostMapping
    public ApiResponse<MockApi> createMockApi(@Parameter(description = "接口信息") @Valid @RequestBody MockApi mockApi) {
        log.info("创建接口请求: {}", mockApi.getName());
        return mockApiService.createMockApi(mockApi, 1L); // TODO: 从认证信息中获取用户ID
    }

    /**
     * 更新接口
     *
     * @param mockApi 接口信息
     * @return 更新的接口
     */
    @Operation(summary = "更新接口", description = "更新自定义接口信息")
    @PutMapping
    public ApiResponse<MockApi> updateMockApi(@Parameter(description = "接口信息") @Valid @RequestBody MockApi mockApi) {
        log.info("更新接口请求: {}", mockApi.getId());
        return mockApiService.updateMockApi(mockApi);
    }

    /**
     * 删除接口
     *
     * @param apiId 接口ID
     * @return 删除结果
     */
    @Operation(summary = "删除接口", description = "删除自定义接口")
    @DeleteMapping("/{apiId}")
    public ApiResponse<Void> deleteMockApi(@Parameter(description = "接口ID", example = "1") @PathVariable Long apiId) {
        log.info("删除接口请求: {}", apiId);
        return mockApiService.deleteMockApi(apiId);
    }

    /**
     * 根据ID查询接口
     *
     * @param apiId 接口ID
     * @return 接口信息
     */
    @Operation(summary = "根据ID查询接口", description = "根据接口ID查询接口信息")
    @GetMapping("/{apiId}")
    public ApiResponse<MockApi> getMockApiById(@Parameter(description = "接口ID", example = "1") @PathVariable Long apiId) {
        log.info("查询接口请求: {}", apiId);
        return mockApiService.getMockApiById(apiId);
    }

    /**
     * 根据接口路径和请求方法查询接口
     *
     * @param path   接口路径
     * @param method 请求方法
     * @return 接口信息
     */
    @Operation(summary = "根据接口路径和请求方法查询接口", description = "根据接口路径和请求方法查询接口信息")
    @GetMapping("/path")
    public ApiResponse<MockApi> getMockApiByPathAndMethod(
            @Parameter(description = "接口路径", example = "/api/user") @RequestParam String path,
            @Parameter(description = "请求方法", example = "GET") @RequestParam MockApi.HttpMethod method) {
        log.info("查询接口请求: {} {}", method, path);
        return mockApiService.getMockApiByPathAndMethod(path, method);
    }

    /**
     * 根据项目ID查询接口列表
     *
     * @param projectId 项目ID
     * @return 接口列表
     */
    @Operation(summary = "根据项目ID查询接口列表", description = "根据项目ID查询该项目下的所有接口")
    @GetMapping("/project/{projectId}")
    public ApiResponse<java.util.List<MockApi>> getMockApisByProjectId(
            @Parameter(description = "项目ID", example = "1") @PathVariable Long projectId) {
        log.info("查询项目接口列表请求: 项目={}", projectId);
        return mockApiService.getMockApisByProjectId(projectId);
    }

    /**
     * 查询所有接口
     *
     * @return 接口列表
     */
    @Operation(summary = "查询所有接口", description = "查询所有自定义接口列表")
    @GetMapping
    public ApiResponse<java.util.List<MockApi>> getAllMockApis() {
        log.info("查询所有接口请求");
        return mockApiService.getAllMockApis();
    }

    /**
     * 查询启用状态的接口
     *
     * @return 接口列表
     */
    @Operation(summary = "查询启用状态的接口", description = "查询所有启用状态的自定义接口")
    @GetMapping("/enabled")
    public ApiResponse<java.util.List<MockApi>> getEnabledMockApis() {
        log.info("查询启用状态接口请求");
        return mockApiService.getEnabledMockApis();
    }

    /**
     * 切换接口状态
     *
     * @param apiId 接口ID
     * @return 操作结果
     */
    @Operation(summary = "切换接口状态", description = "切换接口的启用/禁用状态")
    @PutMapping("/{apiId}/toggle")
    public ApiResponse<MockApi> toggleApiStatus(
            @Parameter(description = "接口ID", example = "1") @PathVariable Long apiId) {
        log.info("切换接口状态请求: {}", apiId);
        return mockApiService.toggleApiStatus(apiId);
    }

    /**
     * 添加接口响应
     *
     * @param apiId        接口ID
     * @param mockResponse 响应信息
     * @return 添加的响应
     */
    @Operation(summary = "添加接口响应", description = "为接口添加响应配置")
    @PostMapping("/{apiId}/responses")
    public ApiResponse<MockResponse> addApiResponse(
            @Parameter(description = "接口ID", example = "1") @PathVariable Long apiId,
            @Parameter(description = "响应信息") @Valid @RequestBody MockResponse mockResponse) {
        log.info("添加接口响应请求: 接口={}", apiId);
        return mockApiService.addApiResponse(apiId, mockResponse);
    }

    /**
     * 更新接口响应
     *
     * @param mockResponse 响应信息
     * @return 更新的响应
     */
    @Operation(summary = "更新接口响应", description = "更新接口响应配置")
    @PutMapping("/responses")
    public ApiResponse<MockResponse> updateApiResponse(
            @Parameter(description = "响应信息") @Valid @RequestBody MockResponse mockResponse) {
        log.info("更新接口响应请求: {}", mockResponse.getId());
        return mockApiService.updateApiResponse(mockResponse);
    }

    /**
     * 删除接口响应
     *
     * @param responseId 响应ID
     * @return 删除结果
     */
    @Operation(summary = "删除接口响应", description = "删除接口响应配置")
    @DeleteMapping("/responses/{responseId}")
    public ApiResponse<Void> deleteApiResponse(
            @Parameter(description = "响应ID", example = "1") @PathVariable Long responseId) {
        log.info("删除接口响应请求: {}", responseId);
        return mockApiService.deleteApiResponse(responseId);
    }

    /**
     * 设置接口的激活响应
     *
     * @param apiId      接口ID
     * @param responseId 响应ID
     * @return 操作结果
     */
    @Operation(summary = "设置接口的激活响应", description = "设置接口的当前激活响应")
    @PostMapping("/{apiId}/responses/{responseId}/active")
    public ApiResponse<Void> setActiveResponse(
            @Parameter(description = "接口ID", example = "1") @PathVariable Long apiId,
            @Parameter(description = "响应ID", example = "1") @PathVariable Long responseId) {
        log.info("设置激活响应请求: 接口={}, 响应={}", apiId, responseId);
        return mockApiService.setActiveResponse(apiId, responseId);
    }

    /**
     * 获取接口的所有响应
     *
     * @param apiId 接口ID
     * @return 响应列表
     */
    @Operation(summary = "获取接口的所有响应", description = "获取指定接口的所有响应配置")
    @GetMapping("/{apiId}/responses")
    public ApiResponse<java.util.List<MockResponse>> getApiResponses(
            @Parameter(description = "接口ID", example = "1") @PathVariable Long apiId) {
        log.info("获取接口响应列表请求: 接口={}", apiId);
        return mockApiService.getApiResponses(apiId);
    }
}
