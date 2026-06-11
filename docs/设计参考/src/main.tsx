import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import PublicLayout from './components/PublicLayout';
import AdminLayout from './components/AdminLayout';
import Home from './pages/Home';
import Courses from './pages/Courses';
import Admin from './pages/Admin';

import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PublicLayout />}>
          <Route index element={<Home />} />
          <Route path="courses" element={<Courses />} />
        </Route>
        
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Admin />} />
        </Route>
        
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);
