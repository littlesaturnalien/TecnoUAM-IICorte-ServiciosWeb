import { useState, useEffect } from "react";
import api from "./api";
import "./App.css";

/* ---------- utilidades ---------- */
const mensajeError = (e) => {
  if (!e.response) return "No se pudo conectar con el backend (localhost:8087).";
  const s = e.response.status;
  const d = e.response.data;
  const detalle = typeof d === "string" ? d : d?.message || d?.error || "";
  const base =
    { 400: "Datos inválidos (400)", 401: "No autenticado (401)", 403: "Sin permisos (403)",
      404: "No encontrado (404)", 409: "Conflicto de negocio (409)" }[s] || `Error ${s}`;
  return detalle ? `${base}: ${detalle}` : base;
};

const leerRoles = (token) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return { usuario: payload.sub, admin: JSON.stringify(payload.roles ?? payload.scope ?? "").includes("ADMIN") };
  } catch {
    return { usuario: "?", admin: false };
  }
};

// <input type="date"> da yyyy-MM-dd; la API espera dd-MM-yyyy
const aFechaApi = (iso) => (iso ? iso.split("-").reverse().join("-") : null);

/* ---------- Login ---------- */
function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const enviar = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError("");
    try {
      const { data } = await api.post("/auth/login", { username, password });
      onLogin(data.token);
    } catch (err) {
      setError(err.response?.status === 401 ? "Credenciales incorrectas (401)" : mensajeError(err));
    } finally {
      setCargando(false);
    }
  };

  return (
    <form className="card login" onSubmit={enviar}>
      <h2>TecnoUAM · Iniciar sesión</h2>
      <input placeholder="Usuario" value={username} onChange={(e) => setUsername(e.target.value)} required />
      <input type="password" placeholder="Contraseña" value={password} onChange={(e) => setPassword(e.target.value)} required />
      <button disabled={cargando}>{cargando ? "Ingresando..." : "Ingresar"}</button>
      {error && <p className="msg error">{error}</p>}
    </form>
  );
}

/* ---------- Formulario de equipo (crear / modificar) ---------- */
const equipoVacio = { codigo: "", nombre: "", tipo: "LAPTOP", estado: "DISPONIBLE" };

function EquipoForm({ editando, onGuardado, onCancelar, onMensaje }) {
  const [form, setForm] = useState(equipoVacio);

  useEffect(() => {
    setForm(editando ?? equipoVacio);
  }, [editando]);

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const guardar = async (e) => {
    e.preventDefault();
    try {
      if (editando) await api.put(`/equipos/${editando.id}`, form);
      else await api.post("/equipos", form);
      onMensaje({ ok: true, texto: editando ? "Equipo actualizado (200)" : "Equipo creado (201)" });
      setForm(equipoVacio);
      onGuardado();
    } catch (err) {
      onMensaje({ ok: false, texto: mensajeError(err) });
    }
  };

  return (
    <form className="card form" onSubmit={guardar}>
      <h3>{editando ? `Modificar equipo #${editando.id}` : "Registrar equipo"}</h3>
      <input name="codigo" placeholder="Código patrimonial" value={form.codigo} onChange={cambiar} required />
      <input name="nombre" placeholder="Nombre" value={form.nombre} onChange={cambiar} required />
      <select name="tipo" value={form.tipo} onChange={cambiar}>
        <option>LAPTOP</option>
        <option>PROYECTOR</option>
        <option>TABLETA</option>
      </select>
      <select name="estado" value={form.estado} onChange={cambiar}>
        <option>DISPONIBLE</option>
        <option>PRESTADO</option>
      </select>
      <div className="fila">
        <button>{editando ? "Guardar cambios" : "Crear"}</button>
        {editando && <button type="button" className="sec" onClick={onCancelar}>Cancelar</button>}
      </div>
    </form>
  );
}

/* ---------- Tabla de equipos ---------- */
function EquiposTabla({ equipos, cargando, error, admin, onEditar, onEliminar, onPrestar }) {
  if (cargando) return <p className="msg">Cargando equipos...</p>;
  if (error) return <p className="msg error">{error}</p>;
  if (equipos.length === 0) return <p className="msg">No hay equipos registrados.</p>;

  return (
    <table>
      <thead>
        <tr><th>Código</th><th>Nombre</th><th>Tipo</th><th>Estado</th>{admin && <th>Acciones</th>}</tr>
      </thead>
      <tbody>
        {equipos.map((eq) => (
          <tr key={eq.id}>
            <td>{eq.codigo}</td>
            <td>{eq.nombre}</td>
            <td>{eq.tipo}</td>
            <td><span className={`badge ${eq.estado}`}>{eq.estado}</span></td>
            {admin && (
              <td className="fila">
                <button onClick={() => onEditar(eq)}>Editar</button>
                <button className="sec" disabled={eq.estado !== "DISPONIBLE"} onClick={() => onPrestar(eq.id)}>Prestar</button>
                <button className="peligro" onClick={() => onEliminar(eq)}>Eliminar</button>
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

/* ---------- Formulario de préstamo ---------- */
function PrestamoForm({ equipos, equipoId, onCreado, onMensaje }) {
  const hoy = new Date().toISOString().slice(0, 10);
  const [form, setForm] = useState({ equipoId: "", solicitante: "", fechaPrestamo: hoy, fechaDevolucion: hoy });

  useEffect(() => {
    if (equipoId) setForm((f) => ({ ...f, equipoId }));
  }, [equipoId]);

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const guardar = async (e) => {
    e.preventDefault();
    try {
      await api.post("/prestamos", {
        equipoId: Number(form.equipoId),
        solicitante: form.solicitante,
        fechaPrestamo: aFechaApi(form.fechaPrestamo),
        fechaDevolucion: aFechaApi(form.fechaDevolucion),
        estadoPrestamo: "ACTIVO",
      });
      onMensaje({ ok: true, texto: "Préstamo registrado (201)" });
      setForm({ ...form, solicitante: "" });
      onCreado();
    } catch (err) {
      onMensaje({ ok: false, texto: mensajeError(err) });
    }
  };

  return (
    <form className="card form" onSubmit={guardar}>
      <h3>Registrar préstamo</h3>
      <select name="equipoId" value={form.equipoId} onChange={cambiar} required>
        <option value="">-- Equipo --</option>
        {equipos.map((eq) => (
          <option key={eq.id} value={eq.id}>{eq.codigo} · {eq.nombre} ({eq.estado})</option>
        ))}
      </select>
      <input name="solicitante" placeholder="Nombre del solicitante" value={form.solicitante} onChange={cambiar} required />
      <label>Fecha de préstamo<input type="date" name="fechaPrestamo" value={form.fechaPrestamo} onChange={cambiar} required /></label>
      <label>Devolución prevista<input type="date" name="fechaDevolucion" value={form.fechaDevolucion} onChange={cambiar} required /></label>
      <button>Prestar</button>
    </form>
  );
}

/* ---------- Tabla de préstamos ---------- */
function PrestamosTabla({ prestamos, equipos, cargando, error, admin, onDevolver }) {
  if (cargando) return <p className="msg">Cargando préstamos...</p>;
  if (error) return <p className="msg error">{error}</p>;
  if (prestamos.length === 0) return <p className="msg">No hay préstamos registrados.</p>;

  const nombreEquipo = (id) => {
    const eq = equipos.find((e) => e.id === id);
    return eq ? `${eq.codigo} · ${eq.nombre}` : `Equipo #${id}`;
  };

  return (
    <table>
      <thead>
        <tr><th>ID</th><th>Equipo</th><th>Solicitante</th><th>Préstamo</th><th>Devolución prevista</th><th>Estado</th>{admin && <th></th>}</tr>
      </thead>
      <tbody>
        {prestamos.map((p) => (
          <tr key={p.id}>
            <td>{p.id}</td>
            <td>{nombreEquipo(p.equipoId)}</td>
            <td>{p.solicitante}</td>
            <td>{p.fechaPrestamo}</td>
            <td>{p.fechaDevolucion}</td>
            <td><span className={`badge ${p.estadoPrestamo}`}>{p.estadoPrestamo}</span></td>
            {admin && (
              <td>
                <button disabled={p.estadoPrestamo !== "ACTIVO"} onClick={() => onDevolver(p.id)}>Devolver</button>
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

/* ---------- Panel principal ---------- */
function Panel({ token, onLogout }) {
  const { usuario, admin } = leerRoles(token);

  const [equipos, setEquipos] = useState([]);
  const [prestamos, setPrestamos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorEquipos, setErrorEquipos] = useState("");
  const [errorPrestamos, setErrorPrestamos] = useState("");
  const [recarga, setRecarga] = useState(0);
  const [editando, setEditando] = useState(null);
  const [equipoAPrestar, setEquipoAPrestar] = useState("");
  const [mensaje, setMensaje] = useState(null);

  // Consulta real a la API (GET protegido) al montar y cada vez que cambia "recarga"
  useEffect(() => {
    let activo = true;
    setCargando(true);

    api.get("/equipos")
      .then((r) => { if (activo) { setEquipos(r.data); setErrorEquipos(""); } })
      .catch((e) => {
        if (!activo) return;
        if (e.response?.status === 401) onLogout();
        setErrorEquipos(mensajeError(e));
      });

    api.get("/prestamos")
      .then((r) => { if (activo) { setPrestamos(r.data); setErrorPrestamos(""); } })
      .catch((e) => { if (activo) setErrorPrestamos(mensajeError(e)); })
      .finally(() => { if (activo) setCargando(false); });

    return () => { activo = false; };
  }, [recarga]);

  const recargar = () => setRecarga((n) => n + 1);

  const eliminar = async (eq) => {
    if (!window.confirm(`¿Eliminar "${eq.nombre}"?`)) return;
    try {
      await api.delete(`/equipos/${eq.id}`);
      setMensaje({ ok: true, texto: "Equipo eliminado (204)" });
      recargar();
    } catch (e) {
      setMensaje({ ok: false, texto: mensajeError(e) });
    }
  };

  const devolver = async (id) => {
    try {
      await api.put(`/prestamos/${id}/devolver`);
      setMensaje({ ok: true, texto: "Devolución registrada, equipo DISPONIBLE (200)" });
      recargar();
    } catch (e) {
      setMensaje({ ok: false, texto: mensajeError(e) });
    }
  };

  return (
    <>
      <header>
        <h1>TecnoUAM · Préstamo de equipos</h1>
        <div className="fila">
          <span>{usuario} · {admin ? "ADMIN" : "USER"}</span>
          <button className="sec" onClick={recargar}>Recargar</button>
          <button className="sec" onClick={onLogout}>Salir</button>
        </div>
      </header>

      {mensaje && (
        <p className={`msg ${mensaje.ok ? "ok" : "error"}`} onClick={() => setMensaje(null)}>{mensaje.texto}</p>
      )}

      <section>
        <h2>Equipos</h2>
        <EquiposTabla
          equipos={equipos} cargando={cargando} error={errorEquipos} admin={admin}
          onEditar={setEditando}
          onEliminar={eliminar}
          onPrestar={setEquipoAPrestar}
        />
      </section>

      <section>
        <h2>Préstamos</h2>
        <PrestamosTabla
          prestamos={prestamos} equipos={equipos} cargando={cargando}
          error={errorPrestamos} admin={admin} onDevolver={devolver}
        />
      </section>

      {admin && (
        <section className="grid">
          <EquipoForm
            editando={editando}
            onGuardado={() => { setEditando(null); recargar(); }}
            onCancelar={() => setEditando(null)}
            onMensaje={setMensaje}
          />
          <PrestamoForm
            equipos={equipos} equipoId={equipoAPrestar}
            onCreado={recargar} onMensaje={setMensaje}
          />
        </section>
      )}
    </>
  );
}

/* ---------- App ---------- */
export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem("token"));

  const iniciarSesion = (t) => { sessionStorage.setItem("token", t); setToken(t); };
  const cerrarSesion = () => { sessionStorage.removeItem("token"); setToken(null); };

  return <div className="app">{token ? <Panel token={token} onLogout={cerrarSesion} /> : <Login onLogin={iniciarSesion} />}</div>;
}
