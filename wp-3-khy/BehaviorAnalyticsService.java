package com.visitalk.backend.service;

import com.visitalk.backend.dto.WeeklyReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BehaviorAnalyticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==========================================
    // C-3 & C-4: 自然周报聚合算法与硬性阈值拦截
    // ==========================================
    public WeeklyReportResponse generateWeeklyReportData(String familyId, LocalDate targetDate) {
        // 1. 严格计算标准自然周边界 (周一 00:00:00 至 周日 23:59:59)
        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);
        
        LocalDateTime startFilter = monday.atStartOfDay();
        LocalDateTime endFilter = sunday.atTime(23, 59, 59);

        // 2. 捞取该自然周内的所有原始行为数据
        String sql = "SELECT emotion_level, trigger_tag, recorded_at FROM behavior_logs " +
                     "WHERE family_id = ? AND recorded_at BETWEEN ? AND ? ORDER BY recorded_at ASC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, familyId, startFilter, endFilter);

        // 3. 【核心 AC 拦截边界】: 有效记录不足 3 条，无条件强行终止，抛出 400 业务级阻断
        if (rows.size() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前周有效行为记录不足3条，无法生成周报。");
        }

        // 4. 算法聚合 A: 按日期对齐情绪中位数，转化为前端图表所需的结构化格式
        Map<String, List<Integer>> dailyEmotions = new LinkedHashMap<>();
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            dailyEmotions.put(d.toString(), new ArrayList<>());
        }
        
        Map<String, Integer> triggerCounts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDateTime recordedAt = (LocalDateTime) row.get("recorded_at");
            int level = (int) row.get("emotion_level");
            String tag = (String) row.get("trigger_tag");

            String dateStr = recordedAt.toLocalDate().toString();
            if (dailyEmotions.containsKey(dateStr)) {
                dailyEmotions.get(dateStr).add(level);
            }
            triggerCounts.put(tag, triggerCounts.getOrDefault(tag, 0) + 1);
        }

        List<Map<String, Object>> chartData = new ArrayList<>();
        dailyEmotions.forEach((dateStr, levels) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            // 计算平均值或中位数作为折线图锚点
            double avg = levels.isEmpty() ? 0.0 : levels.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            point.put("avg_emotion", Math.round(avg * 10.0) / 10.0);
            chartData.add(point);
        });

        // 5. 算法聚合 B: 筛选 Top 3 高频触发源
        List<String> top3 = triggerCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 装配返回
        WeeklyReportResponse response = new WeeklyReportResponse();
        response.setStatus("success");
        response.setWeekStartDate(monday);
        response.setWeekEndDate(sunday);
        response.setChartData(chartData);
        response.setTop3Triggers(top3);
        return response;
    }

    // ==========================================
    // C-5: 智能干预预警算法 (自然日连续 3 天漏斗扫描)
    // ==========================================
    public List<String> checkConsecutiveTriggerAlert(String familyId, int consecutiveDays) {
        // 获取近 10 天所有数据进行滑动窗口检测 (支持高弹性的跨月、跨周边界对齐)
        String sql = "SELECT trigger_tag, recorded_at FROM behavior_logs WHERE family_id = ? " +
                     "AND recorded_at >= ? ORDER BY recorded_at ASC";
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, familyId, tenDaysAgo);

        // 按标签分组，收集所有触发过的独立【自然日】(过滤掉具体时分秒对连续性的干扰)
        Map<String, Set<LocalDate>> tagDatesMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String tag = (String) row.get("trigger_tag");
            LocalDateTime recordedAt = (LocalDateTime) row.get("recorded_at");
            tagDatesMap.computeIfAbsent(tag, k -> new TreeSet<>()).add(recordedAt.toLocalDate());
        }

        List<String> highRiskAlerts = new ArrayList<>();

        // 核心滑动窗口算法：检查是否有任意标签的自然日序列能够无缝连成一个长度为 consecutiveDays 的链条
        tagDatesMap.forEach((tag, dates) -> {
            List<LocalDate> sortedDates = new ArrayList<>(dates);
            int consecutiveCount = 1;
            boolean triggered = false;

            for (int i = 0; i < sortedDates.size() - 1; i++) {
                // 如果后一天刚好等于前一天 + 1天，说明自然日严格连续
                if (sortedDates.get(i).plusDays(1).equals(sortedDates.get(i + 1))) {
                    consecutiveCount++;
                    if (consecutiveCount >= consecutiveDays) {
                        triggered = true;
                    }
                } else {
                    consecutiveCount = 1; // 出现断点，计数器重置
                }
            }

            if (triggered) {
                highRiskAlerts.add(tag);
            }
        });

        return highRiskAlerts;
    }

    // ==========================================
    // C-6 & C-7: 医疗级儿童隐私屏障安全拦截网关
    // ==========================================
    public boolean checkDiaryStatusForParent(String familyId, LocalDate targetDate) {
        // 1. 拦截前置校验：校验 C-7 全局独立空间的开关状态
        String configSql = "SELECT diary_feature_enabled FROM system_configs WHERE family_id = ?";
        List<Boolean> configs = jdbcTemplate.query(configSql, (rs, rowNum) -> rs.getBoolean(1), familyId);
        boolean isEnabled = !configs.isEmpty() && configs.get(0);
        
        if (!isEnabled) {
            return false; // 功能未开启，直接截断
        }

        // 2. 核心隐私阻断拦截 (C-6)
        // 仅查询是否存在 Count 聚合，绝对不在 SQL 中出现 emotion_card 或 color_doodle
        String diarySql = "SELECT COUNT(1) FROM child_diaries WHERE family_id = ? AND created_date = ?";
        Integer count = jdbcTemplate.queryForObject(diarySql, Integer.class, familyId, targetDate);
        
        // 3. 家长端只能获取一个清洁的布尔值 (AC: 写了/没写)，敏感信息完全隔离在应用层与表现层之外
        return count != null && count > 0;
    }
}