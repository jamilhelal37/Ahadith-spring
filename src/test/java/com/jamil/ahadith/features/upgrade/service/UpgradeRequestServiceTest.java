package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.core.ratelimit.RateLimitService;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.upgrade.config.UpgradeDocumentProperties;
import com.jamil.ahadith.features.upgrade.dto.request.UpgradeRequestCreateDto;
import com.jamil.ahadith.features.upgrade.dto.response.MemberUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.mapper.UpgradeRequestMapper;
import com.jamil.ahadith.features.upgrade.repository.UpgradeRequestRepository;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentStorageService;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentUploadResult;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpgradeRequestServiceTest {

    @Mock
    private UpgradeRequestRepository upgradeRequestRepository;

    @Mock
    private UpgradeRequestMapper upgradeRequestMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AdminPageService adminPageService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UpgradeDocumentStorageService documentStorageService;

    @Mock
    private UpgradeDocumentProperties documentProperties;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RateLimitKeyResolver rateLimitKeyResolver;

    @Mock
    private UpgradeRequestTransactionService upgradeRequestTransactionService;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletRequest servletRequest;

    @Test
    void createUpgradeRequestDeletesUploadAndRethrowsOriginalRuntimeExceptionWhenTransactionFails() {
        User user = member();

        UpgradeRequestCreateDto request =
                request();

        UpgradeDocumentUploadResult upload =
                upload();

        RuntimeException failure =
                new RuntimeException(
                        "transaction failed"
                );

        when(
                currentUserService
                        .requireCurrentUser()
        ).thenReturn(user);

        when(
                rateLimitKeyResolver
                        .ipKey(servletRequest)
        ).thenReturn("127.0.0.1");

        when(
                upgradeRequestRepository
                        .existsByUserIdAndStatusIn(
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(
                documentStorageService.upload(
                        request.getDocument(),
                        user.getId()
                )
        ).thenReturn(upload);

        when(
                upgradeRequestTransactionService
                        .createUpgradeRequest(
                                user,
                                request.getNotes(),
                                upload
                        )
        ).thenThrow(failure);

        assertThatThrownBy(
                () -> service()
                        .createUpgradeRequest(
                                request,
                                servletRequest
                        )
        ).isSameAs(failure);

        verify(documentStorageService)
                .delete(
                        upload.publicId()
                );

        verify(
                upgradeRequestMapper,
                never()
        ).toMemberResponseDto(any());
    }

    @Test
    void createUpgradeRequestDeletesUploadAndThrowsConflictWhenTransactionHitsDataIntegrityViolation() {
        User user = member();

        UpgradeRequestCreateDto request =
                request();

        UpgradeDocumentUploadResult upload =
                upload();

        when(
                currentUserService
                        .requireCurrentUser()
        ).thenReturn(user);

        when(
                rateLimitKeyResolver
                        .ipKey(servletRequest)
        ).thenReturn("127.0.0.1");

        when(
                upgradeRequestRepository
                        .existsByUserIdAndStatusIn(
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(
                documentStorageService.upload(
                        request.getDocument(),
                        user.getId()
                )
        ).thenReturn(upload);

        when(
                upgradeRequestTransactionService
                        .createUpgradeRequest(
                                user,
                                request.getNotes(),
                                upload
                        )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate open request"
                )
        );

        assertThatThrownBy(
                () -> service()
                        .createUpgradeRequest(
                                request,
                                servletRequest
                        )
        )
                .isInstanceOf(
                        ConflictException.class
                )
                .hasMessage(
                        "An open upgrade request already exists"
                );

        verify(documentStorageService)
                .delete(
                        upload.publicId()
                );

        verify(
                upgradeRequestMapper,
                never()
        ).toMemberResponseDto(any());
    }

    @Test
    void createUpgradeRequestDoesNotDeleteUploadWhenTransactionSucceeds() {
        User user = member();

        UpgradeRequestCreateDto request =
                request();

        UpgradeDocumentUploadResult upload =
                upload();

        UpgradeRequest saved =
                savedRequest(
                        user,
                        upload
                );

        MemberUpgradeRequestResponseDto response =
                new MemberUpgradeRequestResponseDto();

        when(
                currentUserService
                        .requireCurrentUser()
        ).thenReturn(user);

        when(
                rateLimitKeyResolver
                        .ipKey(servletRequest)
        ).thenReturn("127.0.0.1");

        when(
                upgradeRequestRepository
                        .existsByUserIdAndStatusIn(
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(
                documentStorageService.upload(
                        request.getDocument(),
                        user.getId()
                )
        ).thenReturn(upload);

        when(
                upgradeRequestTransactionService
                        .createUpgradeRequest(
                                user,
                                request.getNotes(),
                                upload
                        )
        ).thenReturn(saved);

        when(
                upgradeRequestMapper
                        .toMemberResponseDto(saved)
        ).thenReturn(response);

        MemberUpgradeRequestResponseDto result =
                service().createUpgradeRequest(
                        request,
                        servletRequest
                );

        assertThat(result)
                .isSameAs(response);

        verify(
                documentStorageService,
                never()
        ).delete(anyString());
    }

    @Test
    void createUpgradeRequestDoesNotDeleteUploadWhenMapperFailsAfterSuccessfulTransaction() {
        User user = member();

        UpgradeRequestCreateDto request =
                request();

        UpgradeDocumentUploadResult upload =
                upload();

        UpgradeRequest saved =
                savedRequest(
                        user,
                        upload
                );

        RuntimeException failure =
                new RuntimeException(
                        "mapper failed"
                );

        when(
                currentUserService
                        .requireCurrentUser()
        ).thenReturn(user);

        when(
                rateLimitKeyResolver
                        .ipKey(servletRequest)
        ).thenReturn("127.0.0.1");

        when(
                upgradeRequestRepository
                        .existsByUserIdAndStatusIn(
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(
                documentStorageService.upload(
                        request.getDocument(),
                        user.getId()
                )
        ).thenReturn(upload);

        when(
                upgradeRequestTransactionService
                        .createUpgradeRequest(
                                user,
                                request.getNotes(),
                                upload
                        )
        ).thenReturn(saved);

        when(
                upgradeRequestMapper
                        .toMemberResponseDto(saved)
        ).thenThrow(failure);

        assertThatThrownBy(
                () -> service()
                        .createUpgradeRequest(
                                request,
                                servletRequest
                        )
        ).isSameAs(failure);

        verify(
                documentStorageService,
                never()
        ).delete(anyString());
    }

    @Test
    void createUpgradeRequestKeepsOriginalTransactionExceptionWhenCleanupFails() {
        User user = member();

        UpgradeRequestCreateDto request =
                request();

        UpgradeDocumentUploadResult upload =
                upload();

        RuntimeException failure =
                new RuntimeException(
                        "transaction failed"
                );

        when(
                currentUserService
                        .requireCurrentUser()
        ).thenReturn(user);

        when(
                rateLimitKeyResolver
                        .ipKey(servletRequest)
        ).thenReturn("127.0.0.1");

        when(
                upgradeRequestRepository
                        .existsByUserIdAndStatusIn(
                                any(),
                                any()
                        )
        ).thenReturn(false);

        when(
                documentStorageService.upload(
                        request.getDocument(),
                        user.getId()
                )
        ).thenReturn(upload);

        when(
                upgradeRequestTransactionService
                        .createUpgradeRequest(
                                user,
                                request.getNotes(),
                                upload
                        )
        ).thenThrow(failure);

        doThrow(
                new UpgradeDocumentStorageException(
                        "cleanup failed"
                )
        )
                .when(documentStorageService)
                .delete(
                        upload.publicId()
                );

        assertThatThrownBy(
                () -> service()
                        .createUpgradeRequest(
                                request,
                                servletRequest
                        )
        ).isSameAs(failure);
    }

    private UpgradeRequestService service() {
        return new UpgradeRequestService(
                upgradeRequestRepository,
                upgradeRequestMapper,
                currentUserService,
                adminPageService,
                notificationRepository,
                documentStorageService,
                documentProperties,
                rateLimitService,
                rateLimitKeyResolver,
                upgradeRequestTransactionService,
                auditEventPublisher,
                eventPublisher
        );
    }

    private User member() {
        User user = new User();

        user.setId(
                UUID.randomUUID()
        );

        user.setType(
                UserType.member
        );

        return user;
    }

    private UpgradeRequestCreateDto request() {
        UpgradeRequestCreateDto request =
                new UpgradeRequestCreateDto();

        request.setNotes(
                "please review"
        );

        request.setDocument(
                new MockMultipartFile(
                        "document",
                        "credentials.pdf",
                        "application/pdf",
                        "%PDF-1.4".getBytes()
                )
        );

        return request;
    }

    private UpgradeDocumentUploadResult upload() {
        return new UpgradeDocumentUploadResult(
                "asset-id",
                "upgrade-requests/user-id/document-id",
                "raw",
                "authenticated",
                "pdf",
                "credentials.pdf",
                128L
        );
    }

    private UpgradeRequest savedRequest(
            User user,
            UpgradeDocumentUploadResult upload
    ) {
        UpgradeRequest request =
                new UpgradeRequest();

        request.setId(
                UUID.randomUUID()
        );

        request.setUser(user);

        request.setDocumentPublicId(
                upload.publicId()
        );

        request.setDocumentOriginalName(
                upload.originalFileName()
        );

        request.setDocumentSizeBytes(
                upload.sizeBytes()
        );

        return request;
    }
}