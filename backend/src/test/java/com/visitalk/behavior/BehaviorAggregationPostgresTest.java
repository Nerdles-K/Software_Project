package com.visitalk.behavior;

import com.visitalk.dto.WeeklyReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P3 — the ONE layer H2 cannot reach: behavior_event uses Postgres {@code text[]}
 * and the analytics SQL relies on {@code unnest(...)}. This boots the app against a
 * REAL Postgres (Testcontainers) with the production schema.sql applied, then drives
 * {@link BehaviorAnalyticsService} against actual array columns.
 *
 * <p>Auto-skipped when Docker is unavailable ({@code disabledWithoutDocker = true}),
 * so the suite stays green on machines without Docker; runs for real in CI / locally
 * where Docker is present.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class BehaviorAggregationPostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // Run the real schema.sql (BIGSERIAL / TEXT[] etc.) and keep Hibernate off it.
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired private BehaviorAnalyticsService analytics;
    @Autowired private JdbcTemplate jdbc;

    private static final String FAMILY = "FAMPGIT";
    private Long parentId;
    // Anchor on the current week's Monday so all three events land in ONE report
    // week (Mon–Sun) regardless of which weekday the run happens on — avoids the
    // week-boundary flakiness a raw now().minusDays(...) would introduce.
    private static final LocalDate WEEK_MONDAY = LocalDate.now().with(DayOfWeek.MONDAY);

    @BeforeEach
    void seed() {
        // Clean any rows from a previous test run, then seed a parent + child.
        jdbc.update("DELETE FROM behavior_event WHERE parent_id IN (SELECT id FROM users WHERE family_id = ?)", FAMILY);
        jdbc.update("DELETE FROM users WHERE family_id = ?", FAMILY);

        parentId = jdbc.queryForObject(
            "INSERT INTO users(email, password_hash, role, family_id) VALUES (?,?,?,?) RETURNING id",
            Long.class, uniq("parent"), "x", "parent", FAMILY);
        Long childId = jdbc.queryForObject(
            "INSERT INTO users(email, password_hash, role, family_id) VALUES (?,?,?,?) RETURNING id",
            Long.class, uniq("child"), "x", "child", FAMILY);

        // Mon/Tue/Wed of this week — "Noise" on all three (a real 3-day streak),
        // plus "Routine" once for top-trigger variety. Real text[] via ::text[] cast.
        insertEvent(childId, 5, "{Noise}", WEEK_MONDAY);
        insertEvent(childId, 4, "{Noise,Routine}", WEEK_MONDAY.plusDays(1));
        insertEvent(childId, 3, "{Noise}", WEEK_MONDAY.plusDays(2));
    }

    @Test
    void weeklyReport_aggregatesRealPostgresArrays() {
        WeeklyReportResponse report = analytics.generateWeeklyReportData(FAMILY, WEEK_MONDAY);

        assertEquals("success", report.getStatus());
        // Noise logged 3x, Routine 1x → Noise is the top trigger.
        assertTrue(report.getTop3Triggers().contains("Noise"));
        assertEquals(7, report.getChartData().size(), "a week of daily emotion points");
    }

    @Test
    void consecutiveTriggerAlert_detectsThreeDayStreakOnRealArrays() {
        List<String> alerts = analytics.checkConsecutiveTriggerAlert(FAMILY, 3);
        // "Noise" appears on Mon, Tue, Wed → a 3-consecutive-day streak fires an alert.
        assertTrue(alerts.contains("Noise"), "expected a Noise streak alert, got " + alerts);
    }

    private void insertEvent(Long childId, int intensity, String tagsLiteral, LocalDate day) {
        jdbc.update("""
            INSERT INTO behavior_event(parent_id, child_id, intensity, trigger_tags, occurred_at)
            VALUES (?, ?, ?, ?::text[], ?)
            """, parentId, childId, intensity, tagsLiteral,
            java.sql.Timestamp.valueOf(day.atTime(12, 0)));
    }

    private static String uniq(String role) {
        return role + "-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@pgit.test";
    }
}
