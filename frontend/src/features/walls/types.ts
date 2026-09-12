export type WallRole = "OWNER" | "EDITOR";

export type CreateWallRequest = {
  name: string;
};

export type WallResponse = {
  id: string;
  name: string;
  ownerId: string;
  role: WallRole;
  shareToken?: string;
  createdAt: string;
};

export type WallSummaryResponse = {
  id: string;
  name: string;
  role: WallRole;
  createdAt: string;
};

export type JoinWallRequest = {
  shareToken: string;
};
