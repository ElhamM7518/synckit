package com.elham.synckit.wall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWallRequest(
        @NotBlank @Size(max = 100) String name
) {
}
