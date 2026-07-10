package com.jamil.ahadith;

import com.jamil.ahadith.config.JwtConfig;
import com.jamil.ahadith.config.MailConfigProperties;
import com.jamil.ahadith.config.ProductionConfigurationValidator;
import com.jamil.ahadith.dtos.requests.HadithSearchRequest;
import com.jamil.ahadith.dtos.requests.SearchMode;
import com.jamil.ahadith.exceptions.InvalidRequestException;
import com.jamil.ahadith.repositories.BookRepository;
import com.jamil.ahadith.repositories.HadithRepository;
import com.jamil.ahadith.repositories.MuhaddithRepository;
import com.jamil.ahadith.repositories.RawiRepository;
import com.jamil.ahadith.repositories.RulingRepository;
import com.jamil.ahadith.repositories.TopicRepository;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.HadithSearchService;
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
    private MuhaddithRepository muhaddithRepository;
    @Mock
    private RawiRepository rawiRepository;
    @Mock
    private RulingRepository rulingRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private TopicRepository topicRepository;

    @Test
    void productionValidatorShouldFailFastForMissingSensitiveConfiguration() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("");
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
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("a".repeat(64));
        jwtConfig.setExpiration(Duration.ofHours(1));
        jwtConfig.setRefreshExpiration(Duration.ofDays(7));

        MailConfigProperties mail = new MailConfigProperties();
        mail.setFrom("no-reply@ahadith.local");
        mail.setFrontendBaseUrl("https://app.ahadith.local");

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("spring.mail.host", "smtp.gmail.com")
                .withProperty("spring.mail.username", "mailer@ahadith.local")
                .withProperty("spring.mail.password", "gmail-app-password-value")
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
    void productionValidatorShouldRejectInvalidMailConfigurationWithoutLeakingValues() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("a".repeat(64));
        MailConfigProperties mail = new MailConfigProperties();
        mail.setFrom("not-an-email");
        mail.setFrontendBaseUrl("not-a-url");
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/ahadith")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("spring.mail.host", "smtp.gmail.com")
                .withProperty("spring.mail.username", "mailer@ahadith.local")
                .withProperty("spring.mail.password", "change-this-password")
                .withProperty("app.cloudinary.cloud-name", "cloud")
                .withProperty("app.cloudinary.api-key", "key")
                .withProperty("app.cloudinary.api-secret", "cloudinary-secret-value");
        environment.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(environment, jwtConfig, mail);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_MAIL_PASSWORD")
                .hasMessageNotContaining("change-this-password");
    }

    @Test
    void adminPageServiceShouldClampSizeAndValidateSortAllowlist() {
        AdminPageService service = new AdminPageService();

        Pageable pageable = service.pageable(-1, 500, null, Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();

        assertThatThrownBy(() -> service.pageable(0, 20, "password,asc", Set.of("createdAt"),
                Sort.by("createdAt")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void publicSearchShouldPreserveExactAndFlexibleModesAsDistinctRepositoryStrategies() {
        HadithSearchService service = new HadithSearchService(
                hadithRepository,
                muhaddithRepository,
                rawiRepository,
                rulingRepository,
                bookRepository,
                topicRepository);
        when(hadithRepository.searchPublicIds(
                any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        HadithSearchRequest exact = new HadithSearchRequest();
        exact.setQuery("حديث");
        exact.setMode(SearchMode.EXACT);
        service.publicSearch(exact);

        HadithSearchRequest flexible = new HadithSearchRequest();
        flexible.setQuery("حديث");
        flexible.setMode(SearchMode.FLEXIBLE);
        service.publicSearch(flexible);

        ArgumentCaptor<String> modeCaptor = ArgumentCaptor.forClass(String.class);
        verify(hadithRepository, org.mockito.Mockito.times(2)).searchPublicIds(
                any(), modeCaptor.capture(), any(), anyBoolean(), any(), any(), any(), any(), any(), any(),
                any(Pageable.class));

        assertThat(modeCaptor.getAllValues()).containsExactly("EXACT", "FLEXIBLE");
    }
}
