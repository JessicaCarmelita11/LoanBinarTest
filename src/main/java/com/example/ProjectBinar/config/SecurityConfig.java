package com.example.ProjectBinar.config;

import com.example.ProjectBinar.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration - Konfigurasi Spring Security dengan JWT.
 * 
 * Fitur:
 * - Stateless session (tidak menggunakan session)
 * - JWT authentication filter
 * - Role-based access control (RBAC)
 * - BCrypt password encoding
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - tidak perlu authentication
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Plafond - semua authenticated user bisa lihat
                        .requestMatchers(HttpMethod.GET, "/plafonds/**").authenticated()

                        // Plafond management - hanya BACK_OFFICE
                        .requestMatchers(HttpMethod.POST, "/plafonds/**").hasRole("BACK_OFFICE")
                        .requestMatchers(HttpMethod.PUT, "/plafonds/**").hasRole("BACK_OFFICE")
                        .requestMatchers(HttpMethod.DELETE, "/plafonds/**").hasRole("BACK_OFFICE")

                        // User management - hanya BACK_OFFICE
                        .requestMatchers("/users/**").hasRole("BACK_OFFICE")

                        // Role management - hanya BACK_OFFICE
                        .requestMatchers("/roles/**").hasRole("BACK_OFFICE")

                        // Branch - BRANCH_MANAGER dan BACK_OFFICE
                        .requestMatchers(HttpMethod.GET, "/branches/**").hasAnyRole("BRANCH_MANAGER", "BACK_OFFICE")
                        .requestMatchers("/branches/**").hasRole("BACK_OFFICE")

                        // Semua request lainnya harus authenticated
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
