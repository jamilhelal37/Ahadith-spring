package com.jamil.ahadith.features.notification.service;

import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.dto.request.reference.FakeHadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.FakeHadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import com.jamil.ahadith.features.notification.mapper.NotificationMapper;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private HadithRepository hadithRepository;

    @Mock
    private FakeHadithRepository fakeHadithRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private HadithSearchService hadithSearchService;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(
                notificationRepository,
                notificationMapper,
                entityManager,
                new AdminPageService(),
                hadithRepository,
                fakeHadithRepository,
                currentUserService,
                auditEventPublisher,
                hadithSearchService
        );

        lenient()
                .when(notificationMapper.toResponseDto(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    var dto = new NotificationResponseDto();
                    if (notification.getFakeHadith() != null) {
                        dto.setFakeHadith(new FakeHadithReferenceResponseDto(
                                notification.getFakeHadith().getId(),
                                "Mapped fake"
                        ));
                    }
                    return dto;
                });
    }

    @Test
    void getNotificationsShouldBulkLoadHadithsOnceAndKeepFakeHadithMapping() {
        UUID hadithId = UUID.randomUUID();
        var first = notification(hadithId, UUID.randomUUID());
        var second = notification(null, UUID.randomUUID());
        var pageable = PageRequest.of(0, 10);

        when(notificationRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
        when(hadithSearchService.getHadithCardsByIdsInOrder(anyList()))
                .thenReturn(List.of(searchItem(hadithId, "Full notification hadith")));

        var response = service.getNotifications(pageable);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getHadith().getId())
                .isEqualTo(hadithId);
        assertThat(response.getItems().get(0).getHadith().getText())
                .isEqualTo("Full notification hadith");
        assertThat(response.getItems().get(0).getFakeHadith().getText())
                .isEqualTo("Mapped fake");
        assertThat(response.getItems().get(1).getHadith()).isNull();
        assertThat(response.getItems().get(1).getFakeHadith().getText())
                .isEqualTo("Mapped fake");

        ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(hadithSearchService).getHadithCardsByIdsInOrder(idsCaptor.capture());
        assertThat(idsCaptor.getValue()).containsExactly(hadithId);
    }

    @Test
    void getNotificationByIdShouldKeepHadithNullAndSkipSearch() {
        UUID id = UUID.randomUUID();
        var notification = notification(null, UUID.randomUUID());
        notification.setId(id);

        when(notificationRepository.findById(id))
                .thenReturn(Optional.of(notification));

        var response = service.getNotificationById(id);

        assertThat(response.getHadith()).isNull();
        assertThat(response.getFakeHadith().getText()).isEqualTo("Mapped fake");
        verify(hadithSearchService, never()).getHadithCardsByIdsInOrder(anyList());
    }

    @Test
    void createNotificationShouldPopulateHadithAndKeepFakeHadithMapping() {
        UUID hadithId = UUID.randomUUID();
        UUID fakeHadithId = UUID.randomUUID();
        var request = request(hadithId, fakeHadithId);
        var entity = new Notification();

        when(notificationMapper.toEntity(request)).thenReturn(entity);
        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith(hadithId)));
        when(fakeHadithRepository.findById(fakeHadithId)).thenReturn(Optional.of(fakeHadith(fakeHadithId)));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(notificationRepository.saveAndFlush(entity)).thenReturn(entity);
        when(hadithSearchService.getHadithCardsByIdsInOrder(List.of(hadithId)))
                .thenReturn(List.of(searchItem(hadithId, "Created full hadith")));

        var response = service.createNotification(request);

        assertThat(response.getHadith().getId()).isEqualTo(hadithId);
        assertThat(response.getHadith().getText()).isEqualTo("Created full hadith");
        assertThat(response.getFakeHadith().getId()).isEqualTo(fakeHadithId);
        assertThat(response.getFakeHadith().getText()).isEqualTo("Mapped fake");
    }

    private Notification notification(UUID hadithId, UUID fakeHadithId) {
        var notification = new Notification();
        notification.setId(UUID.randomUUID());
        if (hadithId != null) {
            notification.setHadith(hadith(hadithId));
        }
        notification.setFakeHadith(fakeHadith(fakeHadithId));
        return notification;
    }

    private Hadith hadith(UUID id) {
        var hadith = new Hadith();
        hadith.setId(id);
        return hadith;
    }

    private FakeHadith fakeHadith(UUID id) {
        var fakeHadith = new FakeHadith();
        fakeHadith.setId(id);
        return fakeHadith;
    }

    private NotificationRequestDto request(UUID hadithId, UUID fakeHadithId) {
        var request = new NotificationRequestDto();
        request.setTitle("Title");
        request.setBody("Body");
        request.setType(NotificationType.daily_hadith);
        request.setHadith(hadithReference(hadithId));
        request.setFakeHadith(fakeHadithReference(fakeHadithId));
        return request;
    }

    private HadithReferenceRequestDto hadithReference(UUID id) {
        var reference = new HadithReferenceRequestDto();
        reference.setId(id);
        return reference;
    }

    private FakeHadithReferenceRequestDto fakeHadithReference(UUID id) {
        var reference = new FakeHadithReferenceRequestDto();
        reference.setId(id);
        return reference;
    }

    private HadithSearchItemDto searchItem(UUID id, String text) {
        return new HadithSearchItemDto(
                id,
                text,
                "Normal " + text,
                7,
                "marfu",
                "Sanad " + text,
                null,
                null,
                null,
                null,
                List.of(),
                false,
                true
        );
    }
}
