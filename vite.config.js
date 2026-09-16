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
  },
});
