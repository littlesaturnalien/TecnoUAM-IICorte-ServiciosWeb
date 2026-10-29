import axios from "axios";

const api = axios.create({ baseURL: "/api" });

// Adjunta el JWT en cada petición
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
