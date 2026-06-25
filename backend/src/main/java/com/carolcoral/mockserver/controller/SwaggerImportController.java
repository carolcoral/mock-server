/**
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.service.SwaggerImportService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class SwaggerImportController {

    private static final Logger log = LoggerFactory.getLogger(SwaggerImportController.class);

    private final SwaggerImportService swaggerImportService;

    public SwaggerImportController(SwaggerImportService swaggerImportService) {
        this.swaggerImportService = swaggerImportService;
    }

    /**
     * 从上传的 Swagger JSON 文件导入接口
     */
    @PostMapping("/{projectId}/import-swagger-file")
    public ApiResponse<SwaggerImportService.ImportResult> importSwaggerFile(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L; // fallback
        }

        if (file.isEmpty()) {
            return ApiResponse.error("上传文件为空");
        }

        try {
            SwaggerImportService.ImportResult result = swaggerImportService.importFromStream(
                    file.getInputStream(), projectId, userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("导入 Swagger 文件失败", e);
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 从 Swagger URL 导入接口
     */
    @PostMapping("/{projectId}/import-swagger-url")
    public ApiResponse<SwaggerImportService.ImportResult> importSwaggerUrl(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L;
        }

        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ApiResponse.error("Swagger 地址不能为空");
        }

        try {
            SwaggerImportService.ImportResult result = swaggerImportService.importFromUrl(url, projectId, userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("从 URL 导入 Swagger 失败", e);
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }
}
