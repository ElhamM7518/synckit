import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../features/auth/api";
import { useAuthStore } from "../features/auth/store";
import { ApiError } from "../lib/http";

export function RegisterPage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const setSession = useAuthStore((state) => state.setSession);

  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (data) => {
      queryClient.clear();
      setSession(data.accessToken, data.user);
      navigate("/app", { replace: true });
    },
    onError: (error: unknown) => {
      if (error instanceof ApiError) {
        if (error.fields) {
          const firstFieldError = Object.values(error.fields)[0];
          setFormError(firstFieldError ?? error.message);
          return;
        }
        setFormError(error.message);
        return;
      }
      setFormError("Could not create account. Is the API running?");
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);
    mutation.mutate({
      displayName: displayName.trim(),
      email: email.trim(),
      password,
    });
  }

  return (
    <div className="page page--centered">
      <form className="auth-form card" onSubmit={handleSubmit} noValidate>
        <p className="eyebrow">Join Atelier</p>
        <h1>Create account</h1>
        <p className="lede">Password must be at least 8 characters.</p>

        <label className="field">
          <span>Display name</span>
          <input
            type="text"
            autoComplete="nickname"
            value={displayName}
            onChange={(event) => setDisplayName(event.target.value)}
            maxLength={100}
            required
          />
        </label>

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
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={8}
            required
          />
        </label>

        {formError ? <p className="form-error" role="alert">{formError}</p> : null}

        <button className="button button--primary" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Creating…" : "Create account"}
        </button>

        <p className="footer-note">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
