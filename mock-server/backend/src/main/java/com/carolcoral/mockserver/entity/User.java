package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体类
 *
 * @author carolcoral
 */
@Schema(description = "用户实体")
@Entity
@Table(name = "t_user")
@Data
public class User implements UserDetails {

    @Schema(description = "用户ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "用户名", example = "admin")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Schema(description = "密码（加密存储）")
    @Column(nullable = false)
    private String password;

    @Schema(description = "邮箱", example = "user@example.com")
    @Column(length = 100)
    private String email;

    @Schema(description = "用户角色", example = "ADMIN", allowableValues = {"ADMIN", "USER"})
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Schema(description = "是否启用", example = "true")
    @Column(nullable = false)
    private Boolean enabled = true;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN,  // 管理员
        USER    // 普通用户
    }
}
