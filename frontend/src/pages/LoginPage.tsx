import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { login } from "../features/auth/api";
import { postLoginPath } from "../features/auth/redirect";
import { useAuthStore } from "../features/auth/store";
import { ApiError } from "../lib/http";

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((state) => state.setSession);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setSession(data.accessToken, data.user);
      navigate(postLoginPath(location.state), { replace: true });
    },
    onError: (error: unknown) => {
      if (error instanceof ApiError) {
        setFormError(error.message);
        return;
      }
      setFormError("Could not sign in. Is the API running?");
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);
    mutation.mutate({ email: email.trim(), password });
  }

  return (
    <div className="page page--centered">
      <form className="auth-form card" onSubmit={handleSubmit} noValidate>
        <p className="eyebrow">Welcome back</p>
        <h1>Sign in</h1>
        <p className="lede">Use the account you registered with SyncKit.</p>

        <label className="field">
          <span>Email</span>
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={8}
            required
          />
        </label>

        {formError ? <p className="form-error" role="alert">{formError}</p> : null}

        <button className="button button--primary" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Signing in…" : "Sign in"}
        </button>

        <p className="footer-note">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </form>
    </div>
  );
}
