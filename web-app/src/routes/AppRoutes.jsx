import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Registration from "../pages/Registration";

const AppRoutes = () => {
  return (
    <Router>
      <Routes>
        <Route path="/registration" element={<Registration />} />
        <Route path="/" element={<Registration />} />
      </Routes>
    </Router>
  );
};

export default AppRoutes;
