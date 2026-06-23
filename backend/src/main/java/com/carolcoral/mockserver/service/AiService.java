/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.*;

/**
 * AI 服务 - 封装 OpenAI 协议兼容的 LLM API 调用
 *
 * @author carolcoral
 * @since 2026-06-23
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Autowired
    private AiConfigService aiConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public AiService() {
        // 使用较短的超时时间，避免 AI 调用阻塞过久
        this.restTemplate = new RestTemplate();
        // 注意：RestTemplate 默认使用 SimpleClientHttpRequestFactory
        // 这里使用构建器方式设置超时
    }

    /**
     * 获取配置了超时的 RestTemplate
     */
    private RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(120).toMillis());
        return new RestTemplate(factory);
    }

    /**
     * 生成 Mock 响应数据
     *
     * @param apiMethod   接口请求方法 (GET/POST/PUT/DELETE/PATCH)
     * @param apiPath     接口路径
     * @param apiName     接口名称
     * @param description 接口描述
     * @param count       生成数量（默认3）
     * @return AI 生成的响应数据列表
     */
    public List<Map<String, Object>> generateMockResponse(
            String apiMethod, String apiPath, String apiName, String description, Integer count) {

        AiConfig config = aiConfigService.getEnabledConfig();
        if (config == null) {
            throw new RuntimeException("未启用任何 AI 服务商，请先在 AI 设置中配置并启用一个服务商");
        }

        if (config.getApiUrl() == null || config.getApiUrl().isBlank()) {
            throw new RuntimeException("AI 服务商 API 地址未配置");
        }
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new RuntimeException("AI 服务商 API Key 未配置");
        }

        int generateCount = (count != null && count > 0 && count <= 5) ? count : 3;

        String prompt = buildPrompt(apiMethod, apiPath, apiName, description, generateCount);

        try {
            String responseJson = callAiApi(config, prompt);
            return parseResponse(responseJson, generateCount);
        } catch (Exception e) {
            log.error("AI 生成 Mock 响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 AI 生成接口描述
     */
    public String generateApiDescription(String apiMethod, String apiPath, String apiName) {
        AiConfig config = aiConfigService.getEnabledConfig();
        if (config == null) {
            throw new RuntimeException("未启用任何 AI 服务商");
        }
        if (config.getApiUrl() == null || config.getApiUrl().isBlank()) {
            throw new RuntimeException("AI 服务商 API 地址未配置");
        }
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new RuntimeException("AI 服务商 API Key 未配置");
        }

        String prompt = String.format(
                "请为以下 API 接口生成一段简洁的中文描述（50字以内），仅返回描述文本，不要包含任何其他内容。\n\n" +
                "接口名称：%s\n请求方法：%s\n接口路径：%s",
                apiName != null ? apiName : "未命名接口",
                apiMethod,
                apiPath
        );

        try {
            String responseJson = callAiApi(config, prompt);
            return extractTextContent(responseJson);
        } catch (Exception e) {
            log.error("AI 生成接口描述失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 生成描述失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建生成 Mock 响应的 Prompt
     */
    private String buildPrompt(String method, String path, String name, String description, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个 API Mock 数据生成助手。请为以下 API 接口生成 ").append(count).append(" 个不同的 Mock 响应数据。\n\n");
        sb.append("接口信息：\n");
        sb.append("- 接口名称：").append(name != null ? name : "未命名接口").append("\n");
        sb.append("- 请求方法：").append(method).append("\n");
        sb.append("- 接口路径：").append(path).append("\n");
        if (description != null && !description.isBlank()) {
            sb.append("- 接口描述：").append(description).append("\n");
        }
        sb.append("\n");

        // 根据路径和名称推测业务场景
        sb.append("要求：\n");
        sb.append("1. 每个响应包含 statusCode（HTTP状态码）、contentType（固定为 application/json）、responseBody（JSON格式的响应体）、conditionDesc（该响应的使用场景描述，10字以内）\n");
        sb.append("2. 第1个必须是成功响应（200），响应体要符合接口名称和路径所暗示的业务场景，数据要逼真\n");
        sb.append("3. 其余响应可以是不同的业务场景（如参数错误400、未找到404、未授权401、服务器错误500等）\n");
        sb.append("4. 响应体必须使用中文内容，字段名使用 camelCase 英文\n");
        sb.append("5. 只返回纯 JSON 数组格式，不要包含 markdown 代码块标记，不要包含任何解释文字\n\n");

        sb.append("返回格式示例：\n");
        sb.append("[\n");
        sb.append("  {\"statusCode\":200,\"contentType\":\"application/json\",\"responseBody\":\"{\\\"code\\\":200,\\\"message\\\":\\\"操作成功\\\",\\\"data\\\":{...}}\",\"conditionDesc\":\"正常响应\"},\n");
        sb.append("  {\"statusCode\":400,\"contentType\":\"application/json\",\"responseBody\":\"{\\\"code\\\":400,\\\"message\\\":\\\"参数错误\\\"}\",\"conditionDesc\":\"参数错误\"}\n");
        sb.append("]");

        return sb.toString();
    }

    /**
     * 调用 OpenAI 兼容 API
     */
    private String callAiApi(AiConfig config, String prompt) throws Exception {
        String apiUrl = config.getApiUrl();
        // 确保 URL 以 /v1 结尾（大多数 OpenAI 兼容 API 需要）
        String chatUrl = apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions";
        // 如果 URL 已经包含 /chat/completions，则直接使用
        if (apiUrl.endsWith("/chat/completions")) {
            chatUrl = apiUrl;
        }

        String model = config.getDefaultModel();
        if (model == null || model.isBlank()) {
            model = "gpt-4o";
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个专业的 API Mock 数据生成助手，只返回要求的 JSON 格式数据，不返回任何额外内容。");
        messages.add(systemMsg);

        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        if (config.getTemperature() != null) {
            requestBody.put("temperature", config.getTemperature());
        } else {
            requestBody.put("temperature", 0.7);
        }

        if (config.getMaxTokens() != null) {
            requestBody.put("max_tokens", config.getMaxTokens());
        } else {
            requestBody.put("max_tokens", 4096);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("AI API 请求: url={}, model={}", chatUrl, model);

        RestTemplate rt = getRestTemplate();
        ResponseEntity<String> response = rt.exchange(chatUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("AI API 返回非 2xx 状态码: " + response.getStatusCode());
        }
    }

    /**
     * 从 OpenAI 格式响应中提取文本内容
     */
    private String extractTextContent(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText().trim();
                }
            }
        }
        throw new RuntimeException("无法解析 AI 响应，响应格式不符合预期");
    }

    /**
     * 解析 AI 生成的 Mock 响应列表
     */
    private List<Map<String, Object>> parseResponse(String responseJson, int expectedCount) throws Exception {
        String content = extractTextContent(responseJson);
        log.debug("AI 原始响应内容: {}", content);

        // 尝试清理可能的 markdown 代码块标记
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        content = content.trim();

        JsonNode root = objectMapper.readTree(content);
        if (!root.isArray()) {
            throw new RuntimeException("AI 返回的数据格式不是 JSON 数组");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < root.size() && results.size() < expectedCount; i++) {
            JsonNode item = root.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("statusCode", item.has("statusCode") ? item.get("statusCode").asInt() : 200);
            map.put("contentType", item.has("contentType") ? item.get("contentType").asText() : "application/json");
            map.put("responseBody", item.has("responseBody") ? item.get("responseBody").asText() : "");
            map.put("conditionDesc", item.has("conditionDesc") ? item.get("conditionDesc").asText() : "");
            results.add(map);
        }

        return results;
    }
}
