CREATE TABLE walls (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    owner_id        UUID NOT NULL REFERENCES users (id),
    share_token     VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_walls_share_token UNIQUE (share_token)
);

CREATE INDEX idx_walls_owner_id ON walls (owner_id);

CREATE TABLE wall_members (
    wall_id         UUID NOT NULL REFERENCES walls (id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users (id),
    role            VARCHAR(20) NOT NULL,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_wall_members PRIMARY KEY (wall_id, user_id),
    CONSTRAINT ck_wall_members_role CHECK (role IN ('OWNER', 'EDITOR'))
);

CREATE INDEX idx_wall_members_user_id ON wall_members (user_id);
