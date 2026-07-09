package com.jamil.ahadith.repositories;

import com.jamil.ahadith.dtos.projections.HadithSearchRow;
import com.jamil.ahadith.dtos.projections.HadithTopicRow;
import com.jamil.ahadith.entities.Hadith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HadithRepository extends JpaRepository<Hadith, UUID> {
    @EntityGraph(attributePaths = {"book", "rawi", "ruling", "explaining"})
    @Query("select h from Hadith h")
    List<Hadith> findAllWithRelations();

    String SEARCH_FROM = """
            from public.ahadith h
            left join public.books b on b.id = h.book
            left join public.muhaddiths m on m.id = b.muhaddith
            left join public.rawis r on r.id = h.rawi
            left join public.ruling ru on ru.id = h.ruling
            left join public.explaining e on e.id = h.explaining
            left join public.topic_classes tc on tc.hadith = h.id
            left join public.topics t on t.id = tc.topic
            """;

    String PUBLIC_SEARCH_WHERE = """
            where (
                :query is null
                or (
                    :mode = 'EXACT'
                    and (
                        h.search_text like concat('%', public.arab_norm(:query), '%')
                        or (
                            :includeExplanation = true
                            and e.search_text like concat('%', public.arab_norm(:query), '%')
                        )
                    )
                )
                or (
                    :mode = 'FLEXIBLE'
                    and (
                        h.search_vector @@ plainto_tsquery('arabic', public.arab_norm(:query))
                        or (
                            :includeExplanation = true
                            and to_tsvector('arabic', coalesce(e.search_text, '')) @@ plainto_tsquery('arabic', public.arab_norm(:query))
                        )
                    )
                )
            )
            and (:muhaddithIds is null or m.id = any(cast(:muhaddithIds as uuid[])))
            and (:rawiIds is null or r.id = any(cast(:rawiIds as uuid[])))
            and (:types is null or cast(h.type as text) = any(cast(:types as text[])))
            and (:rulingIds is null or ru.id = any(cast(:rulingIds as uuid[])))
            and (:bookIds is null or b.id = any(cast(:bookIds as uuid[])))
            and (:topicIds is null or tc.topic = any(cast(:topicIds as uuid[])))
            """;

    String ADMIN_SEARCH_WHERE = """
            where (
                :query is null
                or h.search_text like concat('%', public.arab_norm(:query), '%')
                or cast(h.hadith_number as text) = :query
            )
            """;

    @Query(
            value = """
                    select h.id
                    """ + SEARCH_FROM + PUBLIC_SEARCH_WHERE + """
                    group by h.id, h.hadith_number, h.search_vector
                    order by
                        case
                            when :sort = 'RELEVANCE' and :query is not null and :mode = 'FLEXIBLE'
                            then ts_rank(h.search_vector, plainto_tsquery('arabic', public.arab_norm(:query)))
                        end desc nulls last,
                        case when :sort = 'HADITH_NUMBER_DESC' then h.hadith_number end desc nulls last,
                        case when :sort <> 'HADITH_NUMBER_DESC' then h.hadith_number end asc nulls last,
                        h.id asc
                    """,
            countQuery = "select count(distinct h.id) " + SEARCH_FROM + PUBLIC_SEARCH_WHERE,
            nativeQuery = true
    )
    Page<UUID> searchPublicIds(
            @Param("query") String query,
            @Param("mode") String mode,
            @Param("sort") String sort,
            @Param("includeExplanation") boolean includeExplanation,
            @Param("muhaddithIds") UUID[] muhaddithIds,
            @Param("rawiIds") UUID[] rawiIds,
            @Param("types") String[] types,
            @Param("rulingIds") UUID[] rulingIds,
            @Param("bookIds") UUID[] bookIds,
            @Param("topicIds") UUID[] topicIds,
            Pageable pageable
    );

    @Query(
            value = """
                    select h.id
                    """ + SEARCH_FROM + ADMIN_SEARCH_WHERE + """
                    group by h.id, h.hadith_number
                    order by h.hadith_number asc nulls last, h.id asc
                    """,
            countQuery = "select count(distinct h.id) " + SEARCH_FROM + ADMIN_SEARCH_WHERE,
            nativeQuery = true
    )
    Page<UUID> searchAdminIds(
            @Param("query") String query,
            Pageable pageable
    );

    @Query(
            value = """
                    select
                        h.id as "id",
                        h.text as "text",
                        h.hadith_number as "hadithNumber",
                        cast(h.type as text) as "type",
                        h.sanad as "sanad",
                        b.id as "bookId",
                        b.name as "bookName",
                        r.id as "rawiId",
                        r.name as "rawiName",
                        ru.id as "rulingId",
                        ru.name as "rulingName",
                        m.id as "muhaddithId",
                        m.name as "muhaddithName",
                        e.id as "explanationId",
                        e.text as "explanationText"
                    from public.ahadith h
                    left join public.books b on b.id = h.book
                    left join public.muhaddiths m on m.id = b.muhaddith
                    left join public.rawis r on r.id = h.rawi
                    left join public.ruling ru on ru.id = h.ruling
                    left join public.explaining e on e.id = h.explaining
                    where h.id = any(cast(:ids as uuid[]))
                    """,
            nativeQuery = true
    )
    List<HadithSearchRow> findSearchRowsByIds(@Param("ids") UUID[] ids);

    @Query(
            value = """
                    select
                        h.id as "id",
                        h.text as "text",
                        h.hadith_number as "hadithNumber",
                        cast(h.type as text) as "type",
                        h.sanad as "sanad",
                        b.id as "bookId",
                        b.name as "bookName",
                        r.id as "rawiId",
                        r.name as "rawiName",
                        ru.id as "rulingId",
                        ru.name as "rulingName",
                        m.id as "muhaddithId",
                        m.name as "muhaddithName",
                        e.id as "explanationId",
                        e.text as "explanationText"
                    from public.ahadith h
                    left join public.books b on b.id = h.book
                    left join public.muhaddiths m on m.id = b.muhaddith
                    left join public.rawis r on r.id = h.rawi
                    left join public.ruling ru on ru.id = h.ruling
                    left join public.explaining e on e.id = h.explaining
                    where h.id = :id
                    """,
            nativeQuery = true
    )
    HadithSearchRow findSearchRowById(@Param("id") UUID id);

    @Query(
            value = """
                    select
                        tc.hadith as "hadithId",
                        t.id as "id",
                        t.name as "name"
                    from public.topic_classes tc
                    join public.topics t on t.id = tc.topic
                    where tc.hadith = any(cast(:ids as uuid[]))
                    order by t.name asc
                    """,
            nativeQuery = true
    )
    List<HadithTopicRow> findTopicsByHadithIds(@Param("ids") UUID[] ids);
}
