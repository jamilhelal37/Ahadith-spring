package com.jamil.ahadith.features.upgrade.repository;

import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, UUID> {

    boolean existsByUserIdAndStatusIn(
            UUID userId,
            Collection<UpgradeStatus> statuses
    );

    Optional<UpgradeRequest> findFirstByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<UpgradeRequest> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
            UUID userId,
            Collection<UpgradeStatus> statuses
    );

    Page<UpgradeRequest> findByStatus(
            UpgradeStatus status,
            Pageable pageable
    );

    Optional<UpgradeRequest> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from UpgradeRequest request where request.id = :id")
    Optional<UpgradeRequest> findWithLockingById(UUID id);
}