package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.MockRequest;
import com.carolcoral.mockserver.dto.MockResponseDTO;
import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.entity.ResponseRequestParam;
import com.carolcoral.mockserver.repository.MockApiRepository;
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
    public MockService(CacheUtil cacheUtil, ObjectMapper objectMapper, RequestLogService requestLogService,
                      ResponseRequestParamService responseRequestParamService, MockApiRepository mockApiRepository) {
        this.cacheUtil = cacheUtil;
        this.objectMapper = objectMapper;
        this.requestLogService = requestLogService;
        this.responseRequestParamService = responseRequestParamService;
        this.mockApiRepository = mockApiRepository;
    }

    private final CacheUtil cacheUtil;
    private final ObjectMapper objectMapper;
    private final RequestLogService requestLogService;
    private final ResponseRequestParamService responseRequestParamService;
    private final MockApiRepository mockApiRepository;

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

            // 根据接口模板路径提取 RESTful 路径参数
            Map<String, String> pathParams = extractPathParams(mockRequest.getPath(), mockApi.getPath());
            mockRequest.setPathParams(pathParams);
            log.info("提取路径参数: 路径={}, 模板={}, 参数={}", mockRequest.getPath(), mockApi.getPath(), pathParams);

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

            // 根据条件匹配响应（优先级最高）
            MockResponse matchedResponse = matchResponseByCondition(enabledResponses, mockRequest);
            if (matchedResponse != null) {
                MockResponseDTO responseDTO = buildMockResponse(mockApi, matchedResponse);
                statusCode = responseDTO.getStatusCode();
                return responseDTO;
            }

            // 优先返回默认响应
            MockResponse defaultResponse = enabledResponses.stream()
                    .filter(r -> r.getIsDefault() != null && r.getIsDefault())
                    .findFirst()
                    .orElse(null);
            if (defaultResponse != null) {
                log.info("返回默认响应: 状态码={}", defaultResponse.getStatusCode());
                MockResponseDTO responseDTO = buildMockResponse(mockApi, defaultResponse);
                statusCode = responseDTO.getStatusCode();
                return responseDTO;
            }

            // 如果启用了随机返回，从所有启用且激活的响应中按权重随机选择
            if (mockApi.getEnableRandom() != null && mockApi.getEnableRandom()) {
                // 过滤出启用且激活的响应（排除默认响应）
                List<MockResponse> enabledActiveResponses = new ArrayList<>();
                for (MockResponse response : enabledResponses) {
                    if (response.getActive() != null && response.getActive() &&
                        (response.getIsDefault() == null || !response.getIsDefault())) {
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

            // 返回激活的响应
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

            // 最后返回第一个响应（兜底）
            MockResponse fallbackResponse = enabledResponses.get(0);
            MockResponseDTO responseDTO = buildMockResponse(mockApi, fallbackResponse);
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

            // 先尝试精确匹配
            Optional<MockApi> apiOpt = cacheUtil.getApiFromCache(projectId, path, httpMethod);
            if (apiOpt.isPresent()) {
                return apiOpt;
            }

            // 精确匹配失败，尝试 RESTful 路径模板匹配
            List<MockApi> apis = mockApiRepository.findByProjectIdAndMethod(projectId, httpMethod);
            for (MockApi api : apis) {
                if (isPathMatch(path, api.getPath())) {
                    log.debug("RESTful 路径模板匹配: {} -> {}", path, api.getPath());
                    return Optional.of(api);
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("查找接口失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 从请求路径中提取 RESTful 路径参数
     *
     * @param requestPath  请求路径，如 /user/admin
     * @param templatePath 模板路径，如 /user/{id}
     * @return 路径参数映射
     */
    private Map<String, String> extractPathParams(String requestPath, String templatePath) {
        Map<String, String> pathParams = new HashMap<>();

        // 分离路径和查询字符串
        String requestPathOnly = requestPath.split("\\?")[0];
        String templatePathOnly = templatePath.split("\\?")[0];

        // 分割路径为段
        String[] requestParts = requestPathOnly.split("/");
        String[] templateParts = templatePathOnly.split("/");

        // 如果段数不匹配，返回空
        if (requestParts.length != templateParts.length) {
            return pathParams;
        }

        // 逐段匹配并提取参数
        for (int i = 0; i < templateParts.length; i++) {
            String templatePart = templateParts[i];
            String requestPart = requestParts[i];

            // 如果模板段是占位符 {param}，则提取值
            if (templatePart.startsWith("{") && templatePart.endsWith("}")) {
                String paramName = templatePart.substring(1, templatePart.length() - 1);
                pathParams.put(paramName, requestPart);
            }
        }

        return pathParams;
    }

    /**
     * 判断请求路径是否匹配模板路径（支持 RESTful 风格）
     *
     * @param requestPath 请求路径，如 /user/admin 或 /user?name={name}
     * @param templatePath 模板路径，如 /user/{id} 或 /user?name={name}
     * @return 是否匹配
     */
    private boolean isPathMatch(String requestPath, String templatePath) {
        // 分离路径和查询字符串
        String requestPathOnly = requestPath.split("\\?")[0];
        String templatePathOnly = templatePath.split("\\?")[0];

        // 分割路径为段
        String[] requestParts = requestPathOnly.split("/");
        String[] templateParts = templatePathOnly.split("/");

        // 路径段数量必须相同
        if (requestParts.length != templateParts.length) {
            return false;
        }

        // 逐段匹配路径部分
        for (int i = 0; i < templateParts.length; i++) {
            String templatePart = templateParts[i];
            String requestPart = requestParts[i];

            // 如果模板段是占位符 {param}，则跳过值检查
            if (templatePart.startsWith("{") && templatePart.endsWith("}")) {
                continue;
            }

            // 非占位符段必须完全匹配
            if (!templatePart.equals(requestPart)) {
                return false;
            }
        }

        // 检查查询参数部分
        String requestQuery = requestPath.contains("?") ? requestPath.substring(requestPath.indexOf("?")) : "";
        String templateQuery = templatePath.contains("?") ? templatePath.substring(templatePath.indexOf("?")) : "";

        // 如果都有查询参数或都没有查询参数，则匹配成功
        // 查询参数的具体值匹配在后续的请求参数匹配逻辑中处理
        return true;
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
            // 跳过默认响应，默认响应应该在其他逻辑中处理
            if (response.getIsDefault() != null && response.getIsDefault()) {
                continue;
            }

            boolean conditionMatched = false;

            // 首先检查是否有条件表达式
            if (response.getCondition() != null && !response.getCondition().isEmpty()) {
                try {
                    if (evaluateCondition(response.getCondition(), mockRequest)) {
                        conditionMatched = true;
                    }
                } catch (Exception e) {
                    log.warn("条件表达式评估失败: {}", e.getMessage());
                }
            }

            // 如果条件匹配，或者没有条件，则检查请求参数
            if (conditionMatched || (response.getCondition() == null || response.getCondition().isEmpty())) {
                if (matchRequestParams(response, mockRequest)) {
                    log.info("匹配到响应: 响应ID={}, 条件={}", response.getId(), response.getCondition());
                    return response;
                }
            }
        }
        return null;
    }

    /**
     * 根据请求参数匹配响应
     *
     * @param response    响应
     * @param mockRequest Mock请求
     * @return 是否匹配
     */
    private boolean matchRequestParams(MockResponse response, MockRequest mockRequest) {
        try {
            var result = responseRequestParamService.getParamsByResponseId(response.getId());
            if (result.getCode() != 200 || result.getData() == null || result.getData().isEmpty()) {
                log.debug("响应 {} 没有配置请求参数，直接匹配", response.getId());
                // 没有请求参数，直接返回true
                return true;
            }

            var params = result.getData();
            log.debug("响应 {} 有 {} 个请求参数", response.getId(), params.size());
            for (var param : params) {
                if (!matchRequestParam(param, mockRequest)) {
                    log.debug("响应 {} 参数匹配失败", response.getId());
                    return false;
                }
            }
            log.debug("响应 {} 所有参数匹配成功", response.getId());
            return true;
        } catch (Exception e) {
            log.warn("请求参数匹配失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 匹配单个请求参数
     *
     * @param param       请求参数DTO
     * @param mockRequest Mock请求
     * @return 是否匹配
     */
    private boolean matchRequestParam(com.carolcoral.mockserver.dto.ResponseRequestParamDTO param,
                                   MockRequest mockRequest) {
        try {
            ResponseRequestParam.ParamType paramType = ResponseRequestParam.ParamType.valueOf(param.getParamType());
            String expectedValue = param.getParamValue();
            String paramName = param.getParamName();

            log.debug("匹配请求参数: 名称={}, 类型={}, 期望值={}", paramName, paramType, expectedValue);

            Object actualValue = null;

            switch (paramType) {
                case PATH:
                    // 从路径中获取值（RESTful风格）
                    actualValue = mockRequest.getPathParams() != null ? mockRequest.getPathParams().get(paramName) : null;
                    break;

                case QUERY:
                    // 从请求参数中获取值
                    if (mockRequest.getParams() != null) {
                        actualValue = mockRequest.getParams().get(paramName);
                    }
                    break;

                case REQUEST_BODY:
                    // 从请求体中获取值
                    if (mockRequest.getBody() != null) {
                        try {
                            String jsonBody = objectMapper.writeValueAsString(mockRequest.getBody());
                            actualValue = JsonPath.read(jsonBody, "$." + paramName);
                        } catch (Exception e) {
                            log.debug("从请求体获取值失败: {}", e.getMessage());
                        }
                    }
                    break;

                case HEADER:
                    // 从请求头中获取值
                    if (mockRequest.getHeaders() != null) {
                        actualValue = mockRequest.getHeaders().get(paramName);
                    }
                    break;

                case FILE:
                    // 文件类型直接返回true，不需要匹配值
                    log.debug("文件类型参数，跳过匹配");
                    return true;
            }

            // 如果参数是必填的，但是没有匹配到值，返回false
            if (param.getRequired() && actualValue == null) {
                return false;
            }

            // 如果参数不是必填的，且没有值，跳过这个参数
            if (!param.getRequired() && actualValue == null) {
                return true;
            }

            // 比较值
            if (expectedValue != null && actualValue != null) {
                // 特殊处理"通用"值，表示匹配任意值
                if ("通用".equals(expectedValue) || "*".equals(expectedValue)) {
                    log.debug("参数值匹配（通用模式）: 参数={}, 实际值={}", paramName, actualValue);
                    return true;
                }
                boolean matched = actualValue.toString().equals(expectedValue);
                log.debug("参数值匹配: 参数={}, 期望值={}, 实际值={}, 匹配={}", paramName, expectedValue, actualValue, matched);
                return matched;
            }

            return false;
        } catch (Exception e) {
            log.warn("请求参数匹配失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从路径中提取RESTful参数
     *
     * @param path      路径
     * @param paramName 参数名
     * @return 参数值
     */
    private String extractPathParam(String path, String paramName) {
        // 假设路径格式为: /api/users/{userId}/posts/{postId}
        // 提取 {userId} 或 {postId} 中的值
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.startsWith("{") && part.endsWith("}")) {
                String name = part.substring(1, part.length() - 1);
                if (name.equals(paramName)) {
                    // 这里需要根据实际请求路径来提取值
                    // 暂时返回null，需要从实际请求中获取
                    return null;
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

        // 设置响应延迟（优先使用响应级别的延迟，如果没有则使用接口级别的延迟）
        Integer delay = null;
        if (response.getResponseDelay() != null && response.getResponseDelay() > 0) {
            delay = response.getResponseDelay();
        } else if (mockApi.getResponseDelay() != null && mockApi.getResponseDelay() > 0) {
            delay = mockApi.getResponseDelay();
        }

        if (delay != null && delay > 0) {
            builder.delay(delay);
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
