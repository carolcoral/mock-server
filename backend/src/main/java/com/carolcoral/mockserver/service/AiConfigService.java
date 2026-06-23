/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.AiConfig;
import com.carolcoral.mockserver.repository.AiConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 配置服务
 *
 * @author carolcoral
 * @since 2026-06-23
 */
@Service
public class AiConfigService {

    @Autowired
    private AiConfigRepository aiConfigRepository;

    /**
     * 获取所有 AI 配置
     */
    public List<AiConfig> getAllConfigs() {
        return aiConfigRepository.findAll();
    }

    /**
     * 获取启用的 AI 配置
     */
    public AiConfig getEnabledConfig() {
        return aiConfigRepository.findFirstByEnabledTrue().orElse(null);
    }

    /**
     * 按 ID 获取配置
     */
    public AiConfig getById(Long id) {
        return aiConfigRepository.findById(id).orElse(null);
    }

    /**
     * 按服务商标识获取配置
     */
    public AiConfig getByProvider(String provider) {
        return aiConfigRepository.findByProvider(provider).orElse(null);
    }

    /**
     * 保存或更新 AI 配置
     */
    @Transactional
    public AiConfig saveConfig(AiConfig config) {
        AiConfig existing = aiConfigRepository.findByProvider(config.getProvider()).orElse(null);
        if (existing != null) {
            existing.setProviderName(config.getProviderName());
            existing.setApiUrl(config.getApiUrl());
            existing.setApiKey(config.getApiKey());
            existing.setDefaultModel(config.getDefaultModel());
            existing.setMaxTokens(config.getMaxTokens());
            existing.setTemperature(config.getTemperature());
            existing.setEnabled(config.getEnabled());
            return aiConfigRepository.save(existing);
        }
        return aiConfigRepository.save(config);
    }

    /**
     * 切换启用状态（同时只允许一个服务商启用）
     */
    @Transactional
    public AiConfig toggleEnabled(Long id, boolean enabled) {
        AiConfig config = aiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI配置不存在: " + id));

        if (enabled) {
            // 先禁用所有其他配置
            List<AiConfig> all = aiConfigRepository.findAll();
            for (AiConfig c : all) {
                if (!c.getId().equals(id) && Boolean.TRUE.equals(c.getEnabled())) {
                    c.setEnabled(false);
                    aiConfigRepository.save(c);
                }
            }
        }

        config.setEnabled(enabled);
        return aiConfigRepository.save(config);
    }

    /**
     * 获取预设服务商列表（含默认 API 地址和模型）
     */
    public Map<String, Map<String, String>> getPresetProviders() {
        Map<String, Map<String, String>> providers = new LinkedHashMap<>();

        // 国际
        putProvider(providers, "openai",    "OpenAI",         "https://api.openai.com/v1",              "gpt-4o",                   "https://openai.com");
        putProvider(providers, "azure",     "Azure OpenAI",   "https://{resource}.openai.azure.com",    "gpt-4",                    "https://azure.microsoft.com/en-us/products/ai-services/openai-service");
        putProvider(providers, "google",    "Google Gemini",  "https://generativelanguage.googleapis.com", "gemini-2.5-flash",     "https://ai.google.dev");
        putProvider(providers, "anthropic", "Anthropic Claude","https://api.anthropic.com",             "claude-sonnet-4-20250514", "https://www.anthropic.com");

        // 中国境内主流服务商
        putProvider(providers, "deepseek",  "DeepSeek",       "https://api.deepseek.com/v1",            "deepseek-chat",            "https://www.deepseek.com");
        putProvider(providers, "qwen",      "通义千问",        "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus",   "https://tongyi.aliyun.com");
        putProvider(providers, "zhipu",     "智谱 GLM",       "https://open.bigmodel.cn/api/paas/v4",   "glm-4-plus",               "https://open.bigmodel.cn");
        putProvider(providers, "moonshot",  "Moonshot",       "https://api.moonshot.cn/v1",             "moonshot-v1-8k",           "https://www.moonshot.cn");
        putProvider(providers, "baichuan",  "百川智能",        "https://api.baichuan-ai.com/v1",         "Baichuan4",                "https://www.baichuan-ai.com");
        putProvider(providers, "minimax",   "MiniMax",        "https://api.minimax.chat/v1",            "abab6.5s-chat",            "https://www.minimaxi.com");
        putProvider(providers, "xiaomi",   "小米 MiMo",       "https://api.xiaomimimo.com/v1",           "mimo-pro",                 "https://mimo.xiaomi.com");
        putProvider(providers, "bytedance","火山引擎（豆包）",  "https://ark.cn-beijing.volces.com/api/v3", "doubao-pro-256k",       "https://www.volcengine.com/product/doubao");

        // 自定义
        putProvider(providers, "custom",    "自定义（OpenAI 协议）", "", "", "");

        return providers;
    }

    private void putProvider(Map<String, Map<String, String>> map, String key, String name, String url, String model, String website) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("name", name);
        info.put("apiUrl", url);
        info.put("defaultModel", model);
        info.put("website", website);
        map.put(key, info);
    }
}
