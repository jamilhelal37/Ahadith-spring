package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.features.upgrade.exception.UpgradeRequestNotFoundException;
import com.jamil.ahadith.features.upgrade.repository.UpgradeRequestRepository;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentUploadResult;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import com.jamil.ahadith.features.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpgradeRequestTransactionService {
    private final UpgradeRequestRepository upgradeRequestRepository;
    private final EntityManager entityManager;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public UpgradeRequest createUpgradeRequest(User user, String notes, UpgradeDocumentUploadResult upload) {
        UpgradeRequest upgradeRequest = new UpgradeRequest();
        upgradeRequest.setUser(user);
        upgradeRequest.setStatus(UpgradeStatus.under_review);
        upgradeRequest.setNotes(notes);
        applyDocument(upload, upgradeRequest);
        UpgradeRequest saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional
    public UpgradeRequestDocumentReference deleteUpgradeRequest(UUID id) {
        var request = upgradeRequestRepository.findWithLockingById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        var oldData = AuditData.snapshot(request);
        var documentReference = new UpgradeRequestDocumentReference(request.getDocumentPublicId());
        upgradeRequestRepository.delete(request);
        upgradeRequestRepository.flush();
        auditEventPublisher.publishDelete("upgrade_requests", id, oldData);
        return documentReference;
    }

    private void applyDocument(UpgradeDocumentUploadResult upload, UpgradeRequest request) {
        request.setDocumentAssetId(upload.assetId());
        request.setDocumentPublicId(upload.publicId());
        request.setDocumentResourceType(upload.resourceType());
        request.setDocumentDeliveryType(upload.deliveryType());
        request.setDocumentFormat(upload.format());
        request.setDocumentOriginalName(upload.originalFileName());
        request.setDocumentSizeBytes(upload.sizeBytes());
    }
}
