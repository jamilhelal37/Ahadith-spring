package com.jamil.ahadith.features.search.semantic.repository;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HadithEmbeddingRepositoryPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private HadithEmbeddingRepository repository;

    @Test
    void upsertSemanticFiltersMinimumSimilarityAndCascadeWork() {
        UUID muhaddith = UUID.randomUUID();
        UUID book = UUID.randomUUID();
        UUID otherBook = UUID.randomUUID();
        UUID rawi = UUID.randomUUID();
        UUID ruling = UUID.randomUUID();
        UUID topic = UUID.randomUUID();
        UUID matching = UUID.randomUUID();
        UUID unrelated = UUID.randomUUID();
        jdbc.update("insert into public.muhaddiths(id,name,gender,about) values (?,?,'male','about')", muhaddith, "m");
        jdbc.update("insert into public.books(id,name,muhaddith) values (?,?,?)", book, "b", muhaddith);
        jdbc.update("insert into public.books(id,name,muhaddith) values (?,?,?)", otherBook, "b2", muhaddith);
        jdbc.update("insert into public.rawis(id,name,gender,about) values (?,?,'male','about')", rawi, "r");
        jdbc.update("insert into public.ruling(id,name) values (?,?)", ruling, "ru");
        jdbc.update("insert into public.topics(id,name) values (?,?)", topic, "t");
        jdbc.update("insert into public.ahadith(id,type,text,hadith_number,book,rawi,ruling) values (?,'marfu',?,1,?,?,?)",
                matching, "mercy", book, rawi, ruling);
        jdbc.update("insert into public.ahadith(id,type,text,hadith_number,book,rawi,ruling) values (?,'marfu',?,2,?,?,?)",
                unrelated, "other", otherBook, rawi, ruling);
        jdbc.update("insert into public.topic_classes(id,hadith,topic) values (?,?,?)", UUID.randomUUID(), matching, topic);

        List<Double> query = unitVector(0);
        repository.upsert(matching, query, "BAAI/bge-m3", "1.3.5", "old");
        repository.upsert(matching, query, "BAAI/bge-m3", "1.3.5", "new");
        repository.upsert(unrelated, unitVector(1), "BAAI/bge-m3", "1.3.5", "other");

        HadithSearchRequest filters = new HadithSearchRequest();
        filters.setBookIds(List.of(book));
        filters.setRawiIds(List.of(rawi));
        filters.setRulingIds(List.of(ruling));
        filters.setMuhaddithIds(List.of(muhaddith));
        filters.setTopicIds(List.of(topic));
        List<SemanticCandidate> results = repository.search(query, filters, 0.45, 10);

        assertThat(results).extracting(SemanticCandidate::hadithId).containsExactly(matching);
        assertThat(jdbc.queryForObject("select content_hash from public.hadith_embeddings where hadith_id=?", String.class, matching))
                .isEqualTo("new");

        jdbc.update("delete from public.ahadith where id=?", matching);
        assertThat(jdbc.queryForObject("select count(*) from public.hadith_embeddings where hadith_id=?", Integer.class, matching))
                .isZero();
    }

    private List<Double> unitVector(int index) {
        List<Double> vector = new ArrayList<>(java.util.Collections.nCopies(1024, 0.0));
        vector.set(index, 1.0);
        return vector;
    }
}
