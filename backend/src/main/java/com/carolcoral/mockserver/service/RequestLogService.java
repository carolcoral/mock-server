/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.RequestLog;
import com.carolcoral.mockserver.repository.RequestLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 请求日志服务类
 * 用于记录和统计接口请求日志
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    /**
     * 异步记录请求日志
     * 使用独立事务，避免影响主流程
     *
     * @param mockApi 接口信息
     * @param request HTTP请求对象
     * @param statusCode 响应状态码
     * @param responseTime 响应时间（毫秒）
     * @param userId 用户ID（可选）
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequestAsync(MockApi mockApi, HttpServletRequest request, int statusCode, long responseTime, Long userId) {
        try {
            RequestLog logEntry = RequestLog.builder()
                    .mockApiId(mockApi.getId())
                    .projectId(mockApi.getProject().getId())
                    .method(mockApi.getMethod().name())
                    .path(mockApi.getPath())
                    .requestTime(LocalDateTime.now())
                    .statusCode(statusCode)
                    .responseTime(responseTime)
                    .requestIp(getClientIp(request))
                    .userId(userId)
                    .build();

            requestLogRepository.save(logEntry);
            log.debug("请求日志已记录: 接口={}, 项目={}, 方法={}, 路径={}, 状态码={}, 响应时间={}ms",
                    mockApi.getName(), mockApi.getProject().getName(), mockApi.getMethod(),
                    mockApi.getPath(), statusCode, responseTime);
        } catch (Exception e) {
            // 记录日志失败不应该影响主流程
            log.error("记录请求日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取今天的请求数量
     *
     * @return 今天的请求数量
     */
    public long getTodayRequestCount() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return requestLogRepository.countTodayRequests(todayStart, todayEnd);
    }

    /**
     * 获取指定项目和今天的请求数量
     *
     * @param projectId 项目ID
     * @return 指定项目今天的请求数量
     */
    public long getTodayRequestCountByProject(Long projectId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return requestLogRepository.countByProjectIdAndRequestTimeBetween(projectId, todayStart, todayEnd);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
