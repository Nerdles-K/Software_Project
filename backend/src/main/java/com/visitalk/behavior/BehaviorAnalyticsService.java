package com.visitalk.behavior;

import com.visitalk.dto.WeeklyReportResponse;
import com.visitalk.repository.AlertDismissalRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BehaviorAnalyticsService {

    private final JdbcTemplate jdbcTemplate;
    private final AlertDismissalRepository alertDismissals;

    public BehaviorAnalyticsService(JdbcTemplate jdbcTemplate, AlertDismissalRepository alertDismissals) {
        this.jdbcTemplate = jdbcTemplate;
        this.alertDismissals = alertDismissals;
    }

    // ==========================================
    // C-3 & C-4: 自然周报聚合算法 (已适配 schema: behavior_event / users)
    // ==========================================
    public WeeklyReportResponse generateWeeklyReportData(String familyId, LocalDate targetDate) {
        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);

        LocalDateTime startFilter = monday.atStartOfDay();
        LocalDateTime endFilter = sunday.atTime(23, 59, 59);

        // JOIN users 表以按 family_id 过滤
        String sql = """
            SELECT be.intensity, unnest(be.trigger_tags) AS trigger_tag, be.occurred_at
            FROM behavior_event be
            JOIN users u ON be.parent_id = u.id
            WHERE u.family_id = ? AND be.occurred_at BETWEEN ? AND ?
            ORDER BY be.occurred_at ASC
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, familyId, startFilter, endFilter);

        if (rows.size() < 3) {
            WeeklyReportResponse empty = new WeeklyReportResponse();
            empty.setStatus("insufficient");
            empty.setMessage("This week has fewer than 3 recorded events; report not generated.");
            empty.setWeekStartDate(monday);
            empty.setWeekEndDate(sunday);
            empty.setChartData(Collections.emptyList());
            empty.setTop3Triggers(Collections.emptyList());
            return empty;
        }

        // 按日期对齐情绪
        Map<String, List<Integer>> dailyEmotions = new LinkedHashMap<>();
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            dailyEmotions.put(d.toString(), new ArrayList<>());
        }

        Map<String, Integer> triggerCounts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDateTime occurredAt = ((Timestamp) row.get("occurred_at")).toLocalDateTime();
            int level = ((Number) row.get("intensity")).intValue();
            String tag = (String) row.get("trigger_tag");

            String dateStr = occurredAt.toLocalDate().toString();
            if (dailyEmotions.containsKey(dateStr)) {
                dailyEmotions.get(dateStr).add(level);
            }
            triggerCounts.put(tag, triggerCounts.getOrDefault(tag, 0) + 1);
        }

        List<Map<String, Object>> chartData = new ArrayList<>();
        dailyEmotions.forEach((dateStr, levels) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            double avg = levels.isEmpty() ? 0.0
                : levels.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            point.put("avg_emotion", Math.round(avg * 10.0) / 10.0);
            chartData.add(point);
        });

        List<String> top3 = triggerCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

        WeeklyReportResponse response = new WeeklyReportResponse();
        response.setStatus("success");
        response.setWeekStartDate(monday);
        response.setWeekEndDate(sunday);
        response.setChartData(chartData);
        response.setTop3Triggers(top3);
        return response;
    }

    // ==========================================
    // C-5: 连续触发预警 (滑动窗口检测，已适配 behavior_event)
    // ==========================================
    /**
     * Returns the trigger tags whose most recent consecutive run is `consecutiveDays` or longer
     * AND whose run started AFTER any prior dismissal (so dismissed alerts don't re-fire
     * until a fresh consecutive streak begins).
     */
    public List<String> checkConsecutiveTriggerAlert(String familyId, int consecutiveDays) {
        String sql = """
            SELECT unnest(be.trigger_tags) AS trigger_tag, be.occurred_at
            FROM behavior_event be
            JOIN users u ON be.parent_id = u.id
            WHERE u.family_id = ? AND be.occurred_at >= ?
            ORDER BY be.occurred_at ASC
            """;
        LocalDateTime windowStart = LocalDateTime.now().minusDays(30);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, familyId, windowStart);

        Map<String, TreeSet<LocalDate>> tagDates = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String tag = (String) row.get("trigger_tag");
            LocalDateTime occurredAt = ((Timestamp) row.get("occurred_at")).toLocalDateTime();
            tagDates.computeIfAbsent(tag, k -> new TreeSet<>()).add(occurredAt.toLocalDate());
        }

        List<String> alerts = new ArrayList<>();
        tagDates.forEach((tag, dates) -> {
            LocalDate runStart = runStartOfLatestConsecutiveStreak(new ArrayList<>(dates), consecutiveDays);
            if (runStart == null) return;

            // Suppress if there is a dismissal *after* the start of this current streak.
            boolean dismissed = alertDismissals.findByFamilyIdAndTriggerTag(familyId, tag).stream()
                .anyMatch(d -> !d.getDismissedAt().toLocalDate().isBefore(runStart));
            if (!dismissed) alerts.add(tag);
        });
        return alerts;
    }

    private LocalDate runStartOfLatestConsecutiveStreak(List<LocalDate> sortedDates, int minLen) {
        LocalDate runStart = null;
        int run = 0;
        LocalDate lastSeen = null;
        for (LocalDate d : sortedDates) {
            if (lastSeen != null && d.equals(lastSeen.plusDays(1))) {
                run++;
            } else {
                run = 1;
                runStart = d;
            }
            lastSeen = d;
        }
        return run >= minLen ? runStart : null;
    }

    // ==========================================
    // C-6 & C-7: 儿童隐私屏障 — 家长只能看到布尔值
    // ==========================================
    public boolean checkDiaryStatusForParent(String familyId, LocalDate targetDate) {
        // 检查 system_configs 开关
        String configSql = "SELECT diary_feature_enabled FROM system_configs WHERE family_id = ?";
        List<Boolean> configs = jdbcTemplate.query(configSql,
            (rs, rowNum) -> rs.getBoolean(1), familyId);
        if (configs.isEmpty() || !configs.get(0)) {
            return false;
        }

        // 只查聚合 COUNT，不暴露 emotion_card_id / doodle_url
        String diarySql = """
            SELECT COUNT(1) FROM diary_entry de
            JOIN users u ON de.child_id = u.id
            WHERE u.family_id = ? AND DATE(de.created_at) = ?
            """;
        Integer count = jdbcTemplate.queryForObject(diarySql, Integer.class, familyId, targetDate);
        return count != null && count > 0;
    }
}
