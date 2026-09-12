import { Link } from "react-router-dom";

export function LandingPage() {
  return (
    <div className="page page--centered">
      <section className="hero card">
        <p className="eyebrow">SyncKit · Atelier</p>
        <h1>A studio wall that works offline.</h1>
        <p className="lede">
          Collect notes, polaroids, and color chips on a shared board. Sync when you reconnect —
          without losing anyone&apos;s edits.
        </p>
        <div className="actions">
          <Link className="button button--primary" to="/register">
            Create account
          </Link>
          <Link className="button button--ghost" to="/login">
            Sign in
          </Link>
        </div>
      </section>
    </div>
  );
}
