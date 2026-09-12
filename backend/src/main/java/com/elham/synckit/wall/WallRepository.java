package com.elham.synckit.wall;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WallRepository extends JpaRepository<Wall, UUID> {

    Optional<Wall> findByShareToken(String shareToken);

    boolean existsByShareToken(String shareToken);
}
