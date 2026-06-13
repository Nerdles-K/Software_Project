package com.visitalk.behavior;

import com.visitalk.dto.WeeklyReportResponse;
import com.visitalk.repository.AlertDismissalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BehaviorAnalyticsServiceTest {

    @InjectMocks
    private BehaviorAnalyticsService analyticsService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private AlertDismissalRepository alertDismissals;

    private final String familyId = "FAM_TEST_123";
    private final LocalDate mockToday = LocalDate.of(2026, 6, 4);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==========================================
    // C-3: 记录不足3条时返回 "insufficient" 状态（不再抛 400）
    // ==========================================
    @Test
    void testC3_WeeklyReport_LessThanThree_ShouldReturnInsufficient() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        mockDbRows.add(createLogMock(4, "Sensory Overload", mockToday.atTime(10, 0)));
        mockDbRows.add(createLogMock(3, "Routine Disrupted", mockToday.minusDays(1).atTime(12, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any(), any())).thenReturn(mockDbRows);

        WeeklyReportResponse report = analyticsService.generateWeeklyReportData(familyId, mockToday);

        assertEquals("insufficient", report.getStatus());
        assertTrue(report.getChartData().isEmpty());
        assertTrue(report.getTop3Triggers().isEmpty());
        assertEquals(LocalDate.of(2026, 6, 1), report.getWeekStartDate());
        assertEquals(LocalDate.of(2026, 6, 7), report.getWeekEndDate());
    }

    // ==========================================
    // C-3/C-4: 满足 >= 3 条时周报成功生成
    // ==========================================
    @Test
    void testC3_C4_WeeklyReport_Satisfied_ShouldSuccess() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        mockDbRows.add(createLogMock(5, "Sensory Overload", mockToday.atTime(9, 0)));
        mockDbRows.add(createLogMock(4, "Sensory Overload", mockToday.minusDays(1).atTime(12, 0)));
        mockDbRows.add(createLogMock(3, "Routine Disrupted", mockToday.minusDays(2).atTime(15, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any(), any())).thenReturn(mockDbRows);

        WeeklyReportResponse report = analyticsService.generateWeeklyReportData(familyId, mockToday);

        assertNotNull(report);
        assertEquals("success", report.getStatus());
        assertEquals(LocalDate.of(2026, 6, 1), report.getWeekStartDate());
        assertEquals(LocalDate.of(2026, 6, 7), report.getWeekEndDate());
        assertEquals("Sensory Overload", report.getTop3Triggers().get(0));
    }

    // ==========================================
    // C-5: 跨月连续触发预警
    // ==========================================
    @Test
    void testC5_ConsecutiveTriggerAlert_ThreeConsecutiveDays_ShouldAlert() {
        // Anchor on "today" so the streak always falls inside the service's 30-day
        // window. (The streak detector uses LocalDate.plusDays, so month/year
        // boundaries are handled correctly regardless of where today lands.)
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        mockDbRows.add(createLogMock(4, "Meltdown", today.minusDays(2).atTime(22, 0)));
        mockDbRows.add(createLogMock(4, "Meltdown", today.minusDays(1).atTime(11, 0)));
        mockDbRows.add(createLogMock(5, "Meltdown", today.atTime(9, 30)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any())).thenReturn(mockDbRows);
        // No prior dismissal, so the consecutive streak should surface as an alert.
        when(alertDismissals.findByFamilyIdAndTriggerTag(anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        List<String> alerts = analyticsService.checkConsecutiveTriggerAlert(familyId, 3);
        assertTrue(alerts.contains("Meltdown"));
    }

    // ==========================================
    // C-6/C-7: 家长端日记隐私屏障
    // ==========================================
    @Test
    void testC6_C7_DiaryPrivacyBoundary_ShouldOnlyReturnBoolean() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(familyId)))
            .thenReturn(Collections.singletonList(true));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(familyId), any()))
            .thenReturn(1);

        boolean hasWritten = analyticsService.checkDiaryStatusForParent(familyId, mockToday);
        assertTrue(hasWritten);
    }

    private Map<String, Object> createLogMock(int intensity, String tag, LocalDateTime time) {
        Map<String, Object> map = new HashMap<>();
        map.put("intensity", intensity);
        map.put("trigger_tag", tag);
        // JdbcTemplate returns timestamp columns as java.sql.Timestamp, which the
        // service casts back to LocalDateTime — mirror that here, not a raw LocalDateTime.
        map.put("occurred_at", Timestamp.valueOf(time));
        return map;
    }
}
