[README.md](https://github.com/user-attachments/files/34113201/README.md)
# TecnoUAM – Sistema de préstamo de equipos tecnológicos

Aplicación cliente-servidor: API REST con **Spring Boot + Spring Data JPA + PostgreSQL**, seguridad con **Spring Security + JWT** e interfaz **React (Vite + Axios)**.

| Componente | Tecnología | URL |
|---|---|---|
| Backend | Spring Boot | http://localhost:8087 |
| Base de datos | PostgreSQL (`tecnouam`) | localhost:5432 |
| Frontend | React + Vite | http://localhost:5173 |

---

## 1. Requisitos

- Java 17 o superior (JDK)
- Maven (o el wrapper `mvnw` del proyecto)
- PostgreSQL 14 o superior
- Node.js 18 o superior y npm

---

## 2. Base de datos (PostgreSQL)

Cree la base de datos (con `psql` o pgAdmin):

```sql
CREATE DATABASE tecnouam;
```

Las tablas (`equipo`, `prestamo`) se crean automáticamente al iniciar el backend gracias a `spring.jpa.hibernate.ddl-auto=update`.

---

## 3. Backend (Spring Boot)

### 3.1 Dependencia del driver de PostgreSQL

Verifique que el `pom.xml` incluya:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 3.2 `application.properties`

Ubicación: `src/main/resources/application.properties`

```properties
spring.application.name=tecnouam

# Puerto de la API
server.port=8087

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/tecnouam
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

> Si su usuario o contraseña de PostgreSQL son distintos, defina las variables `DB_USER` y `DB_PASSWORD` (o cambie los valores por defecto).

### 3.3 Variables de entorno

| Variable | Descripción | Obligatoria |
|---|---|---|
| `JWT_SECRET_BASE64` | Clave HS256 en Base64 (mínimo 32 bytes). Sin ella la aplicación no arranca. | Sí |
| `DB_USER` | Usuario de PostgreSQL (por defecto `postgres`) | No |
| `DB_PASSWORD` | Contraseña de PostgreSQL (por defecto `postgres`) | No |

Valor de `JWT_SECRET_BASE64` para este entorno de pruebas:

```
JWT_SECRET_BASE64=c2VjcmV0S2V5Rm9ySldUVGVjbm9VQU1BcHBsaWNhdGlvbldpdGhNaW5pbXVtMzJCeXRlc0xvbmc=
```

### 3.4 Ejecución

**Desde IntelliJ IDEA**

1. `Run > Edit Configurations...` y seleccione la aplicación Spring Boot.
2. En *Environment variables* agregue `JWT_SECRET_BASE64=<valor anterior>`.
3. Ejecute la clase principal.

**Desde terminal**

Windows (PowerShell):

```powershell
$env:JWT_SECRET_BASE64="c2VjcmV0S2V5Rm9ySldUVGVjbm9VQU1BcHBsaWNhdGlvbldpdGhNaW5pbXVtMzJCeXRlc0xvbmc="
./mvnw spring-boot:run
```

Linux / macOS:

```bash
export JWT_SECRET_BASE64="c2VjcmV0S2V5Rm9ySldUVGVjbm9VQU1BcHBsaWNhdGlvbldpdGhNaW5pbXVtMzJCeXRlc0xvbmc="
./mvnw spring-boot:run
```

Si el proyecto usa Gradle: `./gradlew bootRun`.

El backend queda disponible en **http://localhost:8087**.

---

## 4. Cuentas de prueba

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `Admin123!` | ADMIN | Consultar, crear, modificar y eliminar equipos; registrar préstamos y devoluciones |
| `usuario` | `Usuario123!` | USER | Solo consultar equipos y préstamos |

---

## 5. Endpoints

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| POST | `/api/auth/login` | Autenticación, devuelve el JWT | Público |
| GET | `/api/equipos` | Listar equipos | USER, ADMIN |
| GET | `/api/equipos/{id}` | Consultar un equipo | USER, ADMIN |
| POST | `/api/equipos` | Registrar un equipo (201) | ADMIN |
| PUT | `/api/equipos/{id}` | Modificar un equipo | ADMIN |
| DELETE | `/api/equipos/{id}` | Eliminar (solo sin préstamos activos, 204) | ADMIN |
| GET | `/api/prestamos` | Listar préstamos | USER, ADMIN |
| GET | `/api/prestamos/{id}` | Consultar un préstamo | USER, ADMIN |
| POST | `/api/prestamos` | Registrar préstamo (201) | ADMIN |
| PUT | `/api/prestamos/{id}/devolver` | Registrar devolución | ADMIN |

### Ejemplo de login

```http
POST http://localhost:8087/api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Admin123!" }
```

Respuesta:

```json
{ "token": "eyJhbGciOi...", "type": "Bearer" }
```

Use el token en las demás peticiones con el encabezado `Authorization: Bearer <token>`.

### Ejemplo de equipo y préstamo

```json
// POST /api/equipos
{ "codigo": "PAT-001", "nombre": "Laptop Dell", "tipo": "LAPTOP", "estado": "DISPONIBLE" }

// POST /api/prestamos  (las fechas usan el formato dd-MM-yyyy)
{ "equipoId": 1, "solicitante": "Ana Pérez", "fechaPrestamo": "28-10-2026",
  "fechaDevolucion": "30-10-2026", "estadoPrestamo": "ACTIVO" }
```

---

## 6. Frontend (React)

Con el backend ya en ejecución, en otra terminal, dentro de la carpeta del frontend:

```bash
npm install
npm run dev
```

Abra **http://localhost:5173** e inicie sesión con alguna de las cuentas de prueba.

- El servidor de desarrollo (Vite) redirige las peticiones `/api` hacia `http://localhost:8087` mediante un proxy configurado en `vite.config.js`, por lo que no se requiere configurar CORS en el backend.
- Si `npm install` no instala Axios, ejecute `npm install axios`.
- Para ver cambios hechos desde Postman (crear o prestar equipos), use el botón **Recargar** o actualice la página.

---

## 7. Orden de arranque

1. Iniciar PostgreSQL y verificar que existe la base `tecnouam`.
2. Iniciar el backend (puerto 8087) con `JWT_SECRET_BASE64` definida.
3. Iniciar el frontend con `npm run dev` (puerto 5173).
4. (Opcional) Importar la colección de Postman para las pruebas de CRUD, préstamo, devolución, 401 y 403.

---

## 8. Solución de problemas

| Síntoma | Causa probable |
|---|---|
| `Configure JWT_SECRET_BASE64 en IntelliJ` al arrancar | Falta la variable de entorno |
| `password authentication failed` | Usuario o contraseña de PostgreSQL incorrectos (`DB_USER` / `DB_PASSWORD`) |
| `database "tecnouam" does not exist` | No se ejecutó `CREATE DATABASE tecnouam;` |
| `Port 8087 was already in use` | Otro proceso usa el puerto; ciérrelo o cambie `server.port` |
| El frontend muestra "No se pudo conectar con el backend" | El backend no está iniciado o no corre en el puerto 8087 |
| 401 en las peticiones | Token ausente, vencido o inválido; vuelva a iniciar sesión |
| 403 en las peticiones | El usuario tiene rol USER y la operación es solo para ADMIN |
