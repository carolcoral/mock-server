package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.MockRequest;
import com.carolcoral.mockserver.dto.MockResponseDTO;
import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.util.CacheUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock服务类 - 核心服务
 *
 * @author carolcoral
 */
@Tag(name = "Mock服务", description = "Mock请求处理核心服务")
@Service
public class MockService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MockService.class);

    /**
     * 构造器
     */
    public MockService(CacheUtil cacheUtil, ObjectMapper objectMapper, RequestLogService requestLogService) {
        this.cacheUtil = cacheUtil;
        this.objectMapper = objectMapper;
        this.requestLogService = requestLogService;
    }

    private final CacheUtil cacheUtil;
    private final ObjectMapper objectMapper;
    private final RequestLogService requestLogService;

    /**
     * 处理Mock请求
     *
     * @param mockRequest Mock请求
     * @return Mock响应
     */
    @Operation(summary = "处理Mock请求")
    public MockResponseDTO handleMockRequest(@Parameter(description = "Mock请求") MockRequest mockRequest) {
        return handleMockRequest(mockRequest, null);
    }

    /**
     * 处理Mock请求（带请求日志）
     *
     * @param mockRequest Mock请求
     * @param request HTTP请求对象（可选，用于记录日志）
     * @return Mock响应
     */
    @Operation(summary = "处理Mock请求（带请求日志）")
    public MockResponseDTO handleMockRequest(@Parameter(description = "Mock请求") MockRequest mockRequest, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        MockApi matchedApi = null;
        Integer statusCode = 200;
        try {
            log.info("处理Mock请求: {} {} 项目: {}", mockRequest.getMethod(), mockRequest.getPath(), mockRequest.getProjectCode());

            // 查找项目
            Optional<Project> projectOpt = cacheUtil.getProjectFromCache(mockRequest.getProjectCode());
            if (!projectOpt.isPresent()) {
                statusCode = 404;
                return createErrorResponse(404, "项目不存在: " + mockRequest.getProjectCode());
            }

            Project project = projectOpt.get();
            if (!project.getEnabled()) {
                statusCode = 503;
                return createErrorResponse(503, "项目已禁用");
            }

            // 查找接口
            Optional<MockApi> apiOpt = findMockApi(project.getId(), mockRequest.getPath(), mockRequest.getMethod());
            if (!apiOpt.isPresent()) {
                statusCode = 404;
                return createErrorResponse(404, "接口不存在: " + mockRequest.getPath());
            }

            MockApi mockApi = apiOpt.get();
            matchedApi = mockApi;

            if (!mockApi.getEnabled()) {
                statusCode = 503;
                return createErrorResponse(503, "接口已禁用");
            }

            // 获取接口响应列表
            List<MockResponse> responses = cacheUtil.getApiResponsesFromCache(mockApi.getId());
            if (responses == null || responses.isEmpty()) {
                statusCode = 500;
                return createErrorResponse(500, "接口未配置响应");
            }

            // 过滤启用状态的响应
            List<MockResponse> enabledResponses = new ArrayList<>();
            for (MockResponse response : responses) {
                if (response.getEnabled() != null && response.getEnabled()) {
                    enabledResponses.add(response);
                }
            }

            if (enabledResponses.isEmpty()) {
                statusCode = 500;
                return createErrorResponse(500, "接口未配置启用状态的响应");
            }

            // 根据条件匹配响应
            MockResponse matchedResponse = matchResponseByCondition(enabledResponses, mockRequest);
            if (matchedResponse != null) {
                MockResponseDTO responseDTO = buildMockResponse(mockApi, matchedResponse);
                statusCode = responseDTO.getStatusCode();
                return responseDTO;
            }

            // 如果启用了随机返回，从所有启用且激活的响应中按权重随机选择
            if (mockApi.getEnableRandom() != null && mockApi.getEnableRandom()) {
                // 过滤出启用且激活的响应
                List<MockResponse> enabledActiveResponses = new ArrayList<>();
                for (MockResponse response : enabledResponses) {
                    if (response.getActive() != null && response.getActive()) {
                        enabledActiveResponses.add(response);
                    }
                }

                if (!enabledActiveResponses.isEmpty()) {
                    MockResponse randomResponse = selectRandomResponse(enabledActiveResponses);
                    if (randomResponse != null) {
                        log.info("启用随机返回，从激活响应中随机选择: 状态码={}", randomResponse.getStatusCode());
                        MockResponseDTO responseDTO = buildMockResponse(mockApi, randomResponse);
                        statusCode = responseDTO.getStatusCode();
                        return responseDTO;
                    }
                }
            }

            // 优先返回激活的响应
            MockResponse activeResponse = enabledResponses.stream()
                    .filter(r -> r.getActive() != null && r.getActive())
                    .findFirst()
                    .orElse(null);
            if (activeResponse != null) {
                log.info("返回激活响应: 状态码={}", activeResponse.getStatusCode());
                MockResponseDTO responseDTO = buildMockResponse(mockApi, activeResponse);
                statusCode = responseDTO.getStatusCode();
                return responseDTO;
            }

            // 默认返回第一个响应（通常是200状态码）
            MockResponse defaultResponse = enabledResponses.get(0);
            MockResponseDTO responseDTO = buildMockResponse(mockApi, defaultResponse);
            statusCode = responseDTO.getStatusCode();
            return responseDTO;

        } catch (Exception e) {
            log.error("处理Mock请求失败", e);
            statusCode = 500;
            return createErrorResponse(500, "服务器内部错误");
        } finally {
            // 异步记录请求日志
            if (request != null && matchedApi != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                Long userId = null;
                // TODO: 从请求中获取用户ID
                requestLogService.logRequestAsync(matchedApi, request, statusCode, responseTime, userId);
            }
        }
    }

    /**
     * 查找Mock接口
     *
     * @param projectId 项目ID
     * @param path      接口路径
     * @param method    请求方法
     * @return 接口Optional
     */
    private Optional<MockApi> findMockApi(Long projectId, String path, String method) {
        try {
            MockApi.HttpMethod httpMethod = MockApi.HttpMethod.valueOf(method.toUpperCase());
            return cacheUtil.getApiFromCache(path, httpMethod);
        } catch (Exception e) {
            log.error("查找接口失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 根据条件匹配响应
     *
     * @param responses   响应列表
     * @param mockRequest Mock请求
     * @return 匹配的响应
     */
    private MockResponse matchResponseByCondition(List<MockResponse> responses, MockRequest mockRequest) {
        for (MockResponse response : responses) {
            if (response.getCondition() != null && !response.getCondition().isEmpty()) {
                try {
                    if (evaluateCondition(response.getCondition(), mockRequest)) {
                        log.info("根据条件匹配到响应: {}", response.getCondition());
                        return response;
                    }
                } catch (Exception e) {
                    log.warn("条件表达式评估失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 评估条件表达式
     *
     * @param condition   条件表达式
     * @param mockRequest Mock请求
     * @return 是否匹配
     */
    private boolean evaluateCondition(String condition, MockRequest mockRequest) {
        try {
            // 支持简单的JSONPath表达式
            // 例如: $.userId == '123' 或 $.status == 'active'
            
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                if (parts.length == 2) {
                    String jsonPath = parts[0].trim();
                    String expectedValue = parts[1].trim().replace("'", "").replace("\"", "");
                    
                    Object actualValue = null;
                    
                    // 从请求体中获取值
                    if (mockRequest.getBody() != null) {
                        try {
                            String jsonBody = objectMapper.writeValueAsString(mockRequest.getBody());
                            actualValue = JsonPath.read(jsonBody, jsonPath);
                        } catch (Exception e) {
                            log.debug("从请求体获取值失败: {}", e.getMessage());
                        }
                    }
                    
                    // 从请求参数中获取值
                    if (actualValue == null && mockRequest.getParams() != null) {
                        String paramName = jsonPath.replace("$.", "");
                        actualValue = mockRequest.getParams().get(paramName);
                    }
                    
                    // 比较值
                    if (actualValue != null) {
                        return actualValue.toString().equals(expectedValue);
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("条件评估失败: {}", e.getMessage());
        }
        
        return false;
    }

    /**
     * 根据权重随机选择响应
     *
     * @param responses 响应列表
     * @return 随机选择的响应
     */
    private MockResponse selectRandomResponse(List<MockResponse> responses) {
        int totalWeight = 0;
        for (MockResponse response : responses) {
            if (response.getWeight() != null && response.getWeight() > 0) {
                totalWeight += response.getWeight();
            }
        }

        if (totalWeight <= 0) {
            // 如果所有权重的总和为0，则等概率随机选择
            int randomIndex = ThreadLocalRandom.current().nextInt(responses.size());
            return responses.get(randomIndex);
        }

        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (MockResponse response : responses) {
            int weight = response.getWeight() != null ? response.getWeight() : 0;
            if (weight > 0) {
                currentWeight += weight;
                if (randomWeight < currentWeight) {
                    return response;
                }
            }
        }

        // 如果没有找到匹配的，返回第一个
        return responses.get(0);
    }

    /**
     * 构建Mock响应
     *
     * @param mockApi  Mock接口
     * @param response 接口响应
     * @return Mock响应DTO
     */
    private MockResponseDTO buildMockResponse(MockApi mockApi, MockResponse response) {
        MockResponseDTO.MockResponseDTOBuilder builder = MockResponseDTO.builder()
                .statusCode(response.getStatusCode())
                .body(parseResponseBody(response.getResponseBody()));

        // 设置响应头
        if (response.getHeaders() != null && !response.getHeaders().isEmpty()) {
            try {
                Map<String, String> headers = objectMapper.readValue(response.getHeaders(), new TypeReference<Map<String, String>>() {});
                builder.headers(headers);
            } catch (Exception e) {
                log.warn("解析响应头失败: {}", e.getMessage());
            }
        }

        // 设置响应延迟
        if (response.getStatusCode() == 200 && mockApi.getResponseDelay() != null && mockApi.getResponseDelay() > 0) {
            builder.delay(mockApi.getResponseDelay());
        }

        MockResponseDTO mockResponseDTO = builder.build();
        log.info("构建Mock响应: 状态码={}, 延迟={}ms", mockResponseDTO.getStatusCode(), mockResponseDTO.getDelay());
        return mockResponseDTO;
    }

    /**
     * 解析响应体
     *
     * @param responseBody 响应体字符串
     * @return 解析后的响应体
     */
    private Object parseResponseBody(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "";
        }

        responseBody = responseBody.trim();

        // 尝试解析为JSON对象或数组
        if ((responseBody.startsWith("{") && responseBody.endsWith("}")) ||
                (responseBody.startsWith("[") && responseBody.endsWith("]"))) {
            try {
                return objectMapper.readValue(responseBody, Object.class);
            } catch (Exception e) {
                log.debug("解析JSON响应体失败，返回原始字符串: {}", e.getMessage());
            }
        }

        // 返回原始字符串
        return responseBody;
    }

    /**
     * 创建错误响应
     *
     * @param statusCode 状态码
     * @param message    错误消息
     * @return Mock响应DTO
     */
    private MockResponseDTO createErrorResponse(Integer statusCode, String message) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", statusCode);
        errorBody.put("message", message);
        errorBody.put("timestamp", System.currentTimeMillis());

        return MockResponseDTO.builder()
                .statusCode(statusCode)
                .body(errorBody)
                .build();
    }
}
