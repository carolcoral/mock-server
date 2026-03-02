package com.carolcoral.mockserver.repository;

import com.carolcoral.mockserver.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository
 *
 * @author carolcoral
 */
@Tag(name = "用户管理", description = "用户数据访问接口")
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户Optional
     */
    @Operation(summary = "根据用户名查找用户")
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名和密码查找用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户Optional
     */
    @Operation(summary = "根据用户名和密码查找用户")
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户Optional
     */
    @Operation(summary = "根据邮箱查找用户")
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名模糊查询用户
     *
     * @param username 用户名（模糊）
     * @return 用户列表
     */
    @Operation(summary = "根据用户名模糊查询用户")
    List<User> findByUsernameLike(String username);

    /**
     * 根据角色查询用户
     *
     * @param role 角色
     * @return 用户列表
     */
    @Operation(summary = "根据角色查询用户")
    List<User> findByRole(User.UserRole role);

    /**
     * 根据项目ID查询项目成员
     *
     * @param projectId 项目ID
     * @return 用户列表
     */
    @Operation(summary = "根据项目ID查询项目成员")
    @Query("SELECT u FROM Project p JOIN p.members u WHERE p.id = :projectId")
    List<User> findMembersByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询所有启用状态的用户
     *
     * @return 用户列表
     */
    @Operation(summary = "查询所有启用状态的用户")
    List<User> findByEnabledTrue();

    /**
     * 判断用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Operation(summary = "判断用户名是否存在")
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Operation(summary = "判断邮箱是否存在")
    boolean existsByEmail(String email);
}
