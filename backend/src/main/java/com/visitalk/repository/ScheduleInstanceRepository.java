package com.visitalk.repository;

import com.visitalk.model.ScheduleInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class ScheduleInstanceRepository {

    private final JdbcTemplate jdbc;

    public ScheduleInstanceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<ScheduleInstance> MAPPER = (rs, i) -> {
        ScheduleInstance s = new ScheduleInstance();
        s.setId(rs.getLong("id"));
        s.setTemplateId(rs.getLong("template_id"));
        Date d = rs.getDate("date");
        s.setDate(d == null ? null : d.toLocalDate());
        Array done = rs.getArray("completed_step_ids");
        Object[] raw = done == null ? new Object[0] : (Object[]) done.getArray();
        Long[] longs = new Long[raw.length];
        for (int k = 0; k < raw.length; k++) longs[k] = ((Number) raw[k]).longValue();
        s.setCompletedStepIndices(longs);
        return s;
    };

    public Optional<ScheduleInstance> findByTemplateAndDate(Long templateId, LocalDate date) {
        return jdbc.query(
            "SELECT * FROM schedule_instance WHERE template_id = ? AND date = ?",
            MAPPER, templateId, Date.valueOf(date)).stream().findFirst();
    }

    public Optional<ScheduleInstance> findById(Long id) {
        return jdbc.query(
            "SELECT * FROM schedule_instance WHERE id = ?",
            MAPPER, id).stream().findFirst();
    }

    public ScheduleInstance insert(ScheduleInstance s) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO schedule_instance (template_id, date, completed_step_ids) VALUES (?, ?, ?)",
                new String[]{"id"});
            ps.setLong(1, s.getTemplateId());
            ps.setDate(2, Date.valueOf(s.getDate()));
            Array a = con.createArrayOf("bigint",
                s.getCompletedStepIndices() == null ? new Long[0] : s.getCompletedStepIndices());
            ps.setArray(3, a);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) s.setId(key.longValue());
        return s;
    }

    public int updateCompleted(Long instanceId, Long[] completedStepIndices) {
        return jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE schedule_instance SET completed_step_ids = ? WHERE id = ?");
            Array a = con.createArrayOf("bigint", completedStepIndices);
            ps.setArray(1, a);
            ps.setLong(2, instanceId);
            return ps;
        });
    }
}
