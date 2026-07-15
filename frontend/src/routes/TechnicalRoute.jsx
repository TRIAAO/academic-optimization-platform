import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { canViewTechnicalArea } from "../config/permissions";

export default function TechnicalRoute() {
  const { user } = useAuth();

  if (!canViewTechnicalArea(user)) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return <Outlet />;
}