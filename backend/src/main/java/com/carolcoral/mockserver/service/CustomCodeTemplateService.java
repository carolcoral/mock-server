/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.CustomCodeTemplate;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.entity.ProjectMember;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.CustomCodeTemplateRepository;
import com.carolcoral.mockserver.repository.ProjectMemberRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 自定义代码模板服务类
 *
 * @author carolcoral
 */
@Tag(name = "自定义代码模板服务", description = "自定义代码模板业务逻辑处理")
@Service
public class CustomCodeTemplateService {
    private static final Logger log = LoggerFactory.getLogger(CustomCodeTemplateService.class);

    private final CustomCodeTemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public CustomCodeTemplateService(CustomCodeTemplateRepository templateRepository,
                                     ProjectRepository projectRepository,
                                     ProjectMemberRepository projectMemberRepository) {
        this.templateRepository = templateRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    /**
     * 创建模板
     */
    @Operation(summary = "创建模板")
    @Transactional
    public ApiResponse<CustomCodeTemplate> createTemplate(CustomCodeTemplate template, Long userId) {
        try {
            if (template.getProject() == null || template.getProject().getId() == null) {
                return ApiResponse.error("项目ID不能为空");
            }

            Optional<Project> projectOpt = projectRepository.findById(template.getProject().getId());
            if (!projectOpt.isPresent()) {
                return ApiResponse.error("项目不存在");
            }

            template.setCreateUserId(userId);
            template.setProject(projectOpt.get());

            CustomCodeTemplate saved = templateRepository.save(template);
            log.info("创建自定义代码模板成功: id={}, name={}, projectId={}", saved.getId(), saved.getName(), template.getProject().getId());
            return ApiResponse.success(saved);
        } catch (Exception e) {
            log.error("创建自定义代码模板失败: {}", e.getMessage(), e);
            return ApiResponse.error("创建模板失败，请稍后重试");
        }
    }

    /**
     * 更新模板
     */
    @Operation(summary = "更新模板")
    @Transactional
    public ApiResponse<CustomCodeTemplate> updateTemplate(CustomCodeTemplate template, Long userId, User.UserRole userRole) {
        try {
            if (template.getId() == null) {
                return ApiResponse.error("模板ID不能为空");
            }

            Optional<CustomCodeTemplate> existingOpt = templateRepository.findById(template.getId());
            if (!existingOpt.isPresent()) {
                return ApiResponse.error("模板不存在");
            }

            CustomCodeTemplate existing = existingOpt.get();

            // 权限检查：系统管理员、项目创建者、项目管理员可操作
            if (userRole != User.UserRole.ADMIN && !hasProjectAdminPermission(existing.getProject().getId(), userId)) {
                return ApiResponse.error("没有权限修改此模板");
            }

            if (template.getName() != null) {
                existing.setName(template.getName());
            }
            if (template.getDescription() != null) {
                existing.setDescription(template.getDescription());
            }
            if (template.getSourceCode() != null) {
                existing.setSourceCode(template.getSourceCode());
            }
            if (template.getEnabled() != null) {
                existing.setEnabled(template.getEnabled());
            }
            if (template.getProject() != null && template.getProject().getId() != null) {
                Optional<Project> projectOpt = projectRepository.findById(template.getProject().getId());
                if (!projectOpt.isPresent()) {
                    return ApiResponse.error("项目不存在");
                }
                existing.setProject(projectOpt.get());
            }

            CustomCodeTemplate saved = templateRepository.save(existing);
            log.info("更新自定义代码模板成功: id={}", saved.getId());
            return ApiResponse.success(saved);
        } catch (Exception e) {
            log.error("更新自定义代码模板失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新模板失败，请稍后重试");
        }
    }

    /**
     * 删除模板
     */
    @Operation(summary = "删除模板")
    @Transactional
    public ApiResponse<Void> deleteTemplate(Long templateId, Long userId, User.UserRole userRole) {
        try {
            Optional<CustomCodeTemplate> existingOpt = templateRepository.findById(templateId);
            if (!existingOpt.isPresent()) {
                return ApiResponse.error("模板不存在");
            }

            CustomCodeTemplate existing = existingOpt.get();

            // 权限检查：系统管理员、项目创建者、项目管理员可操作
            if (userRole != User.UserRole.ADMIN && !hasProjectAdminPermission(existing.getProject().getId(), userId)) {
                return ApiResponse.error("没有权限删除此模板");
            }

            templateRepository.delete(existing);
            log.info("删除自定义代码模板成功: id={}", templateId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除自定义代码模板失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除模板失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询模板
     */
    @Operation(summary = "根据ID查询模板")
    public ApiResponse<CustomCodeTemplate> getTemplateById(Long templateId) {
        try {
            Optional<CustomCodeTemplate> templateOpt = templateRepository.findById(templateId);
            if (!templateOpt.isPresent()) {
                return ApiResponse.error("模板不存在");
            }
            return ApiResponse.success(templateOpt.get());
        } catch (Exception e) {
            log.error("查询模板失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询模板失败，请稍后重试");
        }
    }

    /**
     * 根据项目ID查询模板列表
     */
    @Operation(summary = "根据项目ID查询模板列表")
    public ApiResponse<List<CustomCodeTemplate>> getTemplatesByProjectId(Long projectId) {
        try {
            List<CustomCodeTemplate> templates = templateRepository.findByProjectId(projectId);
            return ApiResponse.success(templates);
        } catch (Exception e) {
            log.error("查询模板列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询模板列表失败，请稍后重试");
        }
    }

    /**
     * 查询所有模板（系统管理员）或用户有权限访问的项目下的模板（普通用户）
     * 支持按名称、项目ID、启用状态过滤
     */
    @Operation(summary = "查询用户可访问的模板列表（支持过滤）")
    public ApiResponse<List<CustomCodeTemplate>> getAccessibleTemplates(
            Long userId, User.UserRole userRole, String name, Long projectId, Boolean enabled) {
        try {
            Specification<CustomCodeTemplate> spec = buildAccessSpec(userId, userRole, name, projectId, enabled);
            List<CustomCodeTemplate> templates = templateRepository.findAll(spec);
            return ApiResponse.success(templates);
        } catch (Exception e) {
            log.error("查询模板列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询模板列表失败，请稍后重试");
        }
    }

    /**
     * 构建查询条件
     */
    private Specification<CustomCodeTemplate> buildAccessSpec(
            Long userId, User.UserRole userRole, String name, Long projectId, Boolean enabled) {

        Specification<CustomCodeTemplate> spec = Specification.where(null);

        // 普通用户：限制只能看所属项目的模板
        if (userRole != User.UserRole.ADMIN) {
            List<Long> projectIds = getAccessibleProjectIds(userId);
            if (projectIds.isEmpty()) {
                // 没有权限访问任何项目，使用不可能的条件返回空
                spec = spec.and((root, query, cb) -> cb.disjunction());
            } else {
                spec = spec.and((root, query, cb) ->
                        root.get("project").get("id").in(projectIds));
            }
        }

        // 按名称模糊搜索
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        // 按项目ID精确过滤
        if (projectId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("project").get("id"), projectId));
        }

        // 按启用状态过滤
        if (enabled != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("enabled"), enabled));
        }

        return spec;
    }

    /**
     * 获取用户有权限访问的项目ID列表
     */
    private List<Long> getAccessibleProjectIds(Long userId) {
        List<Long> projectIds = new ArrayList<>();

        // 用户创建的项目
        List<Project> ownedProjects = projectRepository.findByCreateUserId(userId);
        projectIds.addAll(ownedProjects.stream().map(Project::getId).collect(Collectors.toList()));

        // 用户作为成员的项目
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        projectIds.addAll(memberships.stream().map(ProjectMember::getProjectId).collect(Collectors.toList()));

        return projectIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 根据项目ID查询已启用的模板列表（用于下拉选择）
     */
    @Operation(summary = "根据项目ID查询已启用的模板列表")
    public ApiResponse<List<CustomCodeTemplate>> getEnabledTemplatesByProjectId(Long projectId) {
        try {
            List<CustomCodeTemplate> templates = templateRepository.findByProjectIdAndEnabled(projectId, true);
            return ApiResponse.success(templates);
        } catch (Exception e) {
            log.error("查询启用模板列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询模板列表失败，请稍后重试");
        }
    }

    /**
     * 检查用户是否是项目管理员（创建者或管理员）
     */
    private boolean hasProjectAdminPermission(Long projectId, Long userId) {
        return projectMemberRepository
                .findByProjectIdAndUserIdAndRole(projectId, userId, ProjectMember.MemberRole.CREATOR)
                .isPresent() ||
               projectMemberRepository
                .findByProjectIdAndUserIdAndRole(projectId, userId, ProjectMember.MemberRole.ADMIN)
                .isPresent();
    }
}
