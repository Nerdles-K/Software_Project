package com.visitalk.repository;

import com.visitalk.model.Sentence;
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
public class SentenceRepository {

    private final JdbcTemplate jdbc;

    public SentenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Sentence> MAPPER = (rs, i) -> {
        Sentence s = new Sentence();
        s.setId(rs.getLong("id"));
        s.setFamilyId(rs.getString("family_id"));
        s.setSenderRole(rs.getString("sender_role"));
        s.setSenderName(rs.getString("sender_name"));
        Array arr = rs.getArray("card_ids");
        Object[] raw = arr == null ? new Object[0] : (Object[]) arr.getArray();
        Long[] ids = new Long[raw.length];
        for (int k = 0; k < raw.length; k++) ids[k] = ((Number) raw[k]).longValue();
        s.setCardIds(ids);
        Timestamp ts = rs.getTimestamp("created_at");
        s.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return s;
    };

    public Sentence insert(Sentence s) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO sentence (family_id, sender_role, sender_name, card_ids, created_at) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"});
            ps.setString(1, s.getFamilyId());
            ps.setString(2, s.getSenderRole());
            ps.setString(3, s.getSenderName());
            Array a = con.createArrayOf("bigint", s.getCardIds() == null ? new Long[0] : s.getCardIds());
            ps.setArray(4, a);
            ps.setTimestamp(5, Timestamp.valueOf(now));
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) s.setId(key.longValue());
        s.setCreatedAt(now);
        return s;
    }

    /** Conversation feed for a family, oldest → newest, limited. */
    public List<Sentence> findByFamilyId(String familyId, int limit) {
        return jdbc.query(
            "SELECT * FROM sentence WHERE family_id = ? ORDER BY created_at ASC, id ASC LIMIT ?",
            MAPPER, familyId, limit);
    }

    /** Strictly newer than `sinceId`, used for polling. */
    public List<Sentence> findNewer(String familyId, long sinceId) {
        return jdbc.query(
            "SELECT * FROM sentence WHERE family_id = ? AND id > ? ORDER BY id ASC",
            MAPPER, familyId, sinceId);
    }
}
