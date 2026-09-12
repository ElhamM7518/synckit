package com.elham.synckit.wall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elham.synckit.common.ApiException;
import com.elham.synckit.wall.dto.CreateWallRequest;
import com.elham.synckit.wall.dto.JoinWallRequest;
import com.elham.synckit.wall.dto.WallResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class WallServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID EDITOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID WALL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private WallRepository wallRepository;

    @Mock
    private WallMemberRepository wallMemberRepository;

    @InjectMocks
    private WallService wallService;

    @Test
    void createPersistsOwnerMembershipAndReturnsShareToken() {
        when(wallRepository.existsByShareToken(any())).thenReturn(false);
        when(wallRepository.save(any(Wall.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(wallMemberRepository.save(any(WallMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WallResponse response = wallService.create(OWNER_ID, new CreateWallRequest("  Darkroom  "));

        assertEquals("Darkroom", response.name());
        assertEquals(OWNER_ID, response.ownerId());
        assertEquals(WallRole.OWNER, response.role());
        assertEquals(32, response.shareToken().length());
        verify(wallMemberRepository).save(any(WallMember.class));
    }

    @Test
    void getHidesShareTokenFromEditors() {
        Wall wall = sampleWall();
        when(wallRepository.findById(WALL_ID)).thenReturn(Optional.of(wall));
        when(wallMemberRepository.findByWallIdAndUserId(WALL_ID, EDITOR_ID))
                .thenReturn(Optional.of(new WallMember(WALL_ID, EDITOR_ID, WallRole.EDITOR, Instant.now())));

        WallResponse response = wallService.get(EDITOR_ID, WALL_ID);

        assertEquals(WallRole.EDITOR, response.role());
        assertNull(response.shareToken());
    }

    @Test
    void getReturnsNotFoundWhenCallerIsNotAMember() {
        when(wallRepository.findById(WALL_ID)).thenReturn(Optional.of(sampleWall()));
        when(wallMemberRepository.findByWallIdAndUserId(WALL_ID, EDITOR_ID)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> wallService.get(EDITOR_ID, WALL_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void listReturnsOnlyMembershipsNewestFirst() {
        Instant older = Instant.parse("2026-01-01T00:00:00Z");
        Instant newer = Instant.parse("2026-02-01T00:00:00Z");
        UUID olderId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Wall olderWall = new Wall(olderId, "Older", OWNER_ID, "a".repeat(32), older);
        Wall newerWall = sampleWall(newer);

        when(wallMemberRepository.findByUserId(OWNER_ID)).thenReturn(List.of(
                new WallMember(olderId, OWNER_ID, WallRole.OWNER, older),
                new WallMember(WALL_ID, OWNER_ID, WallRole.OWNER, newer)
        ));
        when(wallRepository.findAllById(any())).thenReturn(List.of(olderWall, newerWall));

        var walls = wallService.list(OWNER_ID);

        assertEquals(2, walls.size());
        assertEquals(WALL_ID, walls.get(0).id());
        assertEquals(olderId, walls.get(1).id());
    }

    @Test
    void joinAddsEditorWhenTokenMatches() {
        Wall wall = sampleWall();
        when(wallRepository.findByShareToken(wall.getShareToken())).thenReturn(Optional.of(wall));
        when(wallMemberRepository.findByWallIdAndUserId(WALL_ID, EDITOR_ID)).thenReturn(Optional.empty());
        when(wallMemberRepository.save(any(WallMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WallResponse response = wallService.join(EDITOR_ID, new JoinWallRequest("  " + wall.getShareToken().toUpperCase() + "  "));

        assertEquals(WALL_ID, response.id());
        assertEquals(WallRole.EDITOR, response.role());
        assertNull(response.shareToken());
        verify(wallMemberRepository).save(any(WallMember.class));
    }

    @Test
    void joinIsIdempotentForExistingMembers() {
        Wall wall = sampleWall();
        when(wallRepository.findByShareToken(wall.getShareToken())).thenReturn(Optional.of(wall));
        when(wallMemberRepository.findByWallIdAndUserId(WALL_ID, OWNER_ID))
                .thenReturn(Optional.of(new WallMember(WALL_ID, OWNER_ID, WallRole.OWNER, Instant.now())));

        WallResponse response = wallService.join(OWNER_ID, new JoinWallRequest(wall.getShareToken()));

        assertEquals(WallRole.OWNER, response.role());
        assertEquals(wall.getShareToken(), response.shareToken());
        verify(wallMemberRepository, never()).save(any(WallMember.class));
    }

    @Test
    void joinRejectsUnknownToken() {
        when(wallRepository.findByShareToken("missingtokenmissingtokenmissin")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> wallService.join(EDITOR_ID, new JoinWallRequest("missingtokenmissingtokenmissin"))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    private static Wall sampleWall() {
        return sampleWall(Instant.parse("2026-03-01T00:00:00Z"));
    }

    private static Wall sampleWall(Instant createdAt) {
        return new Wall(WALL_ID, "Darkroom", OWNER_ID, "ab".repeat(16), createdAt);
    }
}
