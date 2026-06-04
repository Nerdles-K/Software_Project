package com.visitalk.repository;

import com.visitalk.model.BehaviorEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BehaviorEventRepository {

    private final JdbcTemplate jdbc;

    public BehaviorEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<BehaviorEvent> MAPPER = (rs, i) -> {
        BehaviorEvent e = new BehaviorEvent();
        e.setId(rs.getLong("id"));
        e.setParentId(rs.getLong("parent_id"));
        e.setChildId(rs.getLong("child_id"));
        e.setIntensity(rs.getInt("intensity"));
        Array tags = rs.getArray("trigger_tags");
        e.setTriggerTags(tags == null ? new String[0] : (String[]) tags.getArray());
        e.setNoteEncrypted(rs.getString("note_encrypted"));
        Timestamp ts = rs.getTimestamp("occurred_at");
        e.setOccurredAt(ts == null ? null : ts.toLocalDateTime());
        return e;
    };

    public BehaviorEvent insert(BehaviorEvent e) {
        String sql = """
            INSERT INTO behavior_event (parent_id, child_id, intensity, trigger_tags, note_encrypted, occurred_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            // Naming "id" here makes Postgres return only the generated id column,
            // avoiding KeyHolder.getKey() throwing on a multi-column key map.
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, e.getParentId());
            ps.setLong(2, e.getChildId());
            ps.setInt(3, e.getIntensity());
            Array a = con.createArrayOf("text", e.getTriggerTags() == null ? new String[0] : e.getTriggerTags());
            ps.setArray(4, a);
            ps.setString(5, e.getNoteEncrypted());
            ps.setTimestamp(6, Timestamp.valueOf(e.getOccurredAt() == null ? LocalDateTime.now() : e.getOccurredAt()));
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) e.setId(key.longValue());
        return e;
    }

    public List<BehaviorEvent> findByFamilyId(String familyId, int limit) {
        String sql = """
            SELECT be.* FROM behavior_event be
            JOIN users u ON be.parent_id = u.id
            WHERE u.family_id = ?
            ORDER BY be.occurred_at DESC
            LIMIT ?
            """;
        return jdbc.query(sql, MAPPER, familyId, limit);
    }
}
