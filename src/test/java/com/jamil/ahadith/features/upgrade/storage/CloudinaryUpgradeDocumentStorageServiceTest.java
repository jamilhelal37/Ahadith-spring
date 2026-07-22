package com.jamil.ahadith.features.upgrade.storage;

import com.cloudinary.Cloudinary;
import com.jamil.ahadith.features.upgrade.config.UpgradeDocumentProperties;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentTooLargeException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CloudinaryUpgradeDocumentStorageServiceTest {

    @Test
    void uploadShouldRejectEmptyFile() {
        CloudinaryUpgradeDocumentStorageService service = service(DataSize.ofMegabytes(10), 20);

        assertThatThrownBy(() -> service.upload(file("empty.pdf", "application/pdf", new byte[0]), UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentValidationException.class);
    }

    @Test
    void uploadShouldRejectForgedPdfContent() {
        CloudinaryUpgradeDocumentStorageService service = service(DataSize.ofMegabytes(10), 20);

        assertThatThrownBy(() -> service.upload(
                file("credentials.pdf", "application/pdf", "%PDF-not-a-valid-document".getBytes()),
                UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentValidationException.class);
    }

    @Test
    void uploadShouldRejectWrongContentTypeOrExtension() {
        CloudinaryUpgradeDocumentStorageService service = service(DataSize.ofMegabytes(10), 20);

        assertThatThrownBy(() -> service.upload(file("credentials.txt", "application/pdf", "%PDF-".getBytes()),
                UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentValidationException.class);
        assertThatThrownBy(() -> service.upload(file("credentials.pdf", "text/plain", "%PDF-".getBytes()),
                UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentValidationException.class);
    }

    @Test
    void uploadShouldRejectFileLargerThanConfiguredLimit() {
        CloudinaryUpgradeDocumentStorageService service = service(DataSize.ofBytes(4), 20);

        assertThatThrownBy(() -> service.upload(file("credentials.pdf", "application/pdf", "%PDF-1.4".getBytes()),
                UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentTooLargeException.class);
    }

    @Test
    void uploadShouldRejectEncryptedPdf() throws IOException {
        CloudinaryUpgradeDocumentStorageService service = service(DataSize.ofMegabytes(10), 20);

        assertThatThrownBy(() -> service.upload(file("credentials.pdf", "application/pdf", encryptedPdf()),
                UUID.randomUUID()))
                .isInstanceOf(UpgradeDocumentValidationException.class);
    }

    private CloudinaryUpgradeDocumentStorageService service(DataSize maxSize, int maxPages) {
        UpgradeDocumentProperties properties = new UpgradeDocumentProperties();
        properties.setMaxSize(maxSize);
        properties.setMaxPages(maxPages);
        properties.setDownloadTtl(Duration.ofMinutes(5));
        return new CloudinaryUpgradeDocumentStorageService(mock(Cloudinary.class), properties);
    }

    private MockMultipartFile file(String name, String contentType, byte[] content) {
        return new MockMultipartFile("document", name, contentType, content);
    }

    private byte[] encryptedPdf() throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            AccessPermission permission = new AccessPermission();
            StandardProtectionPolicy policy = new StandardProtectionPolicy("owner", "user", permission);
            policy.setEncryptionKeyLength(128);
            document.protect(policy);
            document.save(output);
            return output.toByteArray();
        }
    }
}
