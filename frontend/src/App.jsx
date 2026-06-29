import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ModuleComingSoon from "./pages/ModuleComingSoon";
import NotFound from "./pages/NotFound";
import OpenApiExplorer from "./pages/OpenApiExplorer";
import OperationalStatus from "./pages/OperationalStatus";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/dashboard" replace />} />

          <Route path="dashboard" element={<Dashboard />} />
          <Route path="researchers" element={<ModuleComingSoon moduleKey="researchers" />} />
          <Route path="academic-profiles" element={<ModuleComingSoon moduleKey="academic-profiles" />} />
          <Route path="orcid" element={<ModuleComingSoon moduleKey="orcid" />} />
          <Route path="openalex" element={<ModuleComingSoon moduleKey="openalex" />} />
          <Route path="manual-review" element={<ModuleComingSoon moduleKey="manual-review" />} />
          <Route path="crossref" element={<ModuleComingSoon moduleKey="crossref" />} />
          <Route path="reports" element={<ModuleComingSoon moduleKey="reports" />} />
          <Route path="google-scholar-checklist" element={<ModuleComingSoon moduleKey="google-scholar" />} />
          <Route path="audit" element={<ModuleComingSoon moduleKey="audit" />} />
          <Route path="status" element={<OperationalStatus />} />
          <Route path="openapi" element={<OpenApiExplorer />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}