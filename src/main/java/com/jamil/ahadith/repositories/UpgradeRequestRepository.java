package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.UpgradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, UUID> {
}