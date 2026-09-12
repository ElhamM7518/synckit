import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import { useAuthStore } from "../features/auth/store";
import { create as createWall, get as getWall, join as joinWall, list as listWalls } from "../features/walls/api";
import type { WallSummaryResponse } from "../features/walls/types";
import { ApiError } from "../lib/http";

const WALLS_QUERY_KEY = ["walls"] as const;

export function HomePage() {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const clearSession = useAuthStore((state) => state.clearSession);

  const [name, setName] = useState("");
  const [shareToken, setShareToken] = useState("");
  const [createError, setCreateError] = useState<string | null>(null);
  const [joinError, setJoinError] = useState<string | null>(null);
  const [inviteByWallId, setInviteByWallId] = useState<Record<string, string>>({});
  const [copiedWallId, setCopiedWallId] = useState<string | null>(null);
  const [inviteError, setInviteError] = useState<string | null>(null);

  const expireSession = useCallback(() => {
    queryClient.clear();
    clearSession();
  }, [queryClient, clearSession]);

  const wallsQuery = useQuery({
    queryKey: [...WALLS_QUERY_KEY, user?.id],
    queryFn: () => listWalls(accessToken ?? ""),
    enabled: Boolean(accessToken && user?.id),
  });

  const createMutation = useMutation({
    mutationFn: (wallName: string) => createWall(accessToken ?? "", { name: wallName }),
    onSuccess: (wall) => {
      setName("");
      setCreateError(null);
      const inviteToken = wall.shareToken;
      if (inviteToken) {
        setInviteByWallId((current) => ({ ...current, [wall.id]: inviteToken }));
      }
      void queryClient.invalidateQueries({ queryKey: WALLS_QUERY_KEY });
    },
    onError: (error: unknown) => {
      if (error instanceof ApiError && error.status === 401) {
        expireSession();
        return;
      }
      setCreateError(messageFromError(error, "Could not create the wall. Is the API running?"));
    },
  });

  const joinMutation = useMutation({
    mutationFn: (token: string) => joinWall(accessToken ?? "", { shareToken: token }),
    onSuccess: () => {
      setShareToken("");
      setJoinError(null);
      void queryClient.invalidateQueries({ queryKey: WALLS_QUERY_KEY });
    },
    onError: (error: unknown) => {
      if (error instanceof ApiError && error.status === 401) {
        expireSession();
        return;
      }
      setJoinError(messageFromError(error, "Could not join that wall."));
    },
  });

  const inviteMutation = useMutation({
    mutationFn: (wallId: string) => getWall(accessToken ?? "", wallId),
    onSuccess: (wall) => {
      setInviteError(null);
      const inviteToken = wall.shareToken;
      if (inviteToken) {
        setInviteByWallId((current) => ({ ...current, [wall.id]: inviteToken }));
      }
    },
    onError: (error: unknown) => {
      if (error instanceof ApiError && error.status === 401) {
        expireSession();
        return;
      }
      setInviteError(messageFromError(error, "Could not load the invite token."));
    },
  });

  useEffect(() => {
    if (wallsQuery.error instanceof ApiError && wallsQuery.error.status === 401) {
      expireSession();
    }
  }, [wallsQuery.error, expireSession]);

  function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const wallName = name.trim();
    if (!wallName) {
      setCreateError("Name is required");
      return;
    }
    setCreateError(null);
    createMutation.mutate(wallName);
  }

  function handleJoin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const token = shareToken.trim().toLowerCase();
    if (!token) {
      setJoinError("Share token is required");
      return;
    }
    setJoinError(null);
    joinMutation.mutate(token);
  }

  async function copyInvite(wallId: string, token: string) {
    try {
      await navigator.clipboard.writeText(token);
      setCopiedWallId(wallId);
    } catch {
      setCopiedWallId(null);
    }
  }

  const walls = wallsQuery.data ?? [];
  const listError =
    wallsQuery.error && !(wallsQuery.error instanceof ApiError && wallsQuery.error.status === 401) ? messageFromError(wallsQuery.error, "Could not load your walls. Is the API running?") : null;

  return (
    <div className="page page--studio">
      <header className="studio-header">
        <div>
          <p className="eyebrow">Atelier</p>
          <h1>Hello, {user?.displayName}</h1>
          <p className="lede">Create a studio wall or join one with a share token.</p>
          <p className="meta">{user?.email}</p>
        </div>
        <button className="button button--ghost" type="button" onClick={expireSession}>
          Sign out
        </button>
      </header>

      <section className="studio-tools">
        <form className="card studio-card" onSubmit={handleCreate} noValidate>
          <p className="eyebrow">New wall</p>
          <label className="field">
            <span>Name</span>
            <input value={name} onChange={(event) => setName(event.target.value)} maxLength={100} required />
          </label>
          {createError ? (
            <p className="form-error" role="alert">
              {createError}
            </p>
          ) : null}
          <button className="button button--primary" type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? "Creating…" : "Create wall"}
          </button>
        </form>

        <form className="card studio-card" onSubmit={handleJoin} noValidate>
          <p className="eyebrow">Join</p>
          <label className="field">
            <span>Share token</span>
            <input value={shareToken} onChange={(event) => setShareToken(event.target.value)} minLength={8} required />
          </label>
          {joinError ? (
            <p className="form-error" role="alert">
              {joinError}
            </p>
          ) : null}
          <button className="button button--primary" type="submit" disabled={joinMutation.isPending}>
            {joinMutation.isPending ? "Joining…" : "Join wall"}
          </button>
        </form>
      </section>

      {listError ? (
        <p className="form-error" role="alert">
          {listError}
        </p>
      ) : null}

      {inviteError ? (
        <p className="form-error" role="alert">
          {inviteError}
        </p>
      ) : null}

      {wallsQuery.isPending ? (
        <p className="meta">Loading walls…</p>
      ) : walls.length === 0 && !listError ? (
        <p className="meta">No walls yet. Create one, or paste a token from someone else.</p>
      ) : (
        <ul className="wall-grid">
          {walls.map((wall) => (
            <WallCover
              key={wall.id}
              wall={wall}
              inviteToken={inviteByWallId[wall.id]}
              invitePending={inviteMutation.isPending && inviteMutation.variables === wall.id}
              copied={copiedWallId === wall.id}
              onRevealInvite={() => inviteMutation.mutate(wall.id)}
              onCopyInvite={(token) => void copyInvite(wall.id, token)}
            />
          ))}
        </ul>
      )}
    </div>
  );
}

type WallCoverProps = {
  wall: WallSummaryResponse;
  inviteToken?: string;
  invitePending: boolean;
  copied: boolean;
  onRevealInvite: () => void;
  onCopyInvite: (token: string) => void;
};

function WallCover({ wall, inviteToken, invitePending, copied, onRevealInvite, onCopyInvite }: WallCoverProps) {
  return (
    <li className="wall-cover">
      <p className="wall-cover__role">{wall.role === "OWNER" ? "Owner" : "Editor"}</p>
      <h2 className="wall-cover__title">{wall.name}</h2>
      <p className="meta">{formatCreatedAt(wall.createdAt)}</p>
      {wall.role === "OWNER" ? (
        inviteToken ? (
          <div className="wall-cover__invite">
            <code className="invite-token">{inviteToken}</code>
            <button className="button button--ghost" type="button" onClick={() => onCopyInvite(inviteToken)}>
              {copied ? "Copied" : "Copy token"}
            </button>
          </div>
        ) : (
          <button className="button button--ghost" type="button" onClick={onRevealInvite} disabled={invitePending}>
            {invitePending ? "Loading…" : "Show invite token"}
          </button>
        )
      ) : null}
    </li>
  );
}

function formatCreatedAt(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return iso;
  }
  return date.toLocaleDateString(undefined, { dateStyle: "medium" });
}

function messageFromError(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return fallback;
}
