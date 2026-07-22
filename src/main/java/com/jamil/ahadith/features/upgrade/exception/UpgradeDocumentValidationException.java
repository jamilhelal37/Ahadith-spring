package com.jamil.ahadith.features.upgrade.exception;

public class UpgradeDocumentValidationException extends RuntimeException {
    public UpgradeDocumentValidationException(String message) {
        super(message);
    }

    public UpgradeDocumentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
