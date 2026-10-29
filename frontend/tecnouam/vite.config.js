import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// El proxy evita problemas de CORS: el navegador llama a /api (puerto 5173)
// y Vite reenvía a Spring Boot (puerto 8087).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { "/api": "http://localhost:8087" },
  },
});
