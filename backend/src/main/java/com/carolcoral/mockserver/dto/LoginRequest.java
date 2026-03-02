package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author carolcoral
 */
@Schema(description = "登录请求")
@Data
public class LoginRequest {

    @Schema(description = "用户名", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "Admin@123", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
