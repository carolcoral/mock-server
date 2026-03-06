/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.dto.ProjectWithRoleDTO;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.entity.ProjectMember;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.ProjectMemberRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.CacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目服务类
 *
 * @author carolcoral
 */
@Tag(name = "项目服务", description = "项目业务逻辑处理")
@Service
public class ProjectService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectService.class);

    /**
     * 构造器
     */
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository, CacheUtil cacheUtil) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.cacheUtil = cacheUtil;
    }

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CacheUtil cacheUtil;

    /**
     * 创建项目
     *
     * @param project 项目信息
     * @param userId  创建人ID
     * @return 创建的项目
     */
    @Operation(summary = "创建项目")
    @Transactional
    public ApiResponse<Project> createProject(@Parameter(description = "项目信息") Project project, @Parameter(description = "创建人ID") Long userId) {
        try {
            // 检查项目编码是否已存在
            if (projectRepository.existsByCode(project.getCode())) {
                return ApiResponse.error("项目编码已存在");
            }

            // 设置创建人
            project.setCreateUserId(userId);

            // 保存项目
            Project savedProject = projectRepository.save(project);

            // 创建创建者成员记录
            ProjectMember creatorMember = new ProjectMember();
            creatorMember.setProjectId(savedProject.getId());
            creatorMember.setUserId(userId);
            creatorMember.setRole(ProjectMember.MemberRole.CREATOR);
            projectMemberRepository.save(creatorMember);

            // 缓存项目
            cacheUtil.cacheProject(savedProject);

            log.info("创建项目成功: {}", savedProject.getCode());
            return ApiResponse.success(savedProject);

        } catch (Exception e) {
            log.error("创建项目失败: {}", e.getMessage(), e);
            return ApiResponse.error("创建项目失败，请稍后重试");
        }
    }

    /**
     * 更新项目
     *
     * @param project 项目信息
     * @param userId  当前用户ID
     * @return 更新的项目
     */
    @Operation(summary = "更新项目")
    @Transactional
    public ApiResponse<Project> updateProject(@Parameter(description = "项目信息") Project project, 
                                        @Parameter(description = "当前用户ID") Long userId) {
        try {
            Optional<Project> existingProjectOpt = projectRepository.findById(project.getId());

            if (!existingProjectOpt.isPresent()) {
                return ApiResponse.error("项目不存在");
            }

            Project existingProject = existingProjectOpt.get();

            // 检查用户是否有权限更新项目（创建者、管理员）
            if (!hasProjectPermission(existingProject.getId(), userId)) {
                return ApiResponse.error("没有权限管理此项目");
            }

            // 检查项目编码是否被其他项目使用
            if (!existingProject.getCode().equals(project.getCode()) &&
                    projectRepository.existsByCode(project.getCode())) {
                return ApiResponse.error("项目编码已存在");
            }

            // 更新项目信息
            if (project.getName() != null) {
                existingProject.setName(project.getName());
            }
            if (project.getDescription() != null) {
                existingProject.setDescription(project.getDescription());
            }
            if (project.getCode() != null) {
                existingProject.setCode(project.getCode());
            }
            if (project.getEnabled() != null) {
                existingProject.setEnabled(project.getEnabled());
            }

            Project updatedProject = projectRepository.save(existingProject);

            // 更新缓存
            cacheUtil.cacheProject(updatedProject);

            log.info("更新项目成功: {}", updatedProject.getCode());
            return ApiResponse.success(updatedProject);

        } catch (Exception e) {
            log.error("更新项目失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新项目失败，请稍后重试");
        }
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @param userId  当前用户ID
     * @param userRole 用户角色
     * @return 删除结果
     */
    @Operation(summary = "删除项目")
    @Transactional
    public ApiResponse<Void> deleteProject(@Parameter(description = "项目ID", example = "1") Long projectId,
                                       @Parameter(description = "当前用户ID") Long userId,
                                       @Parameter(description = "用户角色") User.UserRole userRole) {
        try {
            if (!projectRepository.existsById(projectId)) {
                return ApiResponse.error("项目不存在");
            }

            // 管理员可以删除任何项目
            // 非管理员只能删除自己创建的项目
            if (userRole != User.UserRole.ADMIN) {
                Optional<Project> projectOpt = projectRepository.findById(projectId);
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    if (!project.getCreateUserId().equals(userId)) {
                        return ApiResponse.error("没有权限删除此项目");
                    }
                }
            }

            projectRepository.deleteById(projectId);

            // 清除缓存
            cacheUtil.evictProjectCache(projectId);

            log.info("删除项目成功: {}", projectId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("删除项目失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除项目失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询项目
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    @Operation(summary = "根据ID查询项目")
    public ApiResponse<Project> getProjectById(@Parameter(description = "项目ID", example = "1") Long projectId) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(projectId);

            if (!projectOpt.isPresent()) {
                return ApiResponse.error("项目不存在");
            }

            return ApiResponse.success(projectOpt.get());

        } catch (Exception e) {
            log.error("查询项目失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目失败，请稍后重试");
        }
    }

    /**
     * 根据项目编码查询项目
     *
     * @param code 项目编码
     * @return 项目信息
     */
    @Operation(summary = "根据项目编码查询项目")
    public ApiResponse<Project> getProjectByCode(@Parameter(description = "项目编码", example = "ecmall") String code) {
        try {
            Optional<Project> projectOpt = cacheUtil.getProjectFromCache(code);

            if (!projectOpt.isPresent()) {
                return ApiResponse.error("项目不存在");
            }

            return ApiResponse.success(projectOpt.get());

        } catch (Exception e) {
            log.error("查询项目失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目失败，请稍后重试");
        }
    }

    /**
     * 查询所有项目
     *
     * @return 项目列表
     */
    @Operation(summary = "查询所有项目")
    public ApiResponse<List<Project>> getAllProjects() {
        try {
            List<Project> projects = projectRepository.findAll();
            return ApiResponse.success(projects);

        } catch (Exception e) {
            log.error("查询项目列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目列表失败，请稍后重试");
        }
    }

    /**
     * 查询启用状态的项目
     *
     * @return 项目列表
     */
    @Operation(summary = "查询启用状态的项目")
    public ApiResponse<List<Project>> getEnabledProjects() {
        try {
            List<Project> projects = projectRepository.findByEnabled(true);
            return ApiResponse.success(projects);

        } catch (Exception e) {
            log.error("查询项目列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目列表失败，请稍后重试");
        }
    }

    /**
     * 查询用户有权限访问的项目（带用户角色）
     *
     * @param userId    用户ID
     * @param userRole 用户角色
     * @return 项目列表（带用户角色）
     */
    @Operation(summary = "查询用户有权限访问的项目（带用户角色）")
    public ApiResponse<List<ProjectWithRoleDTO>> getAccessibleProjectsWithRole(@Parameter(description = "用户ID", example = "1") Long userId,
                                                                              @Parameter(description = "用户角色") User.UserRole userRole) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            User user = userOpt.get();
            List<Project> projects;
            List<ProjectWithRoleDTO> projectWithRoles = new ArrayList<>();

            if (userRole == User.UserRole.ADMIN) {
                // 管理员可以访问所有项目，角色为ADMIN
                projects = projectRepository.findAll();
                for (Project project : projects) {
                    String role = project.getCreateUserId().equals(userId) ? "CREATOR" : "ADMIN";
                    projectWithRoles.add(ProjectWithRoleDTO.fromProject(project, role));
                }
            } else {
                // 普通用户只能访问自己是创建者或管理员的项目
                projects = projectRepository.findAccessibleProjectsByUserId(userId);
                for (Project project : projects) {
                    String role = project.getCreateUserId().equals(userId) ? "CREATOR" : determineUserRole(project, userId);
                    projectWithRoles.add(ProjectWithRoleDTO.fromProject(project, role));
                }
            }

            return ApiResponse.success(projectWithRoles);

        } catch (Exception e) {
            log.error("查询项目列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目列表失败，请稍后重试");
        }
    }

    /**
     * 确定用户在项目中的角色
     *
     * @param project 项目
     * @param userId  用户ID
     * @return 角色名称
     */
    private String determineUserRole(Project project, Long userId) {
        if (project.getCreateUserId().equals(userId)) {
            return "CREATOR";
        }
        
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId);
        if (member.isPresent()) {
            return member.get().getRole().name();
        }
        
        return "MEMBER";
    }

    /**
     * 查询用户有权限访问的项目
     *
     * @param userId 用户ID
     * @param userRole 用户角色
     * @return 项目列表
     */
    @Operation(summary = "查询用户有权限访问的项目")
    public ApiResponse<List<Project>> getAccessibleProjects(@Parameter(description = "用户ID", example = "1") Long userId,
                                                       @Parameter(description = "用户角色") User.UserRole userRole) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            User user = userOpt.get();
            List<Project> projects;

            // 管理员可以访问所有项目
            if (userRole == User.UserRole.ADMIN) {
                projects = projectRepository.findAll();
            } else {
                // 普通用户只能访问自己是创建者或管理员的项目
                projects = projectRepository.findAccessibleProjectsByUserId(userId);
            }

            return ApiResponse.success(projects);

        } catch (Exception e) {
            log.error("查询项目列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询项目列表失败，请稍后重试");
        }
    }

    /**
     * 添加项目成员（已废弃，请使用ProjectMemberService）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 操作结果
     */
    @Operation(summary = "添加项目成员（已废弃）", deprecated = true)
    @Deprecated
    public ApiResponse<Void> addProjectMember(@Parameter(description = "项目ID", example = "1") Long projectId,
                                               @Parameter(description = "用户ID", example = "2") Long userId) {
        try {
            // 检查用户是否已经是项目成员
            java.util.Optional<com.carolcoral.mockserver.entity.ProjectMember> existingMember = 
                projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
            if (existingMember.isPresent()) {
                return ApiResponse.error("用户已是项目成员");
            }

            // 添加项目成员（默认为MEMBER角色）
            com.carolcoral.mockserver.entity.ProjectMember member = new com.carolcoral.mockserver.entity.ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(userId);
            member.setRole(com.carolcoral.mockserver.entity.ProjectMember.MemberRole.MEMBER);
            projectMemberRepository.save(member);

            log.info("添加项目成员成功: 项目={}, 用户={}", projectId, userId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("添加项目成员失败: {}", e.getMessage(), e);
            return ApiResponse.error("添加项目成员失败，请稍后重试");
        }
    }

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除项目成员")
    @Transactional
    public ApiResponse<Void> removeProjectMember(@Parameter(description = "项目ID", example = "1") Long projectId,
                                                  @Parameter(description = "用户ID", example = "2") Long userId) {
        try {
            // 检查用户是否在项目成员表中
            Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
            if (!memberOpt.isPresent()) {
                return ApiResponse.error("用户不是项目成员");
            }

            ProjectMember member = memberOpt.get();

            // 不允许移除创建者
            if (member.getRole() == ProjectMember.MemberRole.CREATOR) {
                return ApiResponse.error("不能移除项目创建者");
            }

            // 删除成员记录
            projectMemberRepository.deleteById(member.getId());

            log.info("移除项目成员成功: 项目={}, 用户={}", projectId, userId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("移除项目成员失败: {}", e.getMessage(), e);
            return ApiResponse.error("移除项目成员失败，请稍后重试");
        }
    }

    /**
     * 检查用户是否有项目权限（创建者或管理员）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否有权限
     */
    private boolean hasProjectPermission(Long projectId, Long userId) {
        return projectMemberRepository
                .findByProjectIdAndUserIdAndRole(projectId, userId, ProjectMember.MemberRole.CREATOR)
                .isPresent() ||
               projectMemberRepository
                .findByProjectIdAndUserIdAndRole(projectId, userId, ProjectMember.MemberRole.ADMIN)
                .isPresent();
    }
}
