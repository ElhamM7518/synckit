package com.elham.synckit.wall;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WallMemberRepository extends JpaRepository<WallMember, WallMemberId> {

    List<WallMember> findByUserId(UUID userId);

    Optional<WallMember> findByWallIdAndUserId(UUID wallId, UUID userId);
}
