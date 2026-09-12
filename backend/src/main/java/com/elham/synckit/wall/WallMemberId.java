package com.elham.synckit.wall;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class WallMemberId implements Serializable {

    private UUID wallId;
    private UUID userId;

    public WallMemberId() {
    }

    public WallMemberId(UUID wallId, UUID userId) {
        this.wallId = wallId;
        this.userId = userId;
    }

    public UUID getWallId() {
        return wallId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WallMemberId that)) {
            return false;
        }
        return Objects.equals(wallId, that.wallId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wallId, userId);
    }
}
