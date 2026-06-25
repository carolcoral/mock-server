/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计控制器 - 提供系统统计数据
 *
 * @author carolcoral
 */
@Tag(name = "统计管理", description = "系统统计数据相关接口，仅管理员可访问")
@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {
    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    private final EntityManager entityManager;

    public StatisticsController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * 获取请求频率统计（按天或按小时）
     *
     * @param days        统计天数
     * @param granularity 粒度：daily 或 hourly
     * @return 时间序列数据
     */
    @Operation(summary = "获取请求频率统计", description = "按天或按小时统计接口请求次数")
    @GetMapping("/request-frequency")
    public ApiResponse<Map<String, Object>> getRequestFrequency(
            @Parameter(description = "统计天数", example = "7") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "粒度：daily(按天) / hourly(按小时)", example = "daily") @RequestParam(defaultValue = "daily") String granularity) {

        try {
            LocalDateTime startTime;
            if ("hourly".equals(granularity)) {
                // 按小时统计时最多查最近24小时
                startTime = LocalDateTime.now().minusHours(24);
            } else {
                startTime = LocalDateTime.now().minusDays(days).with(LocalTime.MIN);
            }

            // SQLite 中 LocalDateTime 存储为毫秒时间戳，需除以 1000 并用 'unixepoch' 修饰符
            long startTimeMillis = startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            String groupBy;
            if ("hourly".equals(granularity)) {
                groupBy = "strftime('%Y-%m-%d %H:00', r.request_time / 1000, 'unixepoch')";
            } else {
                groupBy = "strftime('%Y-%m-%d', r.request_time / 1000, 'unixepoch')";
            }

            String sql = "SELECT " + groupBy + " as timeKey, COUNT(*) as cnt " +
                    "FROM t_request_log r " +
                    "WHERE r.request_time >= :startTime AND r.request_time IS NOT NULL " +
                    "GROUP BY timeKey " +
                    "ORDER BY timeKey ASC";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startTime", startTimeMillis);

            List<Object[]> rows = query.getResultList();
            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();

            for (Object[] row : rows) {
                if (row[0] != null) {
                    labels.add(String.valueOf(row[0]));
                    values.add(((Number) row[1]).longValue());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("labels", labels);
            result.put("values", values);
            result.put("granularity", granularity);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取请求频率统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取请求频率统计失败");
        }
    }

    /**
     * 获取来源IP统计（TOP N）
     *
     * @param days 统计天数
     * @return IP统计数据
     */
    @Operation(summary = "获取来源IP统计", description = "统计请求来源IP的TOP排名")
    @GetMapping("/source-ips")
    public ApiResponse<Map<String, Object>> getSourceIps(
            @Parameter(description = "统计天数", example = "7") @RequestParam(defaultValue = "7") int days) {

        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days).with(LocalTime.MIN);
            long startTimeMillis = startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            String sql = "SELECT r.request_ip as ip, COUNT(*) as cnt " +
                    "FROM t_request_log r " +
                    "WHERE r.request_time >= :startTime AND r.request_ip IS NOT NULL AND r.request_ip != '' " +
                    "GROUP BY r.request_ip " +
                    "ORDER BY cnt DESC " +
                    "LIMIT 20";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startTime", startTimeMillis);

            List<Object[]> rows = query.getResultList();
            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();

            for (Object[] row : rows) {
                if (row[0] != null) {
                    labels.add(String.valueOf(row[0]));
                    values.add(((Number) row[1]).longValue());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("labels", labels);
            result.put("values", values);
            result.put("totalIps", labels.size());

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取来源IP统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取来源IP统计失败");
        }
    }

    /**
     * 获取项目和接口新增趋势
     *
     * @param days 统计天数
     * @return 创建趋势数据
     */
    @Operation(summary = "获取新增趋势统计", description = "统计项目、接口的每日新增数量")
    @GetMapping("/creation-trend")
    public ApiResponse<Map<String, Object>> getCreationTrend(
            @Parameter(description = "统计天数", example = "30") @RequestParam(defaultValue = "30") int days) {

        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days).with(LocalTime.MIN);

            // 查询项目每日新增
            String projectSql = "SELECT DATE(p.create_time) as dt, COUNT(*) as cnt " +
                    "FROM t_project p " +
                    "WHERE p.create_time >= :startTime " +
                    "GROUP BY dt ORDER BY dt ASC";
            Query projectQuery = entityManager.createNativeQuery(projectSql);
            projectQuery.setParameter("startTime", startTime);
            List<Object[]> projectRows = projectQuery.getResultList();

            // 查询接口每日新增
            String apiSql = "SELECT DATE(a.create_time) as dt, COUNT(*) as cnt " +
                    "FROM t_mock_api a " +
                    "WHERE a.create_time >= :startTime " +
                    "GROUP BY dt ORDER BY dt ASC";
            Query apiQuery = entityManager.createNativeQuery(apiSql);
            apiQuery.setParameter("startTime", startTime);
            List<Object[]> apiRows = apiQuery.getResultList();

            // 构建完整日期范围
            List<String> labels = new ArrayList<>();
            List<Long> projectValues = new ArrayList<>();
            List<Long> apiValues = new ArrayList<>();

            LocalDate today = LocalDate.now();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.toString();
                labels.add(dateStr);

                projectValues.add(findCount(projectRows, dateStr));
                apiValues.add(findCount(apiRows, dateStr));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("labels", labels);
            result.put("projectValues", projectValues);
            result.put("apiValues", apiValues);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取新增趋势统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取新增趋势统计失败");
        }
    }

    /**
     * 获取系统IOPS统计（每秒请求数）
     *
     * @param minutes 统计最近N分钟
     * @return IOPS时间序列数据
     */
    @Operation(summary = "获取IOPS统计", description = "按分钟/秒统计请求数，用于系统性能监控")
    @GetMapping("/iops")
    public ApiResponse<Map<String, Object>> getIops(
            @Parameter(description = "统计最近N分钟", example = "60") @RequestParam(defaultValue = "60") int minutes) {

        try {
            if (minutes > 1440) minutes = 1440; // 最多24小时

            LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
            // SQLite 中 LocalDateTime 存储为毫秒时间戳
            long startTimeMillis = startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            // 短时间窗口（≤60分钟）按秒分组，长窗口按分钟分组
            // SQLite strftime 需要秒级 Unix 时间戳 + 'unixepoch' 修饰符
            String timeFormat;
            String unit;
            double divisor;
            if (minutes <= 60) {
                // 按秒分组，每桶即为 1 秒的请求量，直接作为 IOPS
                timeFormat = "%Y-%m-%d %H:%M:%S";
                unit = "req/s";
                divisor = 1.0;
            } else {
                // 按分钟分组，显示为 req/min（低流量下比 req/s 更有意义）
                timeFormat = "%Y-%m-%d %H:%M";
                unit = "req/min";
                divisor = 1.0;
            }

            String sql = "SELECT strftime('" + timeFormat + "', r.request_time / 1000, 'unixepoch') as timeKey, COUNT(*) as cnt " +
                    "FROM t_request_log r " +
                    "WHERE r.request_time >= :startTime AND r.request_time IS NOT NULL " +
                    "GROUP BY timeKey " +
                    "ORDER BY timeKey ASC";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startTime", startTimeMillis);

            List<Object[]> rows = query.getResultList();
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();

            for (Object[] row : rows) {
                if (row[0] != null) {
                    labels.add(String.valueOf(row[0]));
                    long count = ((Number) row[1]).longValue();
                    values.add(Math.round(count / divisor * 100.0) / 100.0);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("labels", labels);
            result.put("values", values);
            result.put("unit", unit);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取IOPS统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取IOPS统计失败");
        }
    }

    /**
     * 从查询结果中查找指定日期的计数
     */
    private long findCount(List<Object[]> rows, String dateStr) {
        for (Object[] row : rows) {
            if (dateStr.equals(String.valueOf(row[0]))) {
                return ((Number) row[1]).longValue();
            }
        }
        return 0;
    }

    /**
     * 获取 AI 调用统计（按年/月/日，统计每位用户的调用次数）
     *
     * @param granularity 粒度：yearly / monthly / daily
     * @return 各用户在各时间段的调用次数
     */
    @Operation(summary = "获取 AI 调用统计", description = "按年/月/日统计每位用户的 AI 调用次数，无调用用户默认为0")
    @GetMapping("/ai-calls")
    public ApiResponse<Map<String, Object>> getAiCallStats(
            @Parameter(description = "粒度：yearly(按年) / monthly(按月) / daily(按日)", example = "monthly")
            @RequestParam(defaultValue = "monthly") String granularity) {

        try {
            // 获取所有用户
            String userSql = "SELECT id, username FROM t_user";
            List<Object[]> userRows = entityManager.createNativeQuery(userSql).getResultList();
            List<Map<String, Object>> allUsers = new ArrayList<>();
            for (Object[] row : userRows) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", ((Number) row[0]).longValue());
                userMap.put("username", String.valueOf(row[1]));
                userMap.put("callCount", 0L);
                allUsers.add(userMap);
            }

            // 按粒度构建分组格式
            String timeFormat;
            String timeLabelFormat;
            if ("yearly".equals(granularity)) {
                timeFormat = "%Y";
                timeLabelFormat = "%Y";
            } else if ("daily".equals(granularity)) {
                timeFormat = "%Y-%m-%d";
                timeLabelFormat = "%Y-%m-%d";
            } else {
                // monthly (default)
                timeFormat = "%Y-%m";
                timeLabelFormat = "%Y-%m";
            }

            // 查询 AI 调用统计：按用户+时间段分组
            String statsSql = "SELECT a.username, strftime('" + timeFormat + "', a.call_time) as timeKey, COUNT(*) as cnt " +
                    "FROM t_ai_call_log a " +
                    "WHERE a.call_time IS NOT NULL " +
                    "GROUP BY a.username, timeKey " +
                    "ORDER BY timeKey ASC, cnt DESC";

            List<Object[]> statsRows = entityManager.createNativeQuery(statsSql).getResultList();

            // 构建时间段集合
            java.util.Set<String> timeKeySet = new java.util.LinkedHashSet<>();
            for (Object[] row : statsRows) {
                if (row[1] != null) {
                    timeKeySet.add(String.valueOf(row[1]));
                }
            }
            List<String> timeLabels = new ArrayList<>(timeKeySet);

            // 构建 username -> (timeKey -> count) 映射
            Map<String, Map<String, Long>> userTimeCount = new LinkedHashMap<>();
            for (Object[] row : statsRows) {
                String username = row[0] != null ? String.valueOf(row[0]) : "unknown";
                String timeKey = row[1] != null ? String.valueOf(row[1]) : "";
                long count = ((Number) row[2]).longValue();
                userTimeCount.computeIfAbsent(username, k -> new LinkedHashMap<>()).put(timeKey, count);
            }

            // 构建每位用户的序列数据（无调用默认0）
            List<Map<String, Object>> userSeries = new ArrayList<>();
            for (Map<String, Object> user : allUsers) {
                String username = (String) user.get("username");
                Map<String, Long> timeMap = userTimeCount.getOrDefault(username, new LinkedHashMap<>());
                List<Long> data = new ArrayList<>();
                long totalCount = 0L;
                for (String tk : timeLabels) {
                    long cnt = timeMap.getOrDefault(tk, 0L);
                    data.add(cnt);
                    totalCount += cnt;
                }
                Map<String, Object> series = new LinkedHashMap<>();
                series.put("username", username);
                series.put("data", data);
                series.put("totalCount", totalCount);
                userSeries.add(series);
            }

            // 计算汇总（所有用户在每个时间段的总和）
            List<Long> totalData = new ArrayList<>();
            for (String tk : timeLabels) {
                long sum = 0L;
                for (Map<String, Object> series : userSeries) {
                    @SuppressWarnings("unchecked")
                    List<Long> data = (List<Long>) series.get("data");
                    int idx = timeLabels.indexOf(tk);
                    if (idx >= 0 && idx < data.size()) {
                        sum += data.get(idx);
                    }
                }
                totalData.add(sum);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("granularity", granularity);
            result.put("timeLabels", timeLabels);
            result.put("userSeries", userSeries);
            result.put("totalData", totalData);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取 AI 调用统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取 AI 调用统计失败");
        }
    }

    @Operation(summary = "诊断：请求日志数据状态", description = "检查 t_request_log 表中 request_time 字段的分布情况")
    @GetMapping("/diagnose-logs")
    public ApiResponse<Map<String, Object>> diagnoseRequestLogs() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 总记录数、非 NULL 数、NULL 数
            String countSql = "SELECT COUNT(*) as total, SUM(CASE WHEN request_time IS NOT NULL THEN 1 ELSE 0 END) as not_null_cnt, SUM(CASE WHEN request_time IS NULL THEN 1 ELSE 0 END) as null_cnt FROM t_request_log";
            List<Object[]> countRows = entityManager.createNativeQuery(countSql).getResultList();
            Object[] counts = (Object[]) countRows.get(0);
            result.put("totalCount", ((Number) counts[0]).longValue());
            result.put("notNullCount", ((Number) counts[1]).longValue());
            result.put("nullCount", ((Number) counts[2]).longValue());

            // 最近 5 条有时间的记录
            String recentSql = "SELECT id, mock_api_id, method, path, request_time, status_code FROM t_request_log WHERE request_time IS NOT NULL ORDER BY id DESC LIMIT 5";
            List<Object[]> recentRows = entityManager.createNativeQuery(recentSql).getResultList();
            List<Map<String, Object>> recentLogs = new ArrayList<>();
            for (Object[] row : recentRows) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", row[0]);
                log.put("mockApiId", row[1]);
                log.put("method", row[2]);
                log.put("path", row[3]);
                log.put("requestTime", row[4] != null ? String.valueOf(row[4]) : null);
                log.put("statusCode", row[5]);
                recentLogs.add(log);
            }
            result.put("recentValidLogs", recentLogs);

            // 最近 5 条无时间的记录
            String nullSql = "SELECT id, mock_api_id, method, path, status_code FROM t_request_log WHERE request_time IS NULL ORDER BY id DESC LIMIT 5";
            List<Object[]> nullRows = entityManager.createNativeQuery(nullSql).getResultList();
            List<Map<String, Object>> nullLogs = new ArrayList<>();
            for (Object[] row : nullRows) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", row[0]);
                log.put("mockApiId", row[1]);
                log.put("method", row[2]);
                log.put("path", row[3]);
                log.put("statusCode", row[4]);
                nullLogs.add(log);
            }
            result.put("recentNullLogs", nullLogs);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("诊断请求日志失败: {}", e.getMessage(), e);
            return ApiResponse.error("诊断失败: " + e.getMessage());
        }
    }
}
