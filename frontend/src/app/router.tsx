import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { GuestRoute, ProtectedRoute } from "../features/auth/AuthRoutes";
import { HomePage } from "../pages/HomePage";
import { LandingPage } from "../pages/LandingPage";
import { LoginPage } from "../pages/LoginPage";
import { RegisterPage } from "../pages/RegisterPage";

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />

        <Route element={<GuestRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="/app" element={<HomePage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
