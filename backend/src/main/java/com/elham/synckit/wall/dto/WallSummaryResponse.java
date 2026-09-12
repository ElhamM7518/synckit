package com.elham.synckit.wall.dto;

import com.elham.synckit.wall.WallRole;
import java.time.Instant;
import java.util.UUID;

public record WallSummaryResponse(
        UUID id,
        String name,
        WallRole role,
        Instant createdAt
) {
}
