package com.triacompany.academic.auth;

import com.triacompany.academic.user.UserResponse;

public record AuthResponse(
        String token,
        String tokenType,
        Long expiresInMinutes,
        UserResponse user
) {
}