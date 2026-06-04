package com.visitalk.behavior;

import com.visitalk.dto.WeeklyReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

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

    private final String familyId = "FAM_TEST_123";
    private final LocalDate mockToday = LocalDate.of(2026, 6, 4);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==========================================
    // C-3: 记录不足3条时必须抛出 400
    // ==========================================
    @Test
    void testC3_WeeklyReport_LessThanThree_ShouldThrowBadRequest() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        mockDbRows.add(createLogMock(4, "Sensory Overload", mockToday.atTime(10, 0)));
        mockDbRows.add(createLogMock(3, "Routine Disrupted", mockToday.minusDays(1).atTime(12, 0)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any(), any())).thenReturn(mockDbRows);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            analyticsService.generateWeeklyReportData(familyId, mockToday));

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("不足3条"));
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
    void testC5_ConsecutiveTriggerAlert_CrossMonth_ShouldAlert() {
        List<Map<String, Object>> mockDbRows = new ArrayList<>();
        mockDbRows.add(createLogMock(4, "Meltdown", LocalDateTime.of(2026, 5, 30, 22, 0)));
        mockDbRows.add(createLogMock(4, "Meltdown", LocalDateTime.of(2026, 5, 31, 11, 0)));
        mockDbRows.add(createLogMock(5, "Meltdown", LocalDateTime.of(2026, 6, 1, 9, 30)));

        when(jdbcTemplate.queryForList(anyString(), eq(familyId), any())).thenReturn(mockDbRows);

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
        map.put("occurred_at", time);
        return map;
    }
}
