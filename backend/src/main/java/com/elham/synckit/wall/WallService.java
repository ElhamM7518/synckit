package com.elham.synckit.wall;

import com.elham.synckit.common.ApiException;
import com.elham.synckit.wall.dto.CreateWallRequest;
import com.elham.synckit.wall.dto.JoinWallRequest;
import com.elham.synckit.wall.dto.WallResponse;
import com.elham.synckit.wall.dto.WallSummaryResponse;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WallService {

    private static final int SHARE_TOKEN_ATTEMPTS = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WallRepository wallRepository;
    private final WallMemberRepository wallMemberRepository;

    public WallService(WallRepository wallRepository, WallMemberRepository wallMemberRepository) {
        this.wallRepository = wallRepository;
        this.wallMemberRepository = wallMemberRepository;
    }

    @Transactional
    public WallResponse create(UUID userId, CreateWallRequest request) {
        Instant now = Instant.now();
        UUID wallId = UUID.randomUUID();
        Wall wall = new Wall(
                wallId,
                request.name().trim(),
                userId,
                allocateShareToken(),
                now
        );
        wallRepository.save(wall);
        wallMemberRepository.save(new WallMember(wallId, userId, WallRole.OWNER, now));
        return toResponse(wall, WallRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WallSummaryResponse> list(UUID userId) {
        List<WallMember> memberships = wallMemberRepository.findByUserId(userId);
        Map<UUID, Wall> wallsById = wallRepository.findAllById(
                memberships.stream().map(WallMember::getWallId).toList()
        ).stream().collect(Collectors.toMap(Wall::getId, Function.identity()));

        return memberships.stream()
                .map(membership -> {
                    Wall wall = wallsById.get(membership.getWallId());
                    if (wall == null) {
                        return null;
                    }
                    return new WallSummaryResponse(
                            wall.getId(),
                            wall.getName(),
                            membership.getRole(),
                            wall.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WallSummaryResponse::createdAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public WallResponse get(UUID userId, UUID wallId) {
        Wall wall = wallRepository.findById(wallId)
                .orElseThrow(WallService::wallNotFound);
        WallMember membership = requireMembership(wallId, userId);
        return toResponse(wall, membership.getRole());
    }

    @Transactional
    public WallResponse join(UUID userId, JoinWallRequest request) {
        String token = request.shareToken().trim().toLowerCase(Locale.ROOT);
        Wall wall = wallRepository.findByShareToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Invalid share token"));

        return wallMemberRepository.findByWallIdAndUserId(wall.getId(), userId)
                .map(existing -> toResponse(wall, existing.getRole()))
                .orElseGet(() -> {
                    wallMemberRepository.save(
                            new WallMember(wall.getId(), userId, WallRole.EDITOR, Instant.now())
                    );
                    return toResponse(wall, WallRole.EDITOR);
                });
    }

    private WallMember requireMembership(UUID wallId, UUID userId) {
        return wallMemberRepository.findByWallIdAndUserId(wallId, userId)
                .orElseThrow(WallService::wallNotFound);
    }

    private WallResponse toResponse(Wall wall, WallRole role) {
        String shareToken = role == WallRole.OWNER ? wall.getShareToken() : null;
        return new WallResponse(
                wall.getId(),
                wall.getName(),
                wall.getOwnerId(),
                role,
                shareToken,
                wall.getCreatedAt()
        );
    }

    private String allocateShareToken() {
        for (int attempt = 0; attempt < SHARE_TOKEN_ATTEMPTS; attempt++) {
            String token = generateShareToken();
            if (!wallRepository.existsByShareToken(token)) {
                return token;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a unique share token");
    }

    private static String generateShareToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static ApiException wallNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Wall not found");
    }
}
