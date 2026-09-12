package com.elham.synckit.wall;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wall_members")
@IdClass(WallMemberId.class)
public class WallMember {

    @Id
    @Column(name = "wall_id", nullable = false)
    private UUID wallId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WallRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected WallMember() {
    }

    public WallMember(UUID wallId, UUID userId, WallRole role, Instant joinedAt) {
        this.wallId = wallId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getWallId() {
        return wallId;
    }

    public UUID getUserId() {
        return userId;
    }

    public WallRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
