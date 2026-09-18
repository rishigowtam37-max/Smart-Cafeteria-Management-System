/**
 * Vite build configuration for the FlowBite web app.
 *
 * Exports the default Vite config object, which registers the React plugin
 * (JSX transform + fast refresh) and the Tailwind CSS v4 plugin (which compiles
 * the utility classes referenced in `src/index.css` at build time).
 *
 * The original Java console app lives in `legacy-java/`, outside `src/`, so
 * nothing Vite scans can collide with it on a case-insensitive filesystem.
 */

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    open: false,
    // Everything under /api goes to the Spring Boot application. Proxying rather
    // than calling http://localhost:8080 directly keeps the browser on one
    // origin, so the JSESSIONID cookie is sent with every request and no CORS
    // configuration is needed on either side.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
