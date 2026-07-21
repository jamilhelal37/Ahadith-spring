package com.jamil.ahadith.features.hadith.repository;

import com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto;
import com.jamil.ahadith.features.search.dto.projection.HadithSearchRow;
import com.jamil.ahadith.features.search.dto.projection.HadithTopicRow;
import com.jamil.ahadith.features.hadith.entity.Hadith;
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
                cast(:query as text) is null
                or (
                    cast(:mode as text) = 'EXACT'
                    and (
                        h.search_text like concat('%', public.arab_norm(cast(:query as text)), '%')
                        or (
                            cast(:includeExplanation as boolean) = true
                            and e.search_text like concat('%', public.arab_norm(cast(:query as text)), '%')
                        )
                    )
                )
                or (
                    cast(:mode as text) = 'FLEXIBLE'
                    and (
                        h.search_vector @@ plainto_tsquery('arabic', public.arab_norm(cast(:query as text)))
                        or (
                            cast(:includeExplanation as boolean) = true
                            and to_tsvector('arabic', coalesce(e.search_text, '')) @@ plainto_tsquery('arabic', public.arab_norm(cast(:query as text)))
                        )
                    )
                )
            )
            and (cast(:muhaddithIds as uuid[]) is null or m.id = any(cast(:muhaddithIds as uuid[])))
            and (cast(:rawiIds as uuid[]) is null or r.id = any(cast(:rawiIds as uuid[])))
            and (cast(:types as text[]) is null or cast(h.type as text) = any(cast(:types as text[])))
            and (cast(:rulingIds as uuid[]) is null or ru.id = any(cast(:rulingIds as uuid[])))
            and (cast(:bookIds as uuid[]) is null or b.id = any(cast(:bookIds as uuid[])))
            and (cast(:topicIds as uuid[]) is null or tc.topic = any(cast(:topicIds as uuid[])))
            """;

    String ADMIN_SEARCH_WHERE = """
            where (
                cast(:query as text) is null
                or h.search_text like concat('%', public.arab_norm(cast(:query as text)), '%')
                or cast(h.hadith_number as text) = cast(:query as text)
            )
            """;

    @Query(
            value = """
                    select h.id
                    """ + SEARCH_FROM + PUBLIC_SEARCH_WHERE + """
                    group by h.id, h.hadith_number, h.search_vector
                    order by
                        case
                            when cast(:sort as text) = 'RELEVANCE'
                                and cast(:query as text) is not null
                                and cast(:mode as text) = 'FLEXIBLE'
                            then ts_rank(h.search_vector, plainto_tsquery('arabic', public.arab_norm(cast(:query as text))))
                        end desc nulls last,
                        case when cast(:sort as text) = 'HADITH_NUMBER_DESC' then h.hadith_number end desc nulls last,
                        case when cast(:sort as text) <> 'HADITH_NUMBER_DESC' then h.hadith_number end asc nulls last,
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

    @Query("""
            select h.id
            from Hadith h
            where h.book.id = :bookId
            order by h.hadithNumber asc, h.id asc
            """)
    Page<UUID> findBookAhadithIds(@Param("bookId") UUID bookId, Pageable pageable);

    @Query(
            value = """
                    select
                        h.id as "id",
                        h.text as "text",
                        h.normal_text as "normalText",
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
                        e.text as "explanationText",
                        e.normal_text as "explanationNormalText",
                        h.sub_valid as "subValidId"
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

    @Query("""
            select h.id as id,
                   h.text as text,
                   h.normalText as normalText,
                   h.hadithNumber as hadithNumber,
                   cast(h.type as string) as type,
                   h.sanad as sanad,
                   b.id as bookId,
                   b.name as bookName,
                   r.id as rawiId,
                   r.name as rawiName,
                   ru.id as rulingId,
                   ru.name as rulingName,
                   m.id as muhaddithId,
                   m.name as muhaddithName,
                   e.id as explanationId,
                   e.text as explanationText,
                   e.normalText as explanationNormalText,
                   sv.id as subValidId
            from Hadith h
            left join h.book b
            left join b.muhaddith m
            left join h.rawi r
            left join h.ruling ru
            left join h.explaining e
            left join h.subValid sv
            where h.id in :ids
            """)
    List<HadithSearchRow> findSearchRowsByIdsJpa(@Param("ids") List<UUID> ids);

    @Query(
            value = """
                    select
                        h.id as "id",
                        h.text as "text",
                        h.normal_text as "normalText",
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
                        e.text as "explanationText",
                        e.normal_text as "explanationNormalText",
                        h.sub_valid as "subValidId"
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

    @Query("""
            select h.id as id,
                   h.text as text,
                   h.normalText as normalText,
                   h.hadithNumber as hadithNumber,
                   cast(h.type as string) as type,
                   h.sanad as sanad,
                   b.id as bookId,
                   b.name as bookName,
                   r.id as rawiId,
                   r.name as rawiName,
                   ru.id as rulingId,
                   ru.name as rulingName,
                   m.id as muhaddithId,
                   m.name as muhaddithName,
                   e.id as explanationId,
                   e.text as explanationText,
                   e.normalText as explanationNormalText,
                   sv.id as subValidId
            from Hadith h
            left join h.book b
            left join b.muhaddith m
            left join h.rawi r
            left join h.ruling ru
            left join h.explaining e
            left join h.subValid sv
            where h.id = :id
            """)
    HadithSearchRow findPublicDetailsRowById(@Param("id") UUID id);

    @Query(
            value = """
                    select
                        tc.hadith as "hadithId",
                        t.id as "id",
                        t.name as "name"
                    from public.topic_classes tc
                    join public.topics t on t.id = tc.topic
                    where tc.hadith = any(cast(:ids as uuid[]))
                    order by t.name asc, t.id asc
                    """,
            nativeQuery = true
    )
    List<HadithTopicRow> findTopicsByHadithIds(@Param("ids") UUID[] ids);

    @Query("""
            select tc.hadith.id as hadithId,
                   t.id as id,
                   t.name as name
            from TopicClass tc
            join tc.topic t
            where tc.hadith.id in :ids
            order by t.name asc, t.id asc
            """)
    List<HadithTopicRow> findTopicsByHadithIdsJpa(@Param("ids") List<UUID> ids);

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto(t.id, t.name)
            from TopicClass tc
            join tc.topic t
            where tc.hadith.id = :hadithId
            order by t.name asc, t.id asc
            """)
    List<TopicReferenceResponseDto> findPublicTopicReferencesByHadithId(@Param("hadithId") UUID hadithId);
}
