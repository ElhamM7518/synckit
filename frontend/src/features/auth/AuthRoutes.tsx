import { Navigate, Outlet, useLocation } from "react-router-dom";
import { selectIsAuthenticated, useAuthStore } from "./store";

export function ProtectedRoute() {
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export function GuestRoute() {
  const isAuthenticated = useAuthStore(selectIsAuthenticated);

  if (isAuthenticated) {
    return <Navigate to="/app" replace />;
  }

  return <Outlet />;
}
