package com.jamil.ahadith.features.catalog.service;

import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.catalog.dto.projection.PublicBookRow;
import com.jamil.ahadith.features.catalog.dto.projection.PublicMuhaddithRow;
import com.jamil.ahadith.features.catalog.dto.projection.PublicRawiRow;
import com.jamil.ahadith.features.catalog.dto.response.PublicTextDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicBookResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicMuhaddithListItemDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicRawiListItemDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.entity.Topic;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RawiNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.exception.TopicNotFoundException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.hadith.entity.Explaining;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCatalogServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private RawiRepository rawiRepository;
    @Mock
    private RulingRepository rulingRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private MuhaddithRepository muhaddithRepository;
    @Mock
    private ExplainingRepository explainingRepository;
    @Mock
    private FakeHadithRepository fakeHadithRepository;
    @Mock
    private HadithSearchService hadithSearchService;

    @Test
    void shouldGetAllPublicListsWithDtoConversionAndSerialNumbers() {
        UUID bookId = UUID.randomUUID();
        UUID muhaddithId = UUID.randomUUID();
        UUID secondBookId = UUID.randomUUID();
        UUID rulingId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID explainingId = UUID.randomUUID();
        UUID fakeHadithId = UUID.randomUUID();

        when(bookRepository.findPublicBookRows()).thenReturn(List.of(
                new PublicBookRow(bookId, "Book A", muhaddithId, "Muhaddith A"),
                new PublicBookRow(secondBookId, "Book B", null, null)));
        when(rawiRepository.findPublicRawiRows()).thenReturn(List.of(
                new PublicRawiRow("Rawi A", "about rawi"),
                new PublicRawiRow("Rawi B", null)));
        when(muhaddithRepository.findPublicMuhaddithRows()).thenReturn(List.of(
                new PublicMuhaddithRow("Muhaddith A", "about muhaddith")));
        when(rulingRepository.findAll()).thenReturn(List.of(ruling(rulingId, "Sahih")));
        when(topicRepository.findAll()).thenReturn(List.of(topic(topicId, "Topic A")));
        when(explainingRepository.findAll()).thenReturn(List.of(explaining(explainingId, "Explanation text")));
        when(fakeHadithRepository.findAll()).thenReturn(List.of(fakeHadith(fakeHadithId, "Fake text")));

        PublicCatalogService service = service();

        assertThat(service.getBooks()).containsExactly(
                new PublicBookResponseDto(
                        bookId,
                        "Book A",
                        new MuhaddithReferenceResponseDto(muhaddithId, "Muhaddith A")),
                new PublicBookResponseDto(secondBookId, "Book B", null));
        assertThat(service.getRawis()).containsExactly(
                new PublicRawiListItemDto(1, "Rawi A", "about rawi"),
                new PublicRawiListItemDto(2, "Rawi B", null));
        assertThat(service.getMuhaddiths()).containsExactly(
                new PublicMuhaddithListItemDto(1, "Muhaddith A", "about muhaddith"));
        assertThat(service.getRulings()).containsExactly(new SimpleReferenceDto(rulingId, "Sahih"));
        assertThat(service.getTopics()).containsExactly(new SimpleReferenceDto(topicId, "Topic A"));
        assertThat(service.getExplainings()).containsExactly(new PublicTextDto(explainingId, "Explanation text"));
        assertThat(service.getFakeAhadith()).containsExactly(new PublicTextDto(fakeHadithId, "Fake text"));
    }

    @Test
    void shouldGetExistingItemsByIdAsDtos() {
        UUID bookId = UUID.randomUUID();
        UUID rawiId = UUID.randomUUID();
        UUID rulingId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID muhaddithId = UUID.randomUUID();
        UUID explainingId = UUID.randomUUID();
        UUID fakeHadithId = UUID.randomUUID();

        UUID bookMuhaddithId = UUID.randomUUID();
        when(bookRepository.findPublicBookRowById(bookId))
                .thenReturn(Optional.of(new PublicBookRow(bookId, "Book A", bookMuhaddithId, "Muhaddith A")));
        when(rawiRepository.findById(rawiId)).thenReturn(Optional.of(rawi(rawiId, "Rawi A")));
        when(rulingRepository.findById(rulingId)).thenReturn(Optional.of(ruling(rulingId, "Hasan")));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic(topicId, "Topic A")));
        when(muhaddithRepository.findById(muhaddithId)).thenReturn(Optional.of(muhaddith(muhaddithId, "Muhaddith A")));
        when(explainingRepository.findById(explainingId))
                .thenReturn(Optional.of(explaining(explainingId, "Explanation text")));
        when(fakeHadithRepository.findById(fakeHadithId))
                .thenReturn(Optional.of(fakeHadith(fakeHadithId, "Fake text")));

        PublicCatalogService service = service();

        assertThat(service.getBook(bookId)).isEqualTo(new PublicBookResponseDto(
                bookId,
                "Book A",
                new MuhaddithReferenceResponseDto(bookMuhaddithId, "Muhaddith A")));
        assertThat(service.getRawi(rawiId)).isEqualTo(new SimpleReferenceDto(rawiId, "Rawi A"));
        assertThat(service.getRuling(rulingId)).isEqualTo(new SimpleReferenceDto(rulingId, "Hasan"));
        assertThat(service.getTopic(topicId)).isEqualTo(new SimpleReferenceDto(topicId, "Topic A"));
        assertThat(service.getMuhaddith(muhaddithId)).isEqualTo(new SimpleReferenceDto(muhaddithId, "Muhaddith A"));
        assertThat(service.getExplaining(explainingId)).isEqualTo(new PublicTextDto(explainingId, "Explanation text"));
        assertThat(service.getFakeHadith(fakeHadithId)).isEqualTo(new PublicTextDto(fakeHadithId, "Fake text"));
    }

    @Test
    void shouldThrowCorrectExceptionWhenItemDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        UUID rawiId = UUID.randomUUID();
        UUID rulingId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID muhaddithId = UUID.randomUUID();
        UUID explainingId = UUID.randomUUID();
        UUID fakeHadithId = UUID.randomUUID();

        when(bookRepository.findPublicBookRowById(bookId)).thenReturn(Optional.empty());
        when(rawiRepository.findById(rawiId)).thenReturn(Optional.empty());
        when(rulingRepository.findById(rulingId)).thenReturn(Optional.empty());
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());
        when(muhaddithRepository.findById(muhaddithId)).thenReturn(Optional.empty());
        when(explainingRepository.findById(explainingId)).thenReturn(Optional.empty());
        when(fakeHadithRepository.findById(fakeHadithId)).thenReturn(Optional.empty());

        PublicCatalogService service = service();

        assertThatThrownBy(() -> service.getBook(bookId)).isInstanceOf(BookNotFoundException.class);
        assertThatThrownBy(() -> service.getRawi(rawiId)).isInstanceOf(RawiNotFoundException.class);
        assertThatThrownBy(() -> service.getRuling(rulingId)).isInstanceOf(RulingNotFoundException.class);
        assertThatThrownBy(() -> service.getTopic(topicId)).isInstanceOf(TopicNotFoundException.class);
        assertThatThrownBy(() -> service.getMuhaddith(muhaddithId)).isInstanceOf(MuhaddithNotFoundException.class);
        assertThatThrownBy(() -> service.getExplaining(explainingId)).isInstanceOf(ExplainingNotFoundException.class);
        assertThatThrownBy(() -> service.getFakeHadith(fakeHadithId)).isInstanceOf(FakeHadithNotFoundException.class);
    }

    @Test
    void shouldDelegateBookAhadithSearchWithSameValues() {
        UUID bookId = UUID.randomUUID();
        int page = 2;
        int size = 25;
        SearchResponse<HadithSearchItemDto> expected = new SearchResponse<>(
                List.of(new HadithSearchItemDto(
                        UUID.randomUUID(),
                        "Hadith text",
                        "Hadith normal text",
                        10,
                        "marfu",
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        false,
                        false)),
                new PaginationMeta(page, size, 1, 1, false, true));
        when(hadithSearchService.getBookAhadith(bookId, page, size)).thenReturn(expected);

        SearchResponse<HadithSearchItemDto> actual = service().getBookAhadith(bookId, page, size);

        assertThat(actual).isSameAs(expected);
        verify(hadithSearchService).getBookAhadith(bookId, page, size);
    }

    private PublicCatalogService service() {
        return new PublicCatalogService(
                bookRepository,
                rawiRepository,
                rulingRepository,
                topicRepository,
                muhaddithRepository,
                explainingRepository,
                fakeHadithRepository,
                hadithSearchService);
    }

    private Rawi rawi(UUID id, String name) {
        Rawi rawi = new Rawi();
        rawi.setId(id);
        rawi.setName(name);
        return rawi;
    }

    private Ruling ruling(UUID id, String name) {
        Ruling ruling = new Ruling();
        ruling.setId(id);
        ruling.setName(name);
        return ruling;
    }

    private Topic topic(UUID id, String name) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        return topic;
    }

    private Muhaddith muhaddith(UUID id, String name) {
        Muhaddith muhaddith = new Muhaddith();
        muhaddith.setId(id);
        muhaddith.setName(name);
        return muhaddith;
    }

    private Explaining explaining(UUID id, String text) {
        Explaining explaining = new Explaining();
        explaining.setId(id);
        explaining.setText(text);
        return explaining;
    }

    private FakeHadith fakeHadith(UUID id, String text) {
        FakeHadith fakeHadith = new FakeHadith();
        fakeHadith.setId(id);
        fakeHadith.setText(text);
        return fakeHadith;
    }
}
