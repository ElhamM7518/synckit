import { apiRequest } from "../../lib/http";
import type { AuthResponse, LoginRequest, RegisterRequest } from "./types";

export function register(body: RegisterRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/api/auth/register", {
    method: "POST",
    body: body,
  });
}

export function login(body: LoginRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: body,
  });
}
