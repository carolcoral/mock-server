package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.repository.MockApiRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import com.carolcoral.mockserver.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器 - 提供首页统计数据
 *
 * @author carolcoral
 */
@Tag(name = "仪表盘管理", description = "首页统计数据相关接口")
@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectRepository projectRepository;
    private final MockApiRepository mockApiRepository;
    private final UserRepository userRepository;

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    @Operation(summary = "获取仪表盘统计数据", description = "获取首页项目、接口、用户、今日请求等统计数据")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats(
            @Parameter(description = "是否包含今日请求数", example = "false") boolean includeTodayRequests) {
        try {
            log.info("获取仪表盘统计数据");

            Map<String, Object> stats = new HashMap<>();

            // 项目总数
            long projectCount = projectRepository.count();
            stats.put("projectCount", projectCount);

            // 接口总数
            long apiCount = mockApiRepository.count();
            stats.put("apiCount", apiCount);

            // 用户总数
            long userCount = userRepository.count();
            stats.put("userCount", userCount);

            // 今日请求数（由于没有请求日志表，暂时返回模拟数据）
            // TODO: 后续可以添加请求日志表来统计真实的请求数
            long requestCount = generateTodayRequestCount();
            stats.put("requestCount", requestCount);

            log.info("仪表盘统计数据获取成功: 项目={}, 接口={}, 用户={}, 今日请求={}",
                    projectCount, apiCount, userCount, requestCount);

            return ApiResponse.success(stats);

        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取统计数据失败，请稍后重试");
        }
    }

    /**
     * 生成今日请求数（模拟数据）
     * TODO: 后续可以添加请求日志表来统计真实的请求数
     *
     * @return 今日请求数
     */
    private long generateTodayRequestCount() {
        // 生成一个基于当前日期的伪随机数，确保同一天的数据一致
        LocalDate today = LocalDate.now();
        long seed = today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth();

        // 生成1000-9999之间的随机数
        java.util.Random random = new java.util.Random(seed);
        return 1000 + random.nextInt(9000);
    }
}
