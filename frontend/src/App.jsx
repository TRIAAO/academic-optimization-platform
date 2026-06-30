import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import AcademicProfiles from "./pages/AcademicProfiles";
import Audit from "./pages/Audit";
import Crossref from "./pages/Crossref";
import Dashboard from "./pages/Dashboard";
import GoogleScholarChecklist from "./pages/GoogleScholarChecklist";
import Login from "./pages/Login";
import ManualReview from "./pages/ManualReview";
import NotFound from "./pages/NotFound";
import OpenAlex from "./pages/OpenAlex";
import OpenApiExplorer from "./pages/OpenApiExplorer";
import OperationalStatus from "./pages/OperationalStatus";
import Orcid from "./pages/Orcid";
import Reports from "./pages/Reports";
import ResearcherDetails from "./pages/ResearcherDetails";
import Researchers from "./pages/Researchers";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/dashboard" replace />} />

          <Route path="dashboard" element={<Dashboard />} />

          <Route path="researchers" element={<Researchers />} />
          <Route path="researchers/:id" element={<ResearcherDetails />} />

          <Route path="academic-profiles" element={<AcademicProfiles />} />
          <Route path="orcid" element={<Orcid />} />

          <Route path="openalex" element={<OpenAlex />} />
          <Route path="manual-review" element={<ManualReview />} />
          <Route path="crossref" element={<Crossref />} />

          <Route path="reports" element={<Reports />} />
          <Route
            path="google-scholar-checklist"
            element={<GoogleScholarChecklist />}
          />
          <Route path="audit" element={<Audit />} />

          <Route path="status" element={<OperationalStatus />} />
          <Route path="openapi" element={<OpenApiExplorer />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}