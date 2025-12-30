package com.example.ProjectBinar.service;

import com.example.ProjectBinar.dto.AuthRequest;
import com.example.ProjectBinar.dto.AuthResponse;
import com.example.ProjectBinar.entity.Role;
import com.example.ProjectBinar.entity.User;
import com.example.ProjectBinar.security.CustomUserDetails;
import com.example.ProjectBinar.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Auth Service - Menangani logic autentikasi.
 * 
 * Fungsi utama:
 * - Authenticate user dengan username/password
 * - Generate JWT token setelah login berhasil
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Authenticate user dan generate JWT token.
     * 
     * @param request AuthRequest berisi username dan password
     * @return AuthResponse berisi token dan user info
     * @throws AuthenticationException jika credentials invalid
     */
    public AuthResponse authenticate(AuthRequest request) {
        // Authenticate dengan Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Get user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
