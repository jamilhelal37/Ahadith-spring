package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentUploadResult;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpgradeRequestTransactionServiceIT extends PostgresIntegrationTestBase {
    @Autowired
    private UpgradeRequestTransactionService upgradeRequestTransactionService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createUpgradeRequestRollsBackInsertedRecordWhenFlushFailsBeforeEventPublication() {
        User user = member("upgrade-rollback@example.com");

        assertThatThrownBy(() -> upgradeRequestTransactionService.createUpgradeRequest(
                user,
                "please review",
                uploadWithInvalidDocumentSize()
        )).isInstanceOf(DataIntegrityViolationException.class);

        Long requestCount = jdbc.queryForObject(
                "select count(*) from public.upgrade_requests where user_id = ?",
                Long.class,
                user.getId()
        );
        assertThat(requestCount).isZero();
    }

    private User member(String email) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("password");
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }

    private UpgradeDocumentUploadResult uploadWithInvalidDocumentSize() {
        return new UpgradeDocumentUploadResult(
                "asset-id",
                "upgrade-requests/user-id/document-id",
                "raw",
                "authenticated",
                "pdf",
                "credentials.pdf",
                -1L
        );
    }
}
