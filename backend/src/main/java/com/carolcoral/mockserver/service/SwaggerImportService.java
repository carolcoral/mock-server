package com.carolcoral.mockserver.service;

import com.alibaba.fastjson2.JSON;
import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockApi.HttpMethod;
import com.carolcoral.mockserver.entity.MockApi.RequestType;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.repository.MockApiRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SwaggerImportService {

    private static final Logger log = LoggerFactory.getLogger(SwaggerImportService.class);

    private final MockApiRepository mockApiRepository;
    private final ProjectRepository projectRepository;
    private final MockApiService mockApiService;

    public SwaggerImportService(MockApiRepository mockApiRepository,
                                 ProjectRepository projectRepository,
                                 MockApiService mockApiService) {
        this.mockApiRepository = mockApiRepository;
        this.projectRepository = projectRepository;
        this.mockApiService = mockApiService;
    }

    /**
     * 导入结果 DTO
     */
    public static class ImportResult {
        public int total;
        public int success;
        public int failed;
        public List<ImportError> errors = new ArrayList<>();

        public static class ImportError {
            public String path;
            public String method;
            public String reason;
        }
    }

    /**
     * 从 Swagger JSON 文件流导入
     */
    @Transactional
    public ImportResult importFromStream(InputStream inputStream, Long projectId, Long userId) {
        try {
            String content = new String(inputStream.readAllBytes());
            return importFromJson(content, projectId, userId);
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Swagger URL 导入
     */
    @Transactional
    public ImportResult importFromUrl(String url, Long projectId, Long userId) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("获取 Swagger 文档失败，HTTP 状态码: " + response.statusCode());
            }

            return importFromJson(response.body(), projectId, userId);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("获取 Swagger 文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 JSON 字符串解析并导入
     */
    private ImportResult importFromJson(String jsonContent, Long projectId, Long userId) {
        // 验证项目存在
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("项目不存在");
        }

        // 使用 Swagger Parser 解析（兼容 Swagger 2.0 和 OpenAPI 3.x）
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(jsonContent, null, null);
        OpenAPI openAPI = parseResult.getOpenAPI();

        if (openAPI == null) {
            throw new RuntimeException("无法解析 Swagger/OpenAPI 文档，请确认文件格式正确");
        }

        // 如果 parser 有消息，记录日志
        if (parseResult.getMessages() != null && !parseResult.getMessages().isEmpty()) {
            log.warn("Swagger 解析警告: {}", parseResult.getMessages());
        }

        ImportResult result = new ImportResult();

        // 遍历所有路径
        if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            result.total = 0;
            return result;
        }

        openAPI.getPaths().forEach((path, pathItem) -> {
            processPathItem(path, pathItem, openAPI, projectId, userId, result);
        });

        return result;
    }

    private void processPathItem(String path, PathItem pathItem, OpenAPI openAPI,
                                  Long projectId, Long userId, ImportResult result) {
        // 遍历该路径下的所有 HTTP 方法
        Map<PathItem.HttpMethod, Operation> operations = new LinkedHashMap<>();
        if (pathItem.getGet() != null) operations.put(PathItem.HttpMethod.GET, pathItem.getGet());
        if (pathItem.getPost() != null) operations.put(PathItem.HttpMethod.POST, pathItem.getPost());
        if (pathItem.getPut() != null) operations.put(PathItem.HttpMethod.PUT, pathItem.getPut());
        if (pathItem.getDelete() != null) operations.put(PathItem.HttpMethod.DELETE, pathItem.getDelete());
        if (pathItem.getPatch() != null) operations.put(PathItem.HttpMethod.PATCH, pathItem.getPatch());
        if (pathItem.getHead() != null) operations.put(PathItem.HttpMethod.HEAD, pathItem.getHead());
        if (pathItem.getOptions() != null) operations.put(PathItem.HttpMethod.OPTIONS, pathItem.getOptions());
        if (pathItem.getTrace() != null) operations.put(PathItem.HttpMethod.TRACE, pathItem.getTrace());

        for (Map.Entry<PathItem.HttpMethod, Operation> entry : operations.entrySet()) {
            PathItem.HttpMethod swaggerMethod = entry.getKey();
            Operation operation = entry.getValue();

            // 映射 HTTP 方法
            HttpMethod mockMethod = mapHttpMethod(swaggerMethod);
            if (mockMethod == null) continue;

            result.total++;

            try {
                // 生成 API 名称
                String apiName = generateApiName(operation, path, mockMethod);

                // 检查是否已存在相同 path+method 的接口
                Optional<MockApi> existing = mockApiRepository.findByProjectIdAndPathAndMethod(
                        projectId, path, mockMethod);
                if (existing.isPresent()) {
                    result.failed++;
                    ImportResult.ImportError err = new ImportResult.ImportError();
                    err.path = path;
                    err.method = mockMethod.name();
                    err.reason = "该路径和方法已存在";
                    result.errors.add(err);
                    continue;
                }

                // 创建 MockApi
                MockApi mockApi = new MockApi();
                mockApi.setName(apiName);
                mockApi.setPath(path);
                mockApi.setMethod(mockMethod);
                mockApi.setRequestType(RequestType.HTTP);
                mockApi.setEnabled(true);
                mockApi.setResponseDelay(0);
                mockApi.setEnableRandom(false);

                // 描述：优先用 operation summary，其次 description
                String desc = operation.getSummary();
                if (desc == null || desc.isBlank()) {
                    desc = operation.getDescription();
                }
                if (desc != null && desc.length() > 500) {
                    desc = desc.substring(0, 497) + "...";
                }
                mockApi.setDescription(desc);
                mockApi.setCreateTime(LocalDateTime.now());
                mockApi.setUpdateTime(LocalDateTime.now());
                mockApi.setCreateUserId(userId);

                // 关联项目
                Project project = new Project();
                project.setId(projectId);
                mockApi.setProject(project);

                // 构建默认响应体
                String responseBody = buildDefaultResponse(operation, openAPI);

                // 创建默认响应
                MockResponse mockResponse = new MockResponse();
                mockResponse.setStatusCode(200);
                mockResponse.setContentType("application/json");
                mockResponse.setResponseBody(responseBody);
                mockResponse.setIsDefault(true);
                mockResponse.setEnabled(true);
                mockResponse.setActive(true);
                mockResponse.setWeight(1);
                mockResponse.setConditionDesc("默认响应");
                mockResponse.setMockApi(mockApi);
                mockResponse.setCreateTime(LocalDateTime.now());
                mockResponse.setUpdateTime(LocalDateTime.now());

                // 添加到 API 的响应列表
                List<MockResponse> responses = new ArrayList<>();
                responses.add(mockResponse);
                mockApi.setResponses(responses);

                // 保存
                mockApiRepository.save(mockApi);
                result.success++;

            } catch (Exception e) {
                log.error("导入接口失败: {} {}", swaggerMethod, path, e);
                result.failed++;
                ImportResult.ImportError err = new ImportResult.ImportError();
                err.path = path;
                err.method = swaggerMethod.name();
                err.reason = e.getMessage();
                result.errors.add(err);
            }
        }
    }

    /**
     * 映射 Swagger HttpMethod 到 MockApi HttpMethod
     */
    private HttpMethod mapHttpMethod(PathItem.HttpMethod swaggerMethod) {
        return switch (swaggerMethod) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case PUT -> HttpMethod.PUT;
            case DELETE -> HttpMethod.DELETE;
            case PATCH -> HttpMethod.PATCH;
            default -> null; // HEAD, OPTIONS, TRACE 不支持
        };
    }

    /**
     * 生成 API 名称
     */
    private String generateApiName(Operation operation, String path, HttpMethod method) {
        String name = operation.getSummary();
        if (name != null && !name.isBlank()) {
            return name.length() > 100 ? name.substring(0, 97) + "..." : name;
        }

        // 从 operationId 生成
        String operationId = operation.getOperationId();
        if (operationId != null && !operationId.isBlank()) {
            return operationId.length() > 100 ? operationId.substring(0, 97) + "..." : operationId;
        }

        // 从路径和方法的组合生成
        return method.name() + " " + path;
    }

    /**
     * 根据 Swagger 定义构建默认的 JSON 响应示例
     */
    private String buildDefaultResponse(Operation operation, OpenAPI openAPI) {
        ApiResponses responses = operation.getResponses();
        if (responses == null || responses.isEmpty()) {
            return "{}";
        }

        // 优先取 200 响应
        ApiResponse successResponse = responses.get("200");
        if (successResponse == null) {
            successResponse = responses.get("default");
        }
        if (successResponse == null) {
            // 取第一个响应
            String firstKey = responses.keySet().iterator().next();
            successResponse = responses.get(firstKey);
        }

        if (successResponse == null) {
            return "{}";
        }

        Content content = successResponse.getContent();
        if (content == null) {
            return "{}";
        }

        // 优先 JSON
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.get("*/*");
        }
        if (mediaType == null && !content.isEmpty()) {
            String firstType = content.keySet().iterator().next();
            mediaType = content.get(firstType);
        }

        if (mediaType == null || mediaType.getSchema() == null) {
            return "{}";
        }

        Schema<?> schema = mediaType.getSchema();

        // 如果有 example，直接返回
        if (schema.getExample() != null) {
            Object example = schema.getExample();
            if (example instanceof String) {
                return (String) example;
            }
            return JSON.toJSONString(example);
        }

        // 根据 schema 生成示例 JSON
        return generateExampleFromSchema(schema, openAPI);
    }

    /**
     * 根据 Schema 递归生成示例 JSON
     */
    private String generateExampleFromSchema(Schema<?> schema, OpenAPI openAPI) {
        return generateExampleFromSchema(schema, openAPI, new HashSet<>(), 0);
    }

    private String generateExampleFromSchema(Schema<?> schema, OpenAPI openAPI,
                                              Set<String> visitedRefs, int depth) {
        if (schema == null) return "{}";
        if (depth > 20) return "{}";  // 防止过深递归

        // 如果有 $ref，解析引用
        String ref = schema.get$ref();
        if (ref != null) {
            if (visitedRefs.contains(ref)) return "{}";  // 防止循环引用
            visitedRefs.add(ref);
            Schema<?> resolved = resolveRef(ref, openAPI);
            if (resolved != null) {
                return generateExampleFromSchema(resolved, openAPI, visitedRefs, depth + 1);
            }
            return "{}";
        }

        // 处理数组
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            String itemExample = generateExampleFromSchema(schema.getItems(), openAPI, visitedRefs, depth + 1);
            return "[" + itemExample + "]";
        }

        // 处理对象
        if ("object".equals(schema.getType()) || schema.getProperties() != null) {
            return generateObjectExample(schema, openAPI, visitedRefs, depth);
        }

        // 处理枚举
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return JSON.toJSONString(schema.getEnum().get(0));
        }

        // 基本类型
        return getPrimitiveExample(schema.getType(), schema.getFormat());
    }

    private String generateObjectExample(Schema<?> schema, OpenAPI openAPI,
                                          Set<String> visitedRefs, int depth) {
        Map<String, Object> example = new LinkedHashMap<>();

        if (schema.getProperties() != null) {
            schema.getProperties().forEach((propName, propSchema) -> {
                Object value = generateExampleValue(propSchema, openAPI, visitedRefs, depth + 1);
                example.put(propName, value);
            });
        }

        if (example.isEmpty()) {
            return "{}";
        }

        return JSON.toJSONString(example);
    }

    private Object generateExampleValue(Schema<?> propSchema, OpenAPI openAPI,
                                         Set<String> visitedRefs, int depth) {
        if (propSchema == null) return "";
        if (depth > 20) return "";  // 防止过深递归

        // 解析 $ref
        String ref = propSchema.get$ref();
        if (ref != null) {
            if (visitedRefs.contains(ref)) return "{}";  // 防止循环引用
            visitedRefs.add(ref);
            Schema<?> resolved = resolveRef(ref, openAPI);
            if (resolved != null) {
                return generateExampleValue(resolved, openAPI, visitedRefs, depth + 1);
            }
            return "{}";
        }

        String type = propSchema.getType();

        // 数组
        if ("array".equals(type) && propSchema.getItems() != null) {
            List<Object> arr = new ArrayList<>();
            arr.add(generateExampleValue(propSchema.getItems(), openAPI, visitedRefs, depth + 1));
            return arr;
        }

        // 对象
        if ("object".equals(type) || propSchema.getProperties() != null) {
            Map<String, Object> obj = new LinkedHashMap<>();
            if (propSchema.getProperties() != null) {
                propSchema.getProperties().forEach((k, v) -> {
                    obj.put(k, generateExampleValue(v, openAPI, visitedRefs, depth + 1));
                });
            }
            return obj.isEmpty() ? "{}" : obj;
        }

        // 枚举
        if (propSchema.getEnum() != null && !propSchema.getEnum().isEmpty()) {
            return propSchema.getEnum().get(0);
        }

        // 基本类型
        return getPrimitiveExampleValue(propSchema.getType(), propSchema.getFormat());
    }

    private Object getPrimitiveExampleValue(String type, String format) {
        if (type == null) return "";
        return switch (type) {
            case "integer", "number" -> {
                if ("int64".equals(format) || "long".equals(format)) {
                    yield 1000001L;
                }
                if ("float".equals(format) || "double".equals(format)) {
                    yield 3.14;
                }
                yield 1;
            }
            case "boolean" -> true;
            case "string" -> {
                if ("date".equals(format)) {
                    yield "2024-01-01";
                }
                if ("date-time".equals(format)) {
                    yield "2024-01-01T00:00:00Z";
                }
                if ("email".equals(format)) {
                    yield "user@example.com";
                }
                if ("uri".equals(format) || "url".equals(format)) {
                    yield "https://example.com";
                }
                yield "string";
            }
            default -> "";
        };
    }

    private String getPrimitiveExample(String type, String format) {
        Object val = getPrimitiveExampleValue(type, format);
        if (val instanceof String) {
            return "\"" + val + "\"";
        }
        return String.valueOf(val);
    }

    /**
     * 解析 $ref 引用
     */
    private Schema<?> resolveRef(String ref, OpenAPI openAPI) {
        if (ref == null || openAPI == null) return null;

        // #/components/schemas/Pet
        if (ref.startsWith("#/components/schemas/")) {
            String schemaName = ref.substring("#/components/schemas/".length());
            if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                return openAPI.getComponents().getSchemas().get(schemaName);
            }
        }
        return null;
    }
}
