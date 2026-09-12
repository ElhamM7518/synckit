package com.elham.synckit.wall;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "walls")
public class Wall {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "share_token", nullable = false, unique = true, length = 32)
    private String shareToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Wall() {
    }

    public Wall(UUID id, String name, UUID ownerId, String shareToken, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.shareToken = shareToken;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
