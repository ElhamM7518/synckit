package com.elham.synckit.wall;

import com.elham.synckit.common.CurrentUser;
import com.elham.synckit.wall.dto.CreateWallRequest;
import com.elham.synckit.wall.dto.JoinWallRequest;
import com.elham.synckit.wall.dto.WallResponse;
import com.elham.synckit.wall.dto.WallSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/walls")
public class WallController {

    private final WallService wallService;

    public WallController(WallService wallService) {
        this.wallService = wallService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WallResponse create(@Valid @RequestBody CreateWallRequest request) {
        return wallService.create(CurrentUser.id(), request);
    }

    @GetMapping
    public List<WallSummaryResponse> list() {
        return wallService.list(CurrentUser.id());
    }

    @GetMapping("/{wallId}")
    public WallResponse get(@PathVariable UUID wallId) {
        return wallService.get(CurrentUser.id(), wallId);
    }

    @PostMapping("/join")
    public WallResponse join(@Valid @RequestBody JoinWallRequest request) {
        return wallService.join(CurrentUser.id(), request);
    }
}
