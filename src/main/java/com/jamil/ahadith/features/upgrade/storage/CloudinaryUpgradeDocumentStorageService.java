package com.jamil.ahadith.features.upgrade.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jamil.ahadith.features.upgrade.config.UpgradeDocumentProperties;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentTooLargeException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryUpgradeDocumentStorageService implements UpgradeDocumentStorageService {
    private static final String RESOURCE_TYPE = "raw";
    private static final String DELIVERY_TYPE = "authenticated";
    private static final String FORMAT = "pdf";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes();

    private final Cloudinary cloudinary;
    private final UpgradeDocumentProperties properties;

    @Override
    public UpgradeDocumentUploadResult upload(MultipartFile file, UUID userId) {
        Path tempFile = validate(file);
        String originalName = cleanOriginalName(file.getOriginalFilename());
        String publicId = "upgrade-requests/%s/%s".formatted(userId, UUID.randomUUID());
        try (InputStream input = Files.newInputStream(tempFile)) {
            Map<?, ?> result = cloudinary.uploader().upload(input, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", RESOURCE_TYPE,
                    "type", DELIVERY_TYPE,
                    "overwrite", false,
                    "format", FORMAT,
                    "filename", originalName
            ));
            return new UpgradeDocumentUploadResult(
                    stringValue(result.get("asset_id")),
                    stringValue(result.get("public_id")),
                    stringValueOrDefault(result.get("resource_type"), RESOURCE_TYPE),
                    DELIVERY_TYPE,
                    stringValueOrDefault(result.get("format"), FORMAT),
                    originalName,
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new UpgradeDocumentStorageException("Failed to upload upgrade document", ex);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Override
    public String createDownloadUrl(String publicId, Instant expiresAt) {
        try {
            return cloudinary.privateDownload(publicId, FORMAT, ObjectUtils.asMap(
                    "resource_type", RESOURCE_TYPE,
                    "type", DELIVERY_TYPE,
                    "expires_at", expiresAt.getEpochSecond(),
                    "attachment", true
            ));
        } catch (Exception ex) {
            throw new UpgradeDocumentStorageException("Failed to create upgrade document download URL", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", RESOURCE_TYPE,
                    "type", DELIVERY_TYPE
            ));
        } catch (IOException ex) {
            throw new UpgradeDocumentStorageException("Failed to delete upgrade document", ex);
        }
    }

    private Path validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UpgradeDocumentValidationException("Upgrade document PDF is required");
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw new UpgradeDocumentTooLargeException("Upgrade document size exceeds the configured limit");
        }
        if (!CONTENT_TYPE.equalsIgnoreCase(String.valueOf(file.getContentType()))) {
            throw new UpgradeDocumentValidationException("Upgrade document must be a PDF file");
        }
        String originalName = cleanOriginalName(file.getOriginalFilename());
        if (!originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new UpgradeDocumentValidationException("Upgrade document filename must end with .pdf");
        }

        Path tempFile = copyToTempFile(file);
        validateMagicBytes(tempFile);
        validatePdfDocument(tempFile);
        return tempFile;
    }

    private Path copyToTempFile(MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("upgrade-document-", ".pdf");
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        } catch (IOException ex) {
            throw new UpgradeDocumentValidationException("Unable to read upgrade document", ex);
        }
    }

    private void validateMagicBytes(Path file) {
        try (InputStream input = Files.newInputStream(file)) {
            byte[] header = input.readNBytes(PDF_MAGIC.length);
            if (header.length != PDF_MAGIC.length) {
                throw new UpgradeDocumentValidationException("Upgrade document is not a valid PDF");
            }
            for (int i = 0; i < PDF_MAGIC.length; i++) {
                if (header[i] != PDF_MAGIC[i]) {
                    throw new UpgradeDocumentValidationException("Upgrade document is not a valid PDF");
                }
            }
        } catch (IOException ex) {
            throw new UpgradeDocumentValidationException("Unable to read upgrade document", ex);
        }
    }

    private void validatePdfDocument(Path file) {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            if (document.isEncrypted()) {
                throw new UpgradeDocumentValidationException("Encrypted PDF files are not allowed");
            }
            if (document.getNumberOfPages() > properties.getMaxPages()) {
                throw new UpgradeDocumentValidationException("Upgrade document has too many pages");
            }
        } catch (UpgradeDocumentValidationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new UpgradeDocumentValidationException("Upgrade document is not a valid PDF", ex);
        }
    }

    private String cleanOriginalName(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename).replace('\\', '/');
        int lastSlash = cleaned.lastIndexOf('/');
        String name = lastSlash >= 0 ? cleaned.substring(lastSlash + 1) : cleaned;
        return name.isBlank() ? "document.pdf" : name;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String stringValueOrDefault(Object value, String fallback) {
        String stringValue = stringValue(value);
        return stringValue == null || stringValue.isBlank() ? fallback : stringValue;
    }

    private void deleteTempFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}
