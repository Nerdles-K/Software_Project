package com.visitalk.repository;

import com.visitalk.model.ScheduleTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleTemplateRepository {

    private final JdbcTemplate jdbc;

    public ScheduleTemplateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<ScheduleTemplate> MAPPER = (rs, i) -> {
        ScheduleTemplate t = new ScheduleTemplate();
        t.setId(rs.getLong("id"));
        t.setFamilyId(rs.getString("family_id"));
        t.setName(rs.getString("name"));
        Array steps = rs.getArray("steps");
        Object[] raw = steps == null ? new Object[0] : (Object[]) steps.getArray();
        Long[] longs = new Long[raw.length];
        for (int k = 0; k < raw.length; k++) longs[k] = ((Number) raw[k]).longValue();
        t.setSteps(longs);
        Timestamp ts = rs.getTimestamp("created_at");
        t.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return t;
    };

    public List<ScheduleTemplate> findByFamilyId(String familyId) {
        return jdbc.query(
            "SELECT * FROM schedule_template WHERE family_id = ? ORDER BY created_at DESC",
            MAPPER, familyId);
    }

    public Optional<ScheduleTemplate> findById(Long id) {
        return jdbc.query(
            "SELECT * FROM schedule_template WHERE id = ?",
            MAPPER, id).stream().findFirst();
    }

    public ScheduleTemplate insert(ScheduleTemplate t) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO schedule_template (family_id, name, steps) VALUES (?, ?, ?)",
                new String[]{"id"});
            ps.setString(1, t.getFamilyId());
            ps.setString(2, t.getName());
            Array a = con.createArrayOf("bigint", t.getSteps());
            ps.setArray(3, a);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) t.setId(key.longValue());
        return t;
    }

    public int update(Long id, String name, Long[] steps) {
        return jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE schedule_template SET name = ?, steps = ? WHERE id = ?");
            ps.setString(1, name);
            Array a = con.createArrayOf("bigint", steps);
            ps.setArray(2, a);
            ps.setLong(3, id);
            return ps;
        });
    }

    public int delete(Long id) {
        // Cascade clean up dependent instances first (no FK ON DELETE CASCADE set).
        jdbc.update("DELETE FROM schedule_instance WHERE template_id = ?", id);
        return jdbc.update("DELETE FROM schedule_template WHERE id = ?", id);
    }
}
