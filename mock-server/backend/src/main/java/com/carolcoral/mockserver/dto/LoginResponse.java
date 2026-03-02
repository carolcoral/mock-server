package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 *
 * @author carolcoral
 */
@Schema(description = "登录响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "用户角色", example = "ADMIN")
    private String role;

    @Schema(description = "过期时间（毫秒）", example = "86400000")
    private Long expiresIn;
}
