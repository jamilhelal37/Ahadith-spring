package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
}