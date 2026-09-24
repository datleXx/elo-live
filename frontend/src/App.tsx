import { Route, Routes } from "react-router-dom";
import { Layout } from "@/components/layout/Layout";
import { DashboardPage } from "@/pages/DashboardPage";
import { RatingsPage } from "@/pages/RatingsPage";
import { TeamsPage } from "@/pages/TeamsPage";
import { TeamDetailPage } from "@/pages/TeamDetailPage";
import { AdminPage } from "@/pages/AdminPage";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<DashboardPage />} />
        <Route path="ratings" element={<RatingsPage />} />
        <Route path="teams" element={<TeamsPage />} />
        <Route path="teams/:id" element={<TeamDetailPage />} />
        <Route path="admin" element={<AdminPage />} />
      </Route>
    </Routes>
  );
}

export default App;
