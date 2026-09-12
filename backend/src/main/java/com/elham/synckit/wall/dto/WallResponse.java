package com.elham.synckit.wall.dto;

import com.elham.synckit.wall.WallRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WallResponse(
        UUID id,
        String name,
        UUID ownerId,
        WallRole role,
        String shareToken,
        Instant createdAt
) {
}
