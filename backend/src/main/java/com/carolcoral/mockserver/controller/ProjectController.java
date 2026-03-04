package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 项目控制器
 *
 * @author carolcoral
 */
@Tag(name = "项目管理", description = "项目管理相关接口")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     *
     * @param project 项目信息
     * @param request 请求
     * @return 创建的项目
     */
    @Operation(summary = "创建项目", description = "创建新项目")
    @PostMapping
    public ApiResponse<Project> createProject(@Parameter(description = "项目信息") @Valid @RequestBody Project project,
                                              HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("创建项目请求，用户: {}", userId);
        return projectService.createProject(project, userId);
    }

    /**
     * 更新项目
     *
     * @param project 项目信息
     * @return 更新的项目
     */
    @Operation(summary = "更新项目", description = "更新项目信息")
    @PutMapping
    public ApiResponse<Project> updateProject(@Parameter(description = "项目信息") @Valid @RequestBody Project project) {
        log.info("更新项目请求: {}", project.getId());
        return projectService.updateProject(project);
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @return 删除结果
     */
    @Operation(summary = "删除项目", description = "删除项目，需要管理员权限")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@Parameter(description = "项目ID", example = "1") @PathVariable Long projectId) {
        log.info("删除项目请求: {}", projectId);
        return projectService.deleteProject(projectId);
    }

    /**
     * 根据ID查询项目
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    @Operation(summary = "根据ID查询项目", description = "根据项目ID查询项目信息")
    @GetMapping("/{projectId}")
    public ApiResponse<Project> getProjectById(@Parameter(description = "项目ID", example = "1") @PathVariable Long projectId) {
        log.info("查询项目请求: {}", projectId);
        return projectService.getProjectById(projectId);
    }

    /**
     * 根据项目编码查询项目
     *
     * @param code 项目编码
     * @return 项目信息
     */
    @Operation(summary = "根据项目编码查询项目", description = "根据项目编码查询项目信息")
    @GetMapping("/code/{code}")
    public ApiResponse<Project> getProjectByCode(
            @Parameter(description = "项目编码", example = "ecmall")
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "项目编码只能包含字母、数字、下划线和连字符") String code) {
        log.info("查询项目请求，编码: {}", code);
        return projectService.getProjectByCode(code);
    }

    /**
     * 查询所有项目
     *
     * @return 项目列表
     */
    @Operation(summary = "查询所有项目", description = "查询所有项目列表")
    @GetMapping
    public ApiResponse<java.util.List<Project>> getAllProjects(HttpServletRequest request) {
        log.info("查询所有项目请求");
        return projectService.getAllProjects();
    }

    /**
     * 查询启用状态的项目
     *
     * @return 项目列表
     */
    @Operation(summary = "查询启用状态的项目", description = "查询所有启用状态的项目")
    @GetMapping("/enabled")
    public ApiResponse<java.util.List<Project>> getEnabledProjects() {
        log.info("查询启用状态项目请求");
        return projectService.getEnabledProjects();
    }

    /**
     * 查询用户有权限访问的项目
     *
     * @param request 请求
     * @return 项目列表
     */
    @Operation(summary = "查询用户有权限访问的项目", description = "查询当前用户有权限访问的项目列表")
    @GetMapping("/accessible")
    public ApiResponse<java.util.List<Project>> getAccessibleProjects(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("查询用户有权限访问的项目请求，用户: {}", userId);
        return projectService.getAccessibleProjects(userId);
    }

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 操作结果
     */
    @Operation(summary = "添加项目成员", description = "为项目添加成员，需要项目管理员权限")
    @PostMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> addProjectMember(@Parameter(description = "项目ID", example = "1") @PathVariable Long projectId,
                                               @Parameter(description = "用户ID", example = "2") @PathVariable Long userId) {
        log.info("添加项目成员请求: 项目={}, 用户={}", projectId, userId);
        return projectService.addProjectMember(projectId, userId);
    }

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除项目成员", description = "从项目中移除成员，需要项目管理员权限")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> removeProjectMember(@Parameter(description = "项目ID", example = "1") @PathVariable Long projectId,
                                                  @Parameter(description = "用户ID", example = "2") @PathVariable Long userId) {
        log.info("移除项目成员请求: 项目={}, 用户={}", projectId, userId);
        return projectService.removeProjectMember(projectId, userId);
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request 请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        return userIdAttr != null ? (Long) userIdAttr : 0L;
    }
}