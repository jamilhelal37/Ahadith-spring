package com.jamil.ahadith.core.config;

import com.jamil.ahadith.core.ratelimit.RateLimitingFilter;
import com.jamil.ahadith.core.security.SecurityExceptionResponder;
import com.jamil.ahadith.core.security.SecurityRoleUtils;
import com.jamil.ahadith.core.security.filter.JwtAuthenticationFilter;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserRepository userRepository;
    private final SecurityExceptionResponder securityExceptionResponder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/verify-email",
                                "/verify-email.html",
                                "/verify-email.css",
                                "/verify-email.js"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/metrics/**",
                                "/actuator/prometheus"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/auth/google",
                                "/auth/verify-email",
                                "/auth/resend-verification",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/google",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/ahadith/search",
                                "/api/v1/ahadith/search"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/search/filters"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/ahadith/*/comments"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/scholar/hadiths/*/comments",
                                "/api/v1/scholar/comments/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.scholar))

                        .requestMatchers(
                                "/api/v1/admin/comments/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(
                                "/admin/**",
                                "/api/v1/admin/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                "/scholar/**",
                                "/api/v1/scholar/**"
                        ).hasAnyAuthority(
                                SecurityRoleUtils.authority(UserType.scholar),
                                SecurityRoleUtils.authority(UserType.admin)
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/ahadith/**",
                                "/books/**",
                                "/rawis/**",
                                "/rulings/**",
                                "/topics/**",
                                "/muhaddiths/**",
                                "/explaining/**",
                                "/fake-ahadith/**",
                                "/similar-ahadith/**",
                                "/notifications/**",
                                "/upgrade-requests/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/ahadith/**",
                                "/books/**",
                                "/rawis/**",
                                "/rulings/**",
                                "/topics/**",
                                "/muhaddiths/**",
                                "/explaining/**",
                                "/fake-ahadith/**",
                                "/similar-ahadith/**",
                                "/notifications/**",
                                "/upgrade-requests/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/ahadith/**",
                                "/books/**",
                                "/rawis/**",
                                "/rulings/**",
                                "/topics/**",
                                "/muhaddiths/**",
                                "/explaining/**",
                                "/fake-ahadith/**",
                                "/similar-ahadith/**",
                                "/notifications/**",
                                "/upgrade-requests/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/ahadith/**",
                                "/books/**",
                                "/rawis/**",
                                "/rulings/**",
                                "/topics/**",
                                "/muhaddiths/**",
                                "/explaining/**",
                                "/fake-ahadith/**",
                                "/similar-ahadith/**",
                                "/notifications/**",
                                "/upgrade-requests/**"
                        ).hasAuthority(SecurityRoleUtils.authority(UserType.admin))

                        .requestMatchers(
                                "/me/**",
                                "/api/v1/me/**",
                                "/search/history/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/ahadith",
                                "/ahadith/**",
                                "/api/v1/ahadith",
                                "/api/v1/ahadith/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/books",
                                "/books/**",
                                "/api/v1/books",
                                "/api/v1/books/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/rawis",
                                "/rawis/**",
                                "/api/v1/rawis",
                                "/api/v1/rawis/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/rulings",
                                "/rulings/**",
                                "/api/v1/rulings",
                                "/api/v1/rulings/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/topics",
                                "/topics/**",
                                "/api/v1/topics",
                                "/api/v1/topics/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/muhaddiths",
                                "/muhaddiths/**",
                                "/api/v1/muhaddiths",
                                "/api/v1/muhaddiths/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/explaining",
                                "/explaining/**",
                                "/api/v1/explaining",
                                "/api/v1/explaining/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/fake-ahadith",
                                "/fake-ahadith/**",
                                "/api/v1/fake-ahadith",
                                "/api/v1/fake-ahadith/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/filterslist"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                securityExceptionResponder.write(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Unauthorized",
                                        "Authentication required"
                                ))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                securityExceptionResponder.write(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Forbidden",
                                        "Access denied"
                                ))
                )
                .addFilterBefore(
                        rateLimitingFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        user.getStatus() == UserStatus.active,
                        true,
                        true,
                        user.getStatus() != UserStatus.disabled,
                        List.of(
                                new SimpleGrantedAuthority(
                                        SecurityRoleUtils.authority(user.getType())
                                )
                        )
                ))
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}