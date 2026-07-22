package com.jamil.ahadith.core.config;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class MailRestClientConfig {
    @Bean
    RestClientCustomizer mailTimeoutRestClientCustomizer(MailConfigProperties mailProperties) {
        return builder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(mailProperties.getConnectTimeout());
            requestFactory.setReadTimeout(mailProperties.getReadTimeout());
            builder.requestFactory(requestFactory);
        };
    }
}
