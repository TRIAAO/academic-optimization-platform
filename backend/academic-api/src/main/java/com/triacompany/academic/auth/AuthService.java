package com.triacompany.academic.auth;

import com.triacompany.academic.security.JwtService;
import com.triacompany.academic.user.User;
import com.triacompany.academic.user.UserRepository;
import com.triacompany.academic.user.UserResponse;
import com.triacompany.academic.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }

        UserRole role = request.role() == null ? UserRole.RESEARCHER : request.role();

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMinutes(),
                UserResponse.fromEntity(saved)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("Usuário inativo.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMinutes(),
                UserResponse.fromEntity(user)
        );
    }
}