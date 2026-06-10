/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.plugin;

import com.carolcoral.mockserver.dto.MockRequest;
import com.carolcoral.mockserver.dto.MockResponseDTO;

/**
 * 自定义响应转换器接口
 * <p>
 * 每个自定义接口（MockApi）可以独立配置一个专属的自定义报文处理方法。
 * 实现此接口并注册为Spring Bean后，即可在接口配置中通过类名引用。
 * </p>
 * <p>
 * 该转换器在原有的响应匹配、延迟、随机等基础功能完成后执行，
 * 仅对最终返回的报文数据进行转换，不会干扰或覆盖原有功能。
 * </p>
 *
 * @author carolcoral
 */
public interface CustomResponseTransformer {

    /**
     * 对Mock响应进行自定义转换处理
     * <p>
     * 该方法在基础响应流程（响应匹配、延迟计算、响应体解析等）完成之后调用。
     * 可以在此方法中对响应体（body）、状态码（statusCode）、响应头（headers）等进行任意修改。
     * </p>
     *
     * @param mockResponse 经过基础流程处理后的Mock响应DTO（包含statusCode, headers, body, delay等）
     * @param mockRequest  原始Mock请求（包含path, method, headers, params, body, pathParams等）
     * @param apiName      接口名称，方便日志记录
     * @param apiPath      接口路径，方便日志记录
     * @return 转换后的Mock响应DTO（不应返回null）
     */
    MockResponseDTO transform(MockResponseDTO mockResponse, MockRequest mockRequest,
                              String apiName, String apiPath);

    /**
     * 获取转换器名称，用于在管理界面展示
     *
     * @return 转换器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取转换器描述
     *
     * @return 转换器描述
     */
    default String getDescription() {
        return "";
    }
}
