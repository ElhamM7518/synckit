import { useAuthStore } from "../features/auth/store";

export function HomePage() {
  const user = useAuthStore((state) => state.user);
  const clearSession = useAuthStore((state) => state.clearSession);

  return (
    <div className="page page--centered">
      <section className="card home-card">
        <p className="eyebrow">Signed in</p>
        <h1>Hello, {user?.displayName}</h1>
        <p className="lede">Your studio walls will show up here.</p>
        <p className="meta">{user?.email}</p>
        <button className="button button--ghost" type="button" onClick={clearSession}>
          Sign out
        </button>
      </section>
    </div>
  );
}
