package com.jamil.ahadith.features.account.event;

import com.jamil.ahadith.core.exception.EmailDeliveryException;
import com.jamil.ahadith.core.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountEmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleAccountEmailEvent(AccountEmailEvent event) {
        try {
            switch (event.type()) {
                case VERIFICATION -> emailService.sendVerificationEmail(event.user(), event.rawToken());
                case PASSWORD_RESET -> emailService.sendPasswordResetEmail(event.user(), event.rawToken());
            }
        } catch (EmailDeliveryException e) {
            log.warn("Failed to send {} email for user with ID: {}. Reason: {}", 
                event.type(), event.user().getId(), e.getMessage());
            // Do not rethrow after commit as per requirements
        }
    }
}
