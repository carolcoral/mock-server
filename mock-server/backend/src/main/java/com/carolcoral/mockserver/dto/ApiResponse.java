package com.carolcoral.mockserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用API响应DTO
 *
 * @author carolcoral
 */
@Schema(description = "通用API响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "响应码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳", example = "1703123456789")
    private Long timestamp;

    /**
     * 成功响应
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    /**
     * 错误响应（自定义状态码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 未授权响应
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> unauthorized() {
        return error(401, "未授权访问");
    }

    /**
     * 禁止访问响应
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> forbidden() {
        return error(403, "禁止访问");
    }

    /**
     * 未找到响应
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> notFound() {
        return error(404, "资源未找到");
    }
}
