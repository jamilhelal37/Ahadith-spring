package com.jamil.ahadith.core.web;

import com.jamil.ahadith.core.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpServiceTest {

    @Test
    void shouldUseRemoteAddressByDefault() {
        SecurityProperties properties = new SecurityProperties();
        ClientIpService service = new ClientIpService(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.10");
        request.addHeader("X-Forwarded-For", "203.0.113.20, 198.51.100.1");

        assertThat(service.resolve(request)).isEqualTo("198.51.100.10");
    }

    @Test
    void shouldUseFirstForwardedAddressWhenProxyHeadersAreTrusted() {
        SecurityProperties properties = new SecurityProperties();
        properties.setTrustedProxyHeaders(true);
        ClientIpService service = new ClientIpService(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.10");
        request.addHeader("X-Forwarded-For", "203.0.113.20, 198.51.100.1");

        assertThat(service.resolve(request)).isEqualTo("203.0.113.20");
    }
}
