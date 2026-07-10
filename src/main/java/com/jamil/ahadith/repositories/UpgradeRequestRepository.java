package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.UpgradeRequest;
import com.jamil.ahadith.entities.UpgradeStatus;
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
