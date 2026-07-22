package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.catalog.entity.Rawi;

import com.jamil.ahadith.features.user.entity.Gender;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.catalog.entity.TopicClass;

import com.jamil.ahadith.features.catalog.entity.Book;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.features.catalog.entity.Topic;

import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.request.HadithSearchSort;
import com.jamil.ahadith.features.search.dto.request.SearchMode;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HadithSearchPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private HadithSearchService searchService;

    @Autowired
    private HadithRepository hadithRepository;

    @Test
    void exactSearchShouldUseArabNormNormalization() {
        UUID bookId = book(1, "Exact Book", null);
        UUID expectedId = hadith(10, "إِنَّ الأمانةَ عظيمة", 1, bookId, null, null, null);
        hadith(11, "الصلاة نور وبرهان", 2, bookId, null, null, null);

        String normalized = jdbc.queryForObject("select public.arab_norm(?)", String.class, "إن الأمانة");
        assertThat(normalized).isEqualTo("ان الامانه");

        SearchResponse<HadithSearchItemDto> response = search(request("ان الامانه", SearchMode.EXACT));

        assertIds(response, expectedId);
    }

    @Test
    void flexibleSearchShouldUsePostgresFullTextSearchVector() {
        UUID bookId = book(1, "Flexible Book", null);
        UUID expectedId = hadith(20, "الصلاة نور وبرهان", 1, bookId, null, null, null);
        UUID unrelatedId = hadith(21, "الزكاة طهرة للمال", 2, bookId, null, null, null);

        SearchResponse<HadithSearchItemDto> response = search(request("الصلاة", SearchMode.FLEXIBLE));

        assertIds(response, expectedId);
        assertThat(ids(response)).doesNotContain(unrelatedId);
    }

    @Test
    void searchVectorTriggerShouldUpdateSearchableColumnsWhenTextChanges() {
        UUID bookId = book(1, "Trigger Book", null);
        UUID hadithId = hadith(30, "قديم فقط", 1, bookId, null, null, null);

        jdbc.update("update public.ahadith set text = ? where id = ?", "النية أساس العمل", hadithId);

        assertThat(jdbc.queryForObject(
                "select search_vector is not null and search_text like '%' || public.arab_norm(?) || '%' from public.ahadith where id = ?",
                Boolean.class,
                "النية",
                hadithId))
                .isTrue();
        assertIds(search(request("النية", SearchMode.FLEXIBLE)), hadithId);
    }

    @Test
    void relevanceOrderingShouldFollowDatabaseRankAndStableTieBreakers() {
        UUID bookA = book(1, "Rank Book A", null);
        UUID bookB = book(2, "Rank Book B", null);
        UUID tiedLaterId = hadith(42, "الصدق", 5, bookA, null, null, null);
        UUID highRankId = hadith(40, "الصدق الصدق الصدق", 9, bookA, null, null, null);
        UUID tiedEarlierId = hadith(41, "الصدق", 5, bookB, null, null, null);

        HadithSearchRequest request = request("الصدق", SearchMode.FLEXIBLE);
        request.setSort(HadithSearchSort.RELEVANCE);

        SearchResponse<HadithSearchItemDto> response = search(request);

        assertIds(response, highRankId, tiedEarlierId, tiedLaterId);
    }

    @Test
    void explanationSearchShouldHonorIncludeExplanationFlag() {
        UUID bookId = book(1, "Explanation Book", null);
        UUID explanationId = explaining(50, "شرح فيه الرحمة");
        UUID hadithId = hadith(51, "نص لا يحتوي كلمة البحث", 1, bookId, null, null, explanationId);

        HadithSearchRequest excluded = request("الرحمة", SearchMode.FLEXIBLE);
        excluded.setIncludeExplanation(false);
        assertThat(search(excluded).getItems()).isEmpty();

        HadithSearchRequest included = request("الرحمة", SearchMode.FLEXIBLE);
        included.setIncludeExplanation(true);
        SearchResponse<HadithSearchItemDto> response = search(included);

        assertIds(response, hadithId);
        assertThat(response.getItems().getFirst().isHasExplanation()).isTrue();
    }

    @Test
    void postgresArrayFiltersShouldWorkForAllPublicSearchFilters() {
        UUID muhaddithId = muhaddith(1, "Filter Muhaddith");
        UUID otherMuhaddithId = muhaddith(2, "Other Muhaddith");
        UUID rawiId = rawi(3, "Filter Rawi");
        UUID otherRawiId = rawi(4, "Other Rawi");
        UUID rulingId = ruling(5, "Filter Ruling");
        UUID otherRulingId = ruling(6, "Other Ruling");
        UUID topicId = topic(7, "Filter Topic");
        UUID otherTopicId = topic(8, "Other Topic");
        UUID bookId = book(9, "Filter Book", muhaddithId);
        UUID otherBookId = book(10, "Other Book", otherMuhaddithId);
        UUID matchingId = hadith(60, "نص جامع للفلاتر", 1, bookId, rawiId, rulingId, null);
        UUID otherId = hadith(61, "نص آخر للفلاتر", 2, otherBookId, otherRawiId, otherRulingId, null);
        topicClass(62, matchingId, topicId);
        topicClass(63, otherId, otherTopicId);

        assertOnly(filterRequest(r -> r.setMuhaddithIds(List.of(muhaddithId))), matchingId);
        assertOnly(filterRequest(r -> r.setRawiIds(List.of(rawiId))), matchingId);
        assertOnly(filterRequest(r -> r.setTypes(List.of("marfu"))), matchingId, otherId);
        assertOnly(filterRequest(r -> r.setRulingIds(List.of(rulingId))), matchingId);
        assertOnly(filterRequest(r -> r.setBookIds(List.of(bookId))), matchingId);
        assertOnly(filterRequest(r -> r.setTopicIds(List.of(topicId))), matchingId);

        HadithSearchRequest combined = new HadithSearchRequest();
        combined.setMuhaddithIds(List.of(muhaddithId));
        combined.setRawiIds(List.of(rawiId));
        combined.setTypes(List.of("marfu"));
        combined.setRulingIds(List.of(rulingId));
        combined.setBookIds(List.of(bookId));
        combined.setTopicIds(List.of(topicId));
        assertOnly(combined, matchingId);
    }

    @Test
    void nullAndEmptyFiltersShouldNotExcludeAllRows() {
        UUID bookId = book(1, "Empty Filter Book", null);
        UUID firstId = hadith(70, "الأول", 1, bookId, null, null, null);
        UUID secondId = hadith(71, "الثاني", 2, bookId, null, null, null);

        HadithSearchRequest request = new HadithSearchRequest();
        request.setMuhaddithIds(List.of());
        request.setRawiIds(List.of());
        request.setTypes(List.of("", " "));
        request.setRulingIds(List.of());
        request.setBookIds(List.of());
        request.setTopicIds(List.of());

        SearchResponse<HadithSearchItemDto> response = search(request);

        assertIds(response, firstId, secondId);
    }

    @Test
    void paginationAndCountQueryShouldBeDistinctAndDeterministicAcrossTopicJoins() {
        UUID bookA = book(1, "Page Book A", null);
        UUID bookB = book(2, "Page Book B", null);
        UUID firstId = uuid(80);
        UUID secondId = uuid(81);
        UUID thirdId = uuid(82);
        hadith(firstId, "صفحة أولى", 1, bookA, null, null, null);
        hadith(thirdId, "صفحة ثالثة", 2, bookA, null, null, null);
        hadith(secondId, "صفحة ثانية", 1, bookB, null, null, null);
        UUID topicA = topic(83, "Topic A");
        UUID topicB = topic(84, "Topic B");
        topicClass(85, firstId, topicA);
        topicClass(86, firstId, topicB);

        HadithSearchRequest page0 = new HadithSearchRequest();
        page0.setSize(2);
        page0.setPage(0);
        SearchResponse<HadithSearchItemDto> firstPage = search(page0);

        assertIds(firstPage, firstId, secondId);
        assertThat(firstPage.getPagination().getTotalItems()).isEqualTo(3);
        assertThat(firstPage.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getPagination().isHasNext()).isTrue();
        assertThat(firstPage.getPagination().isHasPrevious()).isFalse();

        HadithSearchRequest page1 = new HadithSearchRequest();
        page1.setSize(2);
        page1.setPage(1);
        SearchResponse<HadithSearchItemDto> secondPage = search(page1);

        assertIds(secondPage, thirdId);
        assertThat(secondPage.getPagination().isHasNext()).isFalse();
        assertThat(secondPage.getPagination().isHasPrevious()).isTrue();
    }

    @Test
    void resultProjectionShouldContainRelationsTopicsExplanationAndPreservePageOrder() {
        UUID muhaddithId = muhaddith(1, "Projection Muhaddith");
        UUID rawiId = rawi(2, "Projection Rawi");
        UUID rulingId = ruling(3, "Projection Ruling");
        UUID bookId = book(4, "Projection Book", muhaddithId);
        UUID explanationId = explaining(5, "Projection Explanation");
        UUID topicA = topic(6, "A Topic");
        UUID topicB = topic(7, "B Topic");
        UUID laterId = hadith(90, "لاحق", 2, bookId, rawiId, rulingId, null);
        UUID firstId = hadith(91, "أول", 1, bookId, rawiId, rulingId, explanationId);
        topicClass(92, firstId, topicB);
        topicClass(93, firstId, topicA);

        SearchResponse<HadithSearchItemDto> response = search(new HadithSearchRequest());

        assertIds(response, firstId, laterId);
        HadithSearchItemDto item = response.getItems().getFirst();
        assertThat(item.getBook().getId()).isEqualTo(bookId);
        assertThat(item.getRawi().getId()).isEqualTo(rawiId);
        assertThat(item.getRuling().getId()).isEqualTo(rulingId);
        assertThat(item.getMuhaddith().getId()).isEqualTo(muhaddithId);
        assertThat(item.getTopics()).extracting("name").containsExactly("A Topic", "B Topic");
        assertThat(item.isHasExplanation()).isTrue();
    }

    @Test
    void adminSearchShouldUsePostgresTextAndHadithNumberSearch() {
        UUID bookId = book(1, "Admin Book", null);
        UUID textMatchId = hadith(100, "بحث إداري خاص", 77, bookId, null, null, null);
        UUID numberMatchId = hadith(101, "نص مختلف", 88, bookId, null, null, null);

        assertThat(hadithRepository.searchAdminIds("إداري", PageRequest.of(0, 10)).getContent())
                .containsExactly(textMatchId);
        assertThat(hadithRepository.searchAdminIds("88", PageRequest.of(0, 10)).getContent())
                .containsExactly(numberMatchId);
    }

    private HadithSearchRequest filterRequest(java.util.function.Consumer<HadithSearchRequest> customizer) {
        HadithSearchRequest request = new HadithSearchRequest();
        customizer.accept(request);
        return request;
    }

    private void assertOnly(HadithSearchRequest request, UUID... expectedIds) {
        assertIds(search(request), expectedIds);
    }

    private SearchResponse<HadithSearchItemDto> search(HadithSearchRequest request) {
        return searchService.publicSearch(request);
    }

    private HadithSearchRequest request(String query, SearchMode mode) {
        HadithSearchRequest request = new HadithSearchRequest();
        request.setQuery(query);
        request.setMode(mode);
        request.setSize(10);
        return request;
    }

    private void assertIds(SearchResponse<HadithSearchItemDto> response, UUID... ids) {
        assertThat(ids(response)).containsExactly(ids);
    }

    private List<UUID> ids(SearchResponse<HadithSearchItemDto> response) {
        return response.getItems().stream()
                .map(HadithSearchItemDto::getId)
                .toList();
    }

    private UUID muhaddith(int id, String name) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.muhaddiths (id, name, gender, about) values (?, ?, cast(? as public.gender), ?)",
                uuid, name, "male", "about " + name);
        return uuid;
    }

    private UUID rawi(int id, String name) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.rawis (id, name, gender, about) values (?, ?, cast(? as public.gender), ?)",
                uuid, name, "male", "about " + name);
        return uuid;
    }

    private UUID ruling(int id, String name) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.ruling (id, name) values (?, ?)", uuid, name);
        return uuid;
    }

    private UUID book(int id, String name, UUID muhaddithId) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.books (id, name, muhaddith) values (?, ?, ?)", uuid, name, muhaddithId);
        return uuid;
    }

    private UUID topic(int id, String name) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.topics (id, name) values (?, ?)", uuid, name);
        return uuid;
    }

    private UUID explaining(int id, String text) {
        UUID uuid = uuid(id);
        jdbc.update("insert into public.explaining (id, text) values (?, ?)", uuid, text);
        return uuid;
    }

    private UUID hadith(int id, String text, int number, UUID bookId, UUID rawiId, UUID rulingId, UUID explainingId) {
        UUID uuid = uuid(id);
        hadith(uuid, text, number, bookId, rawiId, rulingId, explainingId);
        return uuid;
    }

    private void hadith(UUID uuid, String text, int number, UUID bookId, UUID rawiId, UUID rulingId, UUID explainingId) {
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type, book, rawi, ruling, explaining)
                values (?, ?, ?, cast(? as public.hadith_type), ?, ?, ?, ?)
                """, uuid, text, number, "marfu", bookId, rawiId, rulingId, explainingId);
    }

    private void topicClass(int id, UUID hadithId, UUID topicId) {
        jdbc.update("insert into public.topic_classes (id, hadith, topic) values (?, ?, ?)",
                uuid(id), hadithId, topicId);
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
