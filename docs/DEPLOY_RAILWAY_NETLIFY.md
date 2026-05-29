# Guía de Deploy DUNAB — Railway (Backend + DB) + Netlify (Frontend)

---

## Resumen

| Qué | Dónde | Gratis |
|-----|-------|--------|
| Base de datos PostgreSQL | Railway | ✅ (500 hs/mes) |
| Backend Spring Boot | Railway | ✅ (500 hs/mes) |
| Frontend HTML/CSS/JS | Netlify | ✅ (ilimitado) |

---

## PARTE 1 — Backend + DB en Railway

### 1.1 Crear cuenta y proyecto

1. Ve a [railway.app](https://railway.app) y crea una cuenta (puedes entrar con GitHub).
2. Clic en **"New Project"**.

---

### 1.2 Crear la base de datos PostgreSQL

1. Dentro del proyecto, clic en **"Add a service"** → **"Database"** → **"PostgreSQL"**.
2. Railway crea la DB automáticamente. Haz clic en el servicio de Postgres y ve a la pestaña **"Variables"**.
3. Anota (o copia) estas variables — las necesitarás luego:
   - `PGHOST`
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`
   - `DATABASE_URL` (Railway también la provee en formato `jdbc:` a veces como `JDBC_DATABASE_URL`)

---

### 1.3 Preparar el backend para producción

#### A) Crear `application-prod.properties`

Crea el archivo `backend/src/main/resources/application-prod.properties`:

```properties
# ── PostgreSQL (Railway inyecta estas variables) ──
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA ──────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

# ── Flyway ───────────────────────────────────────
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# ── JWT (Railway inyecta DUNAB_JWT_SECRET) ───────
dunab.jwt.secret=${DUNAB_JWT_SECRET}
dunab.jwt.expiration-ms=86400000

# ── CORS (reemplaza con tu URL de Netlify) ───────
dunab.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# ── Puerto (Railway lo asigna automáticamente) ───
server.port=${PORT:8080}
server.servlet.context-path=/api
```

> **Nota sobre `DATABASE_URL`:** Railway provee la URL en formato `postgresql://user:pass@host:port/db`.
> Spring Boot necesita el prefijo `jdbc:`. Agrega este bean en tu clase principal o en una clase de config:

```java
// En application-prod.properties usa esto en lugar de ${DATABASE_URL}:
// spring.datasource.url=${JDBC_DATABASE_URL}
// Railway también expone JDBC_DATABASE_URL con el prefijo correcto.
```

Cambia la línea en `application-prod.properties` a:
```properties
spring.datasource.url=${JDBC_DATABASE_URL}
```

#### B) Verificar que `mvnw` esté ejecutable (Windows → Linux)

Railway corre en Linux. El archivo `mvnw` puede necesitar permisos. En tu repo, ejecuta una vez:

```bash
git update-index --chmod=+x backend/mvnw
git commit -m "fix: make mvnw executable for Railway"
```

---

### 1.4 Subir el backend a Railway

**Opción A — Desde GitHub (recomendada):**

1. Sube tu proyecto a un repo en GitHub si no lo tienes.
2. En Railway, clic en **"Add a service"** → **"GitHub Repo"**.
3. Selecciona tu repo. Railway detecta Spring Boot automáticamente.
4. En la configuración del servicio, ve a **"Settings"** → **"Root Directory"** y escribe `backend` (porque el `pom.xml` está en `backend/`, no en la raíz).
5. En **"Deploy"** → **"Start Command"**, déjalo en blanco (Railway usa el Dockerfile o Buildpacks automáticamente).

**Opción B — Desde un JAR:**

1. En local: `cd backend && ./mvnw clean package -DskipTests`
2. Esto genera `backend/target/dunab-1.0.0.jar`.
3. En Railway puedes subir el JAR directamente con Railway CLI (ver paso 1.6).

---

### 1.5 Configurar variables de entorno en Railway

En el servicio del **backend**, ve a **"Variables"** y agrega:

| Variable | Valor |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JDBC_DATABASE_URL` | (copia el valor de la DB, formato `jdbc:postgresql://...`) |
| `PGUSER` | (usuario de la DB) |
| `PGPASSWORD` | (contraseña de la DB) |
| `DUNAB_JWT_SECRET` | (genera uno nuevo, mínimo 32 chars en base64) |
| `CORS_ALLOWED_ORIGINS` | `https://TU-SITIO.netlify.app` (lo completas después) |
| `PORT` | `8080` |

> **Para generar un JWT secret seguro:**
> ```bash
> openssl rand -base64 32
> ```

Railway también puede enlazar directamente las variables de la DB: en el servicio backend, clic en **"Variables"** → **"Add Reference"** → selecciona el servicio Postgres → elige `JDBC_DATABASE_URL`, `PGUSER`, `PGPASSWORD`. Así se sincronizan automáticamente.

---

### 1.6 (Opcional) Railway CLI

```bash
npm install -g @railway/cli
railway login
railway link        # enlaza tu proyecto local
railway up          # sube y despliega
```

---

### 1.7 Verificar el backend

Una vez desplegado, Railway te da una URL tipo:
```
https://dunab-backend-production.up.railway.app
```

Prueba que funcione:
```
https://dunab-backend-production.up.railway.app/api/swagger-ui.html
```

---

## PARTE 2 — Frontend en Netlify

### 2.1 Cambiar la URL de la API en el frontend

Abre `frontend/js/api.js` y cambia:

```js
// ANTES (desarrollo local)
const API_BASE = 'http://localhost:8080/api';

// DESPUÉS (producción)
const API_BASE = 'https://dunab-backend-production.up.railway.app/api';
```

> Reemplaza la URL con la que te dio Railway en el paso 1.7.

---

### 2.2 Subir el frontend a Netlify

**Opción A — Arrastrar carpeta (más fácil):**

1. Ve a [netlify.com](https://netlify.com) y crea una cuenta.
2. En el dashboard, busca el recuadro **"Deploy manually"** o **"Want to deploy a new site without connecting to Git?"**.
3. Arrastra toda la carpeta `frontend/` al área indicada.
4. Netlify despliega en segundos y te da una URL tipo `https://random-name.netlify.app`.

**Opción B — Desde GitHub:**

1. En Netlify, clic en **"Add new site"** → **"Import an existing project"**.
2. Conecta tu repo de GitHub.
3. En **"Base directory"** escribe `frontend`.
4. **Build command**: déjalo vacío (no hay build, es HTML estático).
5. **Publish directory**: `frontend`.
6. Clic en **"Deploy site"**.

---

### 2.3 Personalizar el dominio (opcional)

En Netlify, **"Site settings"** → **"Domain management"** → **"Options"** → **"Edit site name"**. Puedes cambiar el subdominio a algo como `dunab.netlify.app`.

---

### 2.4 Actualizar CORS en Railway

Con tu URL de Netlify ya definida, vuelve a Railway y actualiza la variable:

```
CORS_ALLOWED_ORIGINS = https://dunab.netlify.app
```

Redespliega el backend (Railway lo hace automático al guardar la variable).

---

## PARTE 3 — Checklist final

- [ ] `application-prod.properties` creado en el backend
- [ ] `mvnw` con permisos de ejecución en git
- [ ] Variable `SPRING_PROFILES_ACTIVE=prod` en Railway
- [ ] Variables de DB enlazadas en Railway (`JDBC_DATABASE_URL`, `PGUSER`, `PGPASSWORD`)
- [ ] Variable `DUNAB_JWT_SECRET` con valor seguro en Railway
- [ ] `api.js` apunta a la URL de Railway (no `localhost`)
- [ ] Variable `CORS_ALLOWED_ORIGINS` en Railway apunta a la URL de Netlify
- [ ] Backend responde en `/api/swagger-ui.html`
- [ ] Login funciona desde Netlify

---

## Flujo completo resumido

```
GitHub repo
    ├── backend/   ──→  Railway (Spring Boot)
    │                       ├── PostgreSQL (Railway DB)
    │                       └── Variables de entorno
    └── frontend/  ──→  Netlify (HTML estático)
                            └── api.js apunta a Railway URL
```

---

## Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `CORS blocked` | `CORS_ALLOWED_ORIGINS` no incluye la URL de Netlify | Actualizar variable en Railway |
| `Connection refused` en DB | `JDBC_DATABASE_URL` mal copiada | Usar la referencia directa de Railway |
| `Failed to validate schema` (Flyway) | DB vacía sin baseline | `spring.flyway.baseline-on-migrate=true` ya está activado |
| Frontend muestra datos de localhost | `api.js` no actualizado | Cambiar `API_BASE` a la URL de Railway |
| `mvnw: Permission denied` | Archivo no ejecutable en Linux | `git update-index --chmod=+x backend/mvnw` |
