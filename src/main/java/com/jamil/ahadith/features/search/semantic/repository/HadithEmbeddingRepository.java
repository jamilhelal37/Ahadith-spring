package com.jamil.ahadith.features.search.semantic.repository;

import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public class HadithEmbeddingRepository {
    private final JdbcTemplate jdbc;

    public HadithEmbeddingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsert(UUID hadithId, List<Double> embedding, String model, String modelVersion, String contentHash) {
        String vector = vectorLiteral(embedding);
        jdbc.update("""
                INSERT INTO public.hadith_embeddings
                    (hadith_id, embedding, model_name, model_version, content_hash, embedded_at)
                VALUES (?, CAST(? AS vector), ?, ?, ?, current_timestamp)
                ON CONFLICT (hadith_id) DO UPDATE SET
                    embedding = EXCLUDED.embedding,
                    model_name = EXCLUDED.model_name,
                    model_version = EXCLUDED.model_version,
                    content_hash = EXCLUDED.content_hash,
                    embedded_at = current_timestamp
                """, hadithId, vector, model, modelVersion, contentHash);
    }

    public boolean isCurrent(UUID hadithId, String contentHash, String model, String modelVersion) {
        Boolean current = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM public.hadith_embeddings
                    WHERE hadith_id = ? AND content_hash = ? AND model_name = ?
                      AND model_version IS NOT DISTINCT FROM ?
                )
                """, Boolean.class, hadithId, contentHash, model, modelVersion);
        return Boolean.TRUE.equals(current);
    }

    public String findHadithText(UUID hadithId) {
        List<String> texts = jdbc.query("SELECT text FROM public.ahadith WHERE id = ?",
                (rs, rowNum) -> rs.getString(1), hadithId);
        return texts.isEmpty() ? null : texts.getFirst();
    }

    public List<HadithEmbeddingState> findNeedingEmbedding(
            String model, String modelVersion, boolean force, UUID afterId, int limit
    ) {
        return jdbc.query("""
                SELECT h.id, h.text
                FROM public.ahadith h
                LEFT JOIN public.hadith_embeddings he ON he.hadith_id = h.id
                WHERE (? OR he.hadith_id IS NULL OR he.content_hash <> encode(digest(h.text, 'sha256'), 'hex')
                       OR he.model_name <> ? OR he.model_version IS DISTINCT FROM ?)
                  AND (?::uuid IS NULL OR h.id > ?::uuid)
                ORDER BY h.id
                LIMIT ?
                """, (rs, rowNum) -> new HadithEmbeddingState(rs.getObject("id", UUID.class), rs.getString("text")),
                force, model, modelVersion, afterId, afterId, limit);
    }

    public EmbeddingStatus status(String model, String modelVersion) {
        return jdbc.queryForObject("""
                SELECT count(*) AS total,
                       count(*) FILTER (WHERE he.hadith_id IS NOT NULL
                           AND he.content_hash = encode(digest(h.text, 'sha256'), 'hex')
                           AND he.model_name = ? AND he.model_version IS NOT DISTINCT FROM ?) AS current_count,
                       count(*) FILTER (WHERE he.hadith_id IS NULL) AS missing,
                       count(*) FILTER (WHERE he.hadith_id IS NOT NULL AND (
                           he.content_hash <> encode(digest(h.text, 'sha256'), 'hex')
                           OR he.model_name <> ? OR he.model_version IS DISTINCT FROM ?)) AS stale
                FROM public.ahadith h
                LEFT JOIN public.hadith_embeddings he ON he.hadith_id = h.id
                """, (rs, rowNum) -> new EmbeddingStatus(
                        rs.getLong("total"), rs.getLong("current_count"),
                        rs.getLong("missing"), rs.getLong("stale")),
                model, modelVersion, model, modelVersion);
    }

    public List<SemanticCandidate> search(
            List<Double> queryEmbedding,
            HadithSearchRequest request,
            double minSimilarity,
            int limit
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT h.id, 1 - (he.embedding <=> CAST(? AS vector)) AS similarity
                FROM public.hadith_embeddings he
                JOIN public.ahadith h ON h.id = he.hadith_id
                LEFT JOIN public.books b ON b.id = h.book
                LEFT JOIN public.muhaddiths m ON m.id = b.muhaddith
                LEFT JOIN public.rawis r ON r.id = h.rawi
                LEFT JOIN public.ruling ru ON ru.id = h.ruling
                WHERE 1 - (he.embedding <=> CAST(? AS vector)) >= ?
                """);
        List<Object> parameters = new ArrayList<>();
        String vector = vectorLiteral(queryEmbedding);
        parameters.add(vector);
        parameters.add(vector);
        parameters.add(minSimilarity);
        addUuidFilter(sql, parameters, "m.id", request.getMuhaddithIds());
        addUuidFilter(sql, parameters, "r.id", request.getRawiIds());
        addStringFilter(sql, parameters, "CAST(h.type AS text)", request.getTypes() == null
                ? null : request.getTypes().stream().filter(value -> value != null).map(Enum::name).distinct().toList());
        addUuidFilter(sql, parameters, "ru.id", request.getRulingIds());
        addUuidFilter(sql, parameters, "b.id", request.getBookIds());
        if (request.getTopicIds() != null && request.getTopicIds().stream().anyMatch(value -> value != null)) {
            List<UUID> ids = request.getTopicIds().stream().filter(value -> value != null).distinct().toList();
            sql.append(" AND EXISTS (SELECT 1 FROM public.topic_classes tc WHERE tc.hadith = h.id AND tc.topic IN (");
            appendPlaceholders(sql, ids.size());
            sql.append("))");
            parameters.addAll(ids);
        }
        sql.append(" ORDER BY he.embedding <=> CAST(? AS vector), h.id LIMIT ?");
        parameters.add(vector);
        parameters.add(limit);
        return jdbc.query(sql.toString(), (rs, rowNum) -> new SemanticCandidate(
                rs.getObject("id", UUID.class), rs.getDouble("similarity")), parameters.toArray());
    }

    private void addUuidFilter(StringBuilder sql, List<Object> parameters, String column, List<UUID> values) {
        if (values == null) return;
        List<UUID> filtered = values.stream().filter(value -> value != null).distinct().toList();
        if (filtered.isEmpty()) return;
        sql.append(" AND ").append(column).append(" IN (");
        appendPlaceholders(sql, filtered.size());
        sql.append(')');
        parameters.addAll(filtered);
    }

    private void addStringFilter(StringBuilder sql, List<Object> parameters, String column, List<String> values) {
        if (values == null || values.isEmpty()) return;
        sql.append(" AND ").append(column).append(" IN (");
        appendPlaceholders(sql, values.size());
        sql.append(')');
        parameters.addAll(values);
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        sql.append(String.join(",", java.util.Collections.nCopies(count, "?")));
    }

    private String vectorLiteral(List<Double> embedding) {
        if (embedding == null || embedding.size() != 1024) {
            throw new IllegalArgumentException("Embedding must contain exactly 1024 values");
        }
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            double component = embedding.get(i);
            if (!Double.isFinite(component)) {
                throw new IllegalArgumentException("Embedding contains a non-finite value");
            }
            if (i > 0) value.append(',');
            value.append(String.format(Locale.ROOT, "%.9g", component));
        }
        return value.append(']').toString();
    }
}
