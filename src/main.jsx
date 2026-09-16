/**
 * React entry point.
 *
 * Mounts <App /> into #root, wrapped in the CafeteriaProvider so every screen
 * shares one store, and pulls in the global Tailwind stylesheet.
 * Exports nothing - this module runs for its side effect.
 */

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { CafeteriaProvider } from './context/CafeteriaContext.jsx';
import './index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <CafeteriaProvider>
      <App />
    </CafeteriaProvider>
  </StrictMode>,
);
