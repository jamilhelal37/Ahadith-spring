package com.jamil.ahadith.features.upgrade.repository;

import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, UUID> {
    boolean existsByUserIdAndStatusIn(UUID userId, Collection<UpgradeStatus> statuses);

    List<UpgradeRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<UpgradeRequest> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, Collection<UpgradeStatus> statuses);

    Page<UpgradeRequest> findByStatus(UpgradeStatus status, Pageable pageable);
}
