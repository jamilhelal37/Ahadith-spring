package com.jamil.ahadith.features.search;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.core.config.JwtConfig;
import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.config.ProductionConfigurationValidator;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.request.SearchMode;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.search.service.SearchHistoryService;
import com.jamil.ahadith.features.search.semantic.service.SemanticSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationPaginationSearchTest {

    @Mock
    private HadithRepository hadithRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SearchHistoryService searchHistoryService;

    @Mock
    private SemanticSearchService semanticSearchService;

    @Test
    void productionValidatorShouldFailFastForMissingSensitiveConfiguration() {
        JwtConfig jwtConfig = new JwtConfig("", Duration.ofSeconds(0), Duration.ofSeconds(0));
        MailConfigProperties mail = new MailConfigProperties();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void productionValidatorShouldAcceptCompleteProductionConfiguration() {
        JwtConfig jwtConfig = new JwtConfig("a".repeat(64), Duration.ofHours(1), Duration.ofDays(7));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setEnabled(true);
        mail.setProvider("resend");
        mail.setResendApiKey("re_test_resend_key_value");
        mail.setFrom("no-reply@mail.jamilhelal.me");
        mail.setFrontendBaseUrl("https://api.jamilhelal.me");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatCode(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .doesNotThrowAnyException();
    }

    @Test
    void productionValidatorShouldRejectMissingResendApiKeyWithoutRequiringSmtpVariables() {
        JwtConfig jwtConfig = new JwtConfig("a".repeat(64), Duration.ofHours(1), Duration.ofDays(7));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setEnabled(true);
        mail.setProvider("resend");
        mail.setFrom("no-reply@mail.jamilhelal.me");
        mail.setFrontendBaseUrl("https://api.jamilhelal.me");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESEND_API_KEY")
                .hasMessageNotContaining("SPRING_MAIL");
    }

    @Test
    void productionValidatorShouldRejectInvalidMailConfigurationWithoutLeakingValues() {
        JwtConfig jwtConfig = new JwtConfig("a".repeat(64), Duration.ofHours(1), Duration.ofDays(7));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setEnabled(true);
        mail.setProvider("resend");
        mail.setResendApiKey("change-this-resend-key");
        mail.setFrom("not-an-email");
        mail.setFrontendBaseUrl("not-a-url");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESEND_API_KEY")
                .hasMessageNotContaining("change-this-resend-key");
    }

    @Test
    void productionValidatorShouldAllowDisabledMailWithoutResendApiKey() {
        JwtConfig jwtConfig = new JwtConfig("a".repeat(64), Duration.ofHours(1), Duration.ofDays(7));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setEnabled(false);
        mail.setProvider("resend");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatCode(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .doesNotThrowAnyException();
    }

    @Test
    void productionValidatorShouldRejectUnsupportedMailProvider() {
        JwtConfig jwtConfig = new JwtConfig("a".repeat(64), Duration.ofHours(1), Duration.ofDays(7));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setEnabled(true);
        mail.setProvider("smtp");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_MAIL_PROVIDER");
    }

    @Test
    void adminPageServiceShouldClampSizeAndValidateSortAllowlist() {
        AdminPageService service = new AdminPageService();

        Pageable pageable = service.pageable(
                -1,
                500,
                null,
                Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))
        );

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();

        assertThatThrownBy(() -> service.pageable(
                0,
                20,
                "password,asc",
                Set.of("createdAt"),
                Sort.by("createdAt")
        ))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void publicSearchShouldPreserveExactAndFlexibleModesAsDistinctRepositoryStrategies() {
        HadithSearchService service = new HadithSearchService(
                searchHistoryService,
                hadithRepository,
                bookRepository,
                semanticSearchService
        );

        when(hadithRepository.searchPublicIds(
                any(),
                any(),
                any(),
                anyBoolean(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(Page.empty(PageRequest.of(0, 20)));

        HadithSearchRequest exact = new HadithSearchRequest();
        exact.setQuery("حديث");
        exact.setMode(SearchMode.EXACT);

        service.publicSearch(exact);

        HadithSearchRequest flexible = new HadithSearchRequest();
        flexible.setQuery("حديث");
        flexible.setMode(SearchMode.FLEXIBLE);

        service.publicSearch(flexible);

        ArgumentCaptor<String> modeCaptor = ArgumentCaptor.forClass(String.class);

        verify(hadithRepository, org.mockito.Mockito.times(2))
                .searchPublicIds(
                        any(),
                        modeCaptor.capture(),
                        any(),
                        anyBoolean(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(Pageable.class)
                );

        assertThat(modeCaptor.getAllValues())
                .containsExactly("EXACT", "FLEXIBLE");
    }
}
