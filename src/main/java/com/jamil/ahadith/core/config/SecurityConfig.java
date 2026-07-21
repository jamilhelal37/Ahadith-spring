package com.jamil.ahadith.core.config;

import com.jamil.ahadith.features.user.entity.UserStatus;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.core.security.filter.JwtAuthenticationFilter;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.SecurityRoleUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/verify-email", "/verify-email.html", "/verify-email.css", "/verify-email.js")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register", "/auth/login", "/auth/refresh", "/auth/logout",
                                "/auth/verify-email", "/auth/resend-verification",
                                "/auth/forgot-password", "/auth/reset-password")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/ahadith/search").permitAll()
                        .requestMatchers(
                                "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasAuthority(SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers("/scholar/**")
                        .hasAnyAuthority(
                                SecurityRoleUtils.authority(UserType.scholar),
                                SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers(HttpMethod.POST,
                                "/ahadith/**", "/books/**", "/rawis/**", "/rulings/**", "/topics/**", "/muhaddiths/**",
                                "/explaining/**", "/fake-ahadith/**", "/similar-ahadith/**",
                                "/notifications/**", "/upgrade-requests/**")
                        .hasAuthority(SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers(HttpMethod.PUT,
                                "/ahadith/**", "/books/**", "/rawis/**", "/rulings/**", "/topics/**", "/muhaddiths/**",
                                "/explaining/**", "/fake-ahadith/**", "/similar-ahadith/**",
                                "/notifications/**", "/upgrade-requests/**")
                        .hasAuthority(SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers(HttpMethod.PATCH,
                                "/ahadith/**", "/books/**", "/rawis/**", "/rulings/**", "/topics/**", "/muhaddiths/**",
                                "/explaining/**", "/fake-ahadith/**", "/similar-ahadith/**",
                                "/notifications/**", "/upgrade-requests/**")
                        .hasAuthority(SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers(HttpMethod.DELETE,
                                "/ahadith/**", "/books/**", "/rawis/**", "/rulings/**", "/topics/**", "/muhaddiths/**",
                                "/explaining/**", "/fake-ahadith/**", "/similar-ahadith/**",
                                "/notifications/**", "/upgrade-requests/**")
                        .hasAuthority(SecurityRoleUtils.authority(UserType.admin))
                        .requestMatchers("/me/**", "/search/history/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/ahadith/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/rawis/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/rulings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/topics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/muhaddiths/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/explaining/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fake-ahadith/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/filterslist").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Access denied\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        user.getStatus() == com.jamil.ahadith.features.user.entity.UserStatus.active,
                        true,
                        true,
                        user.getStatus() != com.jamil.ahadith.features.user.entity.UserStatus.disabled,
                        List.of(new SimpleGrantedAuthority(SecurityRoleUtils.authority(user.getType())))))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
