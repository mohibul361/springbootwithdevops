package com.example.SpringBootWithSecurityAndDevops.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.example.SpringBootWithSecurityAndDevops.enums.Role.ADMIN;
import static com.example.SpringBootWithSecurityAndDevops.enums.Role.MANAGER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors
                        .configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .disable()) // Disable CSRF if you're using JWT or don't need CSRF protection
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/user/update/profile/**").hasAnyRole(MANAGER.name(), ADMIN.name())
                        .requestMatchers("/api/v1/user/profile/**").hasAnyRole(MANAGER.name(), ADMIN.name())
                        .requestMatchers("/api/v1/user/all").hasRole(ADMIN.name())
                        .requestMatchers("/api/v1/user/users/{id}").hasRole(ADMIN.name())
                        .requestMatchers("/api/v1/auth/authenticate").permitAll()
                        .requestMatchers("/api/v1/user/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // Change this to your frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Specify the HTTP methods allowed
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept")); // Specify the allowed headers
        configuration.setExposedHeaders(Arrays.asList("Authorization")); // Specify headers to expose to the client

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
