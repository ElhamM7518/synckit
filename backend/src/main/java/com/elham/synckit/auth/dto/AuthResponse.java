package com.elham.synckit.auth.dto;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {
}
