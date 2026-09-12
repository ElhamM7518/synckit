package com.elham.synckit.auth;

import com.elham.synckit.auth.dto.AuthResponse;
import com.elham.synckit.auth.dto.LoginRequest;
import com.elham.synckit.auth.dto.RegisterRequest;
import com.elham.synckit.auth.dto.UserResponse;
import com.elham.synckit.common.ApiException;
import com.elham.synckit.user.User;
import com.elham.synckit.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                Instant.now()
        );

        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
        return new AuthResponse(token, userResponse);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
