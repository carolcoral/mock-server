package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.repository.MockApiRepository;
import com.carolcoral.mockserver.repository.MockResponseRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import com.carolcoral.mockserver.util.CacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 自定义接口服务类
 *
 * @author carolcoral
 */
@Tag(name = "接口服务", description = "自定义接口业务逻辑处理")
@Slf4j
@Service
@RequiredArgsConstructor
public class MockApiService {

    private final MockApiRepository mockApiRepository;
    private final MockResponseRepository mockResponseRepository;
    private final ProjectRepository projectRepository;
    private final CacheUtil cacheUtil;

    /**
     * 创建接口
     *
     * @param mockApi 接口信息
     * @param userId  创建人ID
     * @return 创建的接口
     */
    @Operation(summary = "创建接口")
    @Transactional
    public ApiResponse<MockApi> createMockApi(@Parameter(description = "接口信息") MockApi mockApi, @Parameter(description = "创建人ID") Long userId) {
        try {
            // 检查项目是否存在
            Long projectId = mockApi.getProject() != null ? mockApi.getProject().getId() : null;
            if (projectId == null) {
                return ApiResponse.error("项目ID不能为空");
            }

            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                return ApiResponse.error("项目不存在");
            }

            mockApi.setProject(projectOpt.get());

            // 检查接口路径和请求方法是否已存在
            if (mockApiRepository.existsByPathAndMethod(mockApi.getPath(), mockApi.getMethod())) {
                return ApiResponse.error("接口路径和请求方法已存在");
            }

            // 设置创建人
            mockApi.setCreateUserId(userId);

            // 设置默认值
            if (mockApi.getEnabled() == null) {
                mockApi.setEnabled(true);
            }
            if (mockApi.getEnableRandom() == null) {
                mockApi.setEnableRandom(false);
            }

            MockApi savedApi = mockApiRepository.save(mockApi);

            // 缓存接口
            cacheUtil.cacheApi(savedApi);

            log.info("创建接口成功: {}/{} {}", savedApi.getProject().getCode(), savedApi.getPath(), savedApi.getMethod());
            return ApiResponse.success(savedApi);

        } catch (Exception e) {
            log.error("创建接口失败: {}", e.getMessage(), e);
            return ApiResponse.error("创建接口失败，请稍后重试");
        }
    }

    /**
     * 更新接口
     *
     * @param mockApi 接口信息
     * @return 更新的接口
     */
    @Operation(summary = "更新接口")
    @Transactional
    public ApiResponse<MockApi> updateMockApi(@Parameter(description = "接口信息") MockApi mockApi) {
        try {
            Optional<MockApi> existingApiOpt = mockApiRepository.findById(mockApi.getId());

            if (!existingApiOpt.isPresent()) {
                return ApiResponse.error("接口不存在");
            }

            MockApi existingApi = existingApiOpt.get();

            // 检查接口路径和请求方法是否被其他接口使用
            if (!existingApi.getPath().equals(mockApi.getPath()) ||
                    !existingApi.getMethod().equals(mockApi.getMethod())) {
                if (mockApiRepository.existsByPathAndMethod(mockApi.getPath(), mockApi.getMethod())) {
                    return ApiResponse.error("接口路径和请求方法已存在");
                }
            }

            // 更新接口信息
            if (mockApi.getName() != null) {
                existingApi.setName(mockApi.getName());
            }
            if (mockApi.getPath() != null) {
                existingApi.setPath(mockApi.getPath());
            }
            if (mockApi.getMethod() != null) {
                existingApi.setMethod(mockApi.getMethod());
            }
            if (mockApi.getRequestType() != null) {
                existingApi.setRequestType(mockApi.getRequestType());
            }
            if (mockApi.getDescription() != null) {
                existingApi.setDescription(mockApi.getDescription());
            }
            if (mockApi.getEnabled() != null) {
                existingApi.setEnabled(mockApi.getEnabled());
            }
            if (mockApi.getResponseDelay() != null) {
                existingApi.setResponseDelay(mockApi.getResponseDelay());
            }
            if (mockApi.getEnableRandom() != null) {
                existingApi.setEnableRandom(mockApi.getEnableRandom());
            }

            MockApi updatedApi = mockApiRepository.save(existingApi);

            // 更新缓存
            cacheUtil.cacheApi(updatedApi);

            log.info("更新接口成功: {}/{} {}", updatedApi.getProject().getCode(), updatedApi.getPath(), updatedApi.getMethod());
            return ApiResponse.success(updatedApi);

        } catch (Exception e) {
            log.error("更新接口失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新接口失败，请稍后重试");
        }
    }

    /**
     * 删除接口
     *
     * @param apiId 接口ID
     * @return 删除结果
     */
    @Operation(summary = "删除接口")
    @Transactional
    public ApiResponse<Void> deleteMockApi(@Parameter(description = "接口ID", example = "1") Long apiId) {
        try {
            if (!mockApiRepository.existsById(apiId)) {
                return ApiResponse.error("接口不存在");
            }

            // 删除接口下的所有响应
            mockResponseRepository.deleteByMockApiId(apiId);

            // 删除接口
            mockApiRepository.deleteById(apiId);

            // 清除缓存
            cacheUtil.evictApiCache(apiId);

            log.info("删除接口成功: {}", apiId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("删除接口失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除接口失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询接口
     *
     * @param apiId 接口ID
     * @return 接口信息
     */
    @Operation(summary = "根据ID查询接口")
    public ApiResponse<MockApi> getMockApiById(@Parameter(description = "接口ID", example = "1") Long apiId) {
        try {
            Optional<MockApi> apiOpt = mockApiRepository.findById(apiId);

            if (!apiOpt.isPresent()) {
                return ApiResponse.error("接口不存在");
            }

            return ApiResponse.success(apiOpt.get());

        } catch (Exception e) {
            log.error("查询接口失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询接口失败，请稍后重试");
        }
    }

    /**
     * 根据接口路径和请求方法查询接口
     *
     * @param path   接口路径
     * @param method 请求方法
     * @return 接口信息
     */
    @Operation(summary = "根据接口路径和请求方法查询接口")
    public ApiResponse<MockApi> getMockApiByPathAndMethod(@Parameter(description = "接口路径", example = "/api/user") String path,
                                                           @Parameter(description = "请求方法", example = "GET") MockApi.HttpMethod method) {
        try {
            Optional<MockApi> apiOpt = cacheUtil.getApiFromCache(path, method);

            if (!apiOpt.isPresent()) {
                return ApiResponse.error("接口不存在");
            }

            return ApiResponse.success(apiOpt.get());

        } catch (Exception e) {
            log.error("查询接口失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询接口失败，请稍后重试");
        }
    }

    /**
     * 根据项目ID查询接口列表
     *
     * @param projectId 项目ID
     * @return 接口列表
     */
    @Operation(summary = "根据项目ID查询接口列表")
    public ApiResponse<List<MockApi>> getMockApisByProjectId(@Parameter(description = "项目ID", example = "1") Long projectId) {
        try {
            List<MockApi> apis = cacheUtil.getProjectApisFromCache(projectId);

            if (apis == null) {
                apis = mockApiRepository.findByProjectId(projectId);
                cacheUtil.cacheProjectApis(projectId, apis);
            }

            return ApiResponse.success(apis);

        } catch (Exception e) {
            log.error("查询接口列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询接口列表失败，请稍后重试");
        }
    }

    /**
     * 查询所有接口
     *
     * @return 接口列表
     */
    @Operation(summary = "查询所有接口")
    public ApiResponse<List<MockApi>> getAllMockApis() {
        try {
            List<MockApi> apis = mockApiRepository.findAll();
            return ApiResponse.success(apis);

        } catch (Exception e) {
            log.error("查询接口列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询接口列表失败，请稍后重试");
        }
    }

    /**
     * 查询启用状态的接口
     *
     * @return 接口列表
     */
    @Operation(summary = "查询启用状态的接口")
    public ApiResponse<List<MockApi>> getEnabledMockApis() {
        try {
            List<MockApi> apis = mockApiRepository.findByEnabled(true);
            return ApiResponse.success(apis);

        } catch (Exception e) {
            log.error("查询接口列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询接口列表失败，请稍后重试");
        }
    }

    /**
     * 切换接口状态
     *
     * @param apiId 接口ID
     * @return 操作结果
     */
    @Operation(summary = "切换接口状态")
    @Transactional
    public ApiResponse<MockApi> toggleApiStatus(@Parameter(description = "接口ID", example = "1") Long apiId) {
        try {
            Optional<MockApi> apiOpt = mockApiRepository.findById(apiId);

            if (!apiOpt.isPresent()) {
                return ApiResponse.error("接口不存在");
            }

            MockApi api = apiOpt.get();
            api.setEnabled(!api.getEnabled());

            MockApi updatedApi = mockApiRepository.save(api);

            // 更新缓存
            cacheUtil.cacheApi(updatedApi);

            log.info("切换接口状态成功: {} -> {}", apiId, updatedApi.getEnabled());
            return ApiResponse.success(updatedApi);

        } catch (Exception e) {
            log.error("切换接口状态失败: {}", e.getMessage(), e);
            return ApiResponse.error("切换接口状态失败，请稍后重试");
        }
    }

    /**
     * 添加接口响应
     *
     * @param apiId         接口ID
     * @param mockResponse  响应信息
     * @return 添加的响应
     */
    @Operation(summary = "添加接口响应")
    @Transactional
    public ApiResponse<MockResponse> addApiResponse(@Parameter(description = "接口ID", example = "1") Long apiId,
                                                   @Parameter(description = "响应信息") MockResponse mockResponse) {
        try {
            Optional<MockApi> apiOpt = mockApiRepository.findById(apiId);

            if (!apiOpt.isPresent()) {
                return ApiResponse.error("接口不存在");
            }

            mockResponse.setMockApi(apiOpt.get());

            // 设置默认值
            if (mockResponse.getWeight() == null) {
                mockResponse.setWeight(100);
            }
            if (mockResponse.getEnabled() == null) {
                mockResponse.setEnabled(true);
            }

            MockResponse savedResponse = mockResponseRepository.save(mockResponse);

            // 更新接口响应缓存
            List<MockResponse> responses = mockResponseRepository.findByMockApiId(apiId);
            cacheUtil.cacheApiResponses(apiId, responses);

            log.info("添加接口响应成功: 接口={}, 状态码={}", apiId, savedResponse.getStatusCode());
            return ApiResponse.success(savedResponse);

        } catch (Exception e) {
            log.error("添加接口响应失败: {}", e.getMessage(), e);
            return ApiResponse.error("添加接口响应失败，请稍后重试");
        }
    }

    /**
     * 更新接口响应
     *
     * @param mockResponse 响应信息
     * @return 更新的响应
     */
    @Operation(summary = "更新接口响应")
    @Transactional
    public ApiResponse<MockResponse> updateApiResponse(@Parameter(description = "响应信息") MockResponse mockResponse) {
        try {
            Optional<MockResponse> existingResponseOpt = mockResponseRepository.findById(mockResponse.getId());

            if (!existingResponseOpt.isPresent()) {
                return ApiResponse.error("响应不存在");
            }

            MockResponse existingResponse = existingResponseOpt.get();

            // 更新响应信息
            if (mockResponse.getStatusCode() != null) {
                existingResponse.setStatusCode(mockResponse.getStatusCode());
            }
            if (mockResponse.getContentType() != null) {
                existingResponse.setContentType(mockResponse.getContentType());
            }
            if (mockResponse.getHeaders() != null) {
                existingResponse.setHeaders(mockResponse.getHeaders());
            }
            if (mockResponse.getResponseBody() != null) {
                existingResponse.setResponseBody(mockResponse.getResponseBody());
            }
            if (mockResponse.getWeight() != null) {
                existingResponse.setWeight(mockResponse.getWeight());
            }
            if (mockResponse.getCondition() != null) {
                existingResponse.setCondition(mockResponse.getCondition());
            }
            if (mockResponse.getConditionDesc() != null) {
                existingResponse.setConditionDesc(mockResponse.getConditionDesc());
            }
            if (mockResponse.getEnabled() != null) {
                existingResponse.setEnabled(mockResponse.getEnabled());
            }

            MockResponse updatedResponse = mockResponseRepository.save(existingResponse);

            // 更新接口响应缓存
            List<MockResponse> responses = mockResponseRepository.findByMockApiId(existingResponse.getMockApi().getId());
            cacheUtil.cacheApiResponses(existingResponse.getMockApi().getId(), responses);

            log.info("更新接口响应成功: 响应ID={}", updatedResponse.getId());
            return ApiResponse.success(updatedResponse);

        } catch (Exception e) {
            log.error("更新接口响应失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新接口响应失败，请稍后重试");
        }
    }

    /**
     * 删除接口响应
     *
     * @param responseId 响应ID
     * @return 删除结果
     */
    @Operation(summary = "删除接口响应")
    @Transactional
    public ApiResponse<Void> deleteApiResponse(@Parameter(description = "响应ID", example = "1") Long responseId) {
        try {
            Optional<MockResponse> responseOpt = mockResponseRepository.findById(responseId);

            if (!responseOpt.isPresent()) {
                return ApiResponse.error("响应不存在");
            }

            MockResponse response = responseOpt.get();
            Long apiId = response.getMockApi().getId();

            mockResponseRepository.deleteById(responseId);

            // 更新接口响应缓存
            List<MockResponse> responses = mockResponseRepository.findByMockApiId(apiId);
            cacheUtil.cacheApiResponses(apiId, responses);

            log.info("删除接口响应成功: {}", responseId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("删除接口响应失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除接口响应失败，请稍后重试");
        }
    }

    /**
     * 设置接口的激活响应
     *
     * @param apiId      接口ID
     * @param responseId 响应ID
     * @return 操作结果
     */
    @Operation(summary = "设置接口的激活响应")
    @Transactional
    public ApiResponse<Void> setActiveResponse(@Parameter(description = "接口ID", example = "1") Long apiId,
                                              @Parameter(description = "响应ID", example = "1") Long responseId) {
        try {
            // 取消该接口的所有响应的激活状态
            List<MockResponse> responses = mockResponseRepository.findByMockApiId(apiId);
            for (MockResponse response : responses) {
                response.setActive(false);
            }
            mockResponseRepository.saveAll(responses);

            // 设置指定的响应为激活状态
            Optional<MockResponse> targetResponseOpt = mockResponseRepository.findById(responseId);
            if (!targetResponseOpt.isPresent()) {
                return ApiResponse.error("响应不存在");
            }

            MockResponse targetResponse = targetResponseOpt.get();
            targetResponse.setActive(true);
            mockResponseRepository.save(targetResponse);

            // 更新接口响应缓存
            List<MockResponse> updatedResponses = mockResponseRepository.findByMockApiId(apiId);
            cacheUtil.cacheApiResponses(apiId, updatedResponses);

            log.info("设置激活响应成功: 接口={}, 响应={}", apiId, responseId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("删除接口响应失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除接口响应失败，请稍后重试");
        }
    }
}
