export function postLoginPath(state: unknown): string {
  if (
    typeof state === "object" &&
    state !== null &&
    "from" in state &&
    typeof state.from === "string" &&
    state.from.startsWith("/") &&
    !state.from.startsWith("//")
  ) {
    return state.from;
  }
  return "/app";
}
