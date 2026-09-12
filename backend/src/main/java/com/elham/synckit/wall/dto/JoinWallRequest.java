package com.elham.synckit.wall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinWallRequest(
        @NotBlank @Size(min = 8, max = 64) String shareToken
) {
}
