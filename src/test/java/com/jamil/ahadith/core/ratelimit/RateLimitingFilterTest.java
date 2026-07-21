package com.jamil.ahadith.core.ratelimit;

import com.jamil.ahadith.core.config.SecurityProperties;
import com.jamil.ahadith.core.web.ClientIpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    @ParameterizedTest
    @CsvSource({
            "POST,/auth/register,register",
            "POST,/api/v1/auth/register,register",
            "POST,/auth/login,login",
            "POST,/api/v1/auth/login,login",
            "POST,/auth/forgot-password,forgot-password-ip",
            "POST,/api/v1/auth/forgot-password,forgot-password-ip",
            "POST,/auth/resend-verification,resend-verification",
            "POST,/api/v1/auth/resend-verification,resend-verification",
            "POST,/auth/verify-email,verify-email",
            "POST,/api/v1/auth/verify-email,verify-email",
            "POST,/auth/reset-password,reset-password",
            "POST,/api/v1/auth/reset-password,reset-password",
            "POST,/auth/refresh,refresh",
            "POST,/api/v1/auth/refresh,refresh",
            "POST,/ahadith/search,public-hadith-search",
            "POST,/api/v1/ahadith/search,public-hadith-search"
    })
    void shouldApplyExpectedPolicy(String method, String path, String policy) throws Exception {
        RecordingRateLimitService rateLimitService = new RecordingRateLimitService();
        RateLimitingFilter filter = filter(rateLimitService);
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("203.0.113.10");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(rateLimitService.calls).containsExactly(policy + ":ip:203.0.113.10");
    }

    @Test
    void shouldIgnoreOptionsRequests() throws Exception {
        RecordingRateLimitService rateLimitService = new RecordingRateLimitService();
        RateLimitingFilter filter = filter(rateLimitService);

        filter.doFilter(
                new MockHttpServletRequest("OPTIONS", "/auth/login"),
                new MockHttpServletResponse(),
                new MockFilterChain()
        );

        assertThat(rateLimitService.calls).isEmpty();
    }

    private RateLimitingFilter filter(RecordingRateLimitService rateLimitService) {
        SecurityProperties securityProperties = new SecurityProperties();
        RateLimitKeyResolver keyResolver = new RateLimitKeyResolver(new ClientIpService(securityProperties));
        return new RateLimitingFilter(rateLimitService, keyResolver, (request, response, handler, ex) -> null);
    }

    private static final class RecordingRateLimitService implements RateLimitService {
        private final List<String> calls = new ArrayList<>();

        @Override
        public RateLimitResult tryConsume(String policyName, String key) {
            calls.add(policyName + ":" + key);
            return RateLimitResult.success();
        }
    }
}
