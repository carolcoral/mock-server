/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * 自定义用户详情服务
 * 在 Spring Security 加载用户时，同步加载角色权限
 *
 * @author carolcoral
 * @since 2.3.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public CustomUserDetailsService(UserRepository userRepository,
                                     PermissionService permissionService) {
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        User user = userOpt.get();
        // 加载用户权限
        loadPermissions(user);
        return user;
    }

    /**
     * 通过用户名加载用户（支持邮箱登录）
     */
    public UserDetails loadUserByUsernameOrEmail(String account) throws UsernameNotFoundException {
        Optional<User> userOpt;
        if (account.contains("@")) {
            userOpt = userRepository.findByEmail(account);
        } else {
            userOpt = userRepository.findByUsername(account);
        }
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在: " + account);
        }
        User user = userOpt.get();
        loadPermissions(user);
        return user;
    }

    /**
     * 加载用户权限并设置到 User 对象上
     */
    public void loadPermissions(User user) {
        try {
            if (user.getRoleId() != null) {
                java.util.Set<String> permCodes = permissionService.getUserPermissionCodes(
                        Collections.singletonList(user.getRoleId()));
                user.setPermissions(permCodes);
                log.debug("用户 {} 权限加载完成: roleId={}, permissions={}",
                        user.getUsername(), user.getRoleId(), permCodes);
            } else {
                user.setPermissions(Collections.emptySet());
            }
        } catch (Exception e) {
            log.warn("加载用户权限失败: username={}, error={}", user.getUsername(), e.getMessage());
            user.setPermissions(Collections.emptySet());
        }
    }
}
