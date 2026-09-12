import { apiRequest } from "../../lib/http";
import type {
  CreateWallRequest,
  JoinWallRequest,
  WallResponse,
  WallSummaryResponse,
} from "./types";

export function create(token: string, body: CreateWallRequest): Promise<WallResponse> {
  return apiRequest<WallResponse>("/api/walls", {
    method: "POST",
    body,
    token,
  });
}

export function list(token: string): Promise<WallSummaryResponse[]> {
  return apiRequest<WallSummaryResponse[]>("/api/walls", {
    token,
  });
}

export function get(token: string, wallId: string): Promise<WallResponse> {
  return apiRequest<WallResponse>(`/api/walls/${wallId}`, {
    token,
  });
}

export function join(token: string, body: JoinWallRequest): Promise<WallResponse> {
  return apiRequest<WallResponse>("/api/walls/join", {
    method: "POST",
    body,
    token,
  });
}
