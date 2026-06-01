package com.visitalk.backend.service;

import com.visitalk.backend.dto.WeeklyReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class BehaviorAnalyticsServiceTest {

    @InjectMocks
    private BehaviorAnalyticsService analyticsService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private final String familyId = "FAM_TEST_123";
    // 强制锚定一个安全的测试日期：2026-06-04（星期四）
    private final LocalDate mockToday = LocalDate.of(2026, 6, 4); 

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==========================================
    // 验证 C-3：周报数据不足3条时必须硬性拦截
    // ==========================================
    @Test
    public void testC3_WeeklyReport_LessThanThree_ShouldThrowBadRequest() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        // 模拟当前自然周数据库里只有 2 条记录
        mockDbRows.add(createLogMock(4, "Sensory Overload", mockToday.atTime(10, 0)));
        mockDbRows.add(createLogMock(3, "Routine Disrupted", mockToday.minusDays(1).atTime(12, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any(), any())).thenReturn(mockDbRows);

        // 断言：系统必须抛出 400 BAD_REQUEST 业务中断异常，完美通过 C-3 边界检验
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            analyticsService.generateWeeklyReportData(familyId, mockToday);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("有效行为记录不足3条"));
    }

    // ==========================================
    // 验证 C-3 / C-4：满足 >= 3 条时周报顺利聚合吐出
    // ==========================================
    @Test
    public void testC3_C4_WeeklyReport_Satisfied_ShouldSuccess() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        // 补齐 3 条及以上有效记录
        mockDbRows.add(createLogMock(5, "Sensory Overload", mockToday.atTime(9, 0)));
        mockDbRows.add(createLogMock(4, "Sensory Overload", mockToday.minusDays(1).atTime(12, 0)));
        mockDbRows.add(createLogMock(3, "Routine Disrupted", mockToday.minusDays(2).atTime(15, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any(), any())).thenReturn(mockDbRows);

        WeeklyReportResponse report = analyticsService.generateWeeklyReportData(familyId, mockToday);

        assertNotNull(report);
        assertEquals("success", report.getStatus());
        assertEquals(LocalDate.of(2026, 6, 1), report.getWeekStartDate()); // 2026-06-01 周一
        assertEquals(LocalDate.of(2026, 6, 7), report.getWeekEndDate());   // 2026-06-07 周日
        assertEquals("Sensory Overload", report.getTop3Triggers().get(0));   // 正确提取高频标签
    }

    // ==========================================
    // 验证 C-5：智能干预算法跨月自然日精确对齐及触发
    // ==========================================
    @Test
    public void testC5_ConsecutiveTriggerAlert_CrossMonth_ShouldAlert() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        // 构造一个严苛的跨月无缝连续时间序列：5月30日、5月31日、6月1日
        mockDbRows.add(createLogMock(4, "Meltdown", LocalDateTime.of(2026, 5, 30, 22, 0)));
        mockDbRows.add(createLogMock(4, "Meltdown", LocalDateTime.of(2026, 5, 31, 11, 0)));
        mockDbRows.add(createLogMock(5, "Meltdown", LocalDateTime.of(2026, 6, 1, 9, 30)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any())).thenReturn(mockDbRows);

        List<String> alerts = analyticsService.checkConsecutiveTriggerAlert(familyId, 3);

        // 断言：跨月无缝连续 3 天，算法必须精准击中预警
        assertTrue(alerts.contains("Meltdown"));
    }

    // ==========================================
    // 验证 C-6 / C-7：家长端日记信息严格阻断隔离
    // ==========================================
    @Test
    public void testC6_C7_DiaryPrivacyBoundary_ShouldOnlyReturnBoolean() {
        // 1. 模拟系统总开关已开启 (C-7)
        when(jdbcTemplate.query(anyString(), any(), eq(familyId))).thenReturn(Collections.singletonList(true));
        // 2. 模拟当天存在日记记录
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(familyId), any())).thenReturn(1);

        boolean hasWritten = analyticsService.checkDiaryStatusForParent(familyId, mockToday);

        // 断言：家长端只能得到布尔状态
        assertTrue(hasWritten);
    }

    private Map<String, Object> createLogMock(int emotionLevel, String tag, LocalDateTime time) {
        Map<String, Object> map = new HashMap<>();
        map.put("emotion_level", emotionLevel);
        map.put("trigger_tag", tag);
        map.put("recorded_at", time);
        return map;
    }
}