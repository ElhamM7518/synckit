import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { User } from "./types";

type AuthState = {
  accessToken: string | null;
  user: User | null;
  setSession: (accessToken: string, user: User) => void;
  clearSession: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      user: null,
      setSession: (accessToken, user) => set({ accessToken, user }),
      clearSession: () => set({ accessToken: null, user: null }),
    }),
    {
      name: "atelier-auth",
    },
  ),
);

export function selectIsAuthenticated(state: AuthState): boolean {
  return Boolean(state.accessToken && state.user);
}
