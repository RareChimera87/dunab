# PROMPT — Reestructuración profesional del proyecto DUNAB

> Copia y pega este documento completo a otro modelo de Anthropic (Claude) para
> que ejecute la reestructuración del proyecto DUNAB sin errores de referencia.
> Ejecuta las fases EN ORDEN y respeta las reglas R1–R8.

---

## 1. ROL Y MODO DE TRABAJO

Actúa como arquitecto Full-Stack senior con experiencia en Spring Boot, Maven y proyectos web académicos. Vas a reorganizar el proyecto DUNAB (Gestión de Dinero UNAB) sin romper ninguna funcionalidad existente.

Reglas operativas:

- Antes de mover o renombrar cualquier archivo, lista su ubicación actual y la destino para confirmar el cambio.
- Ejecuta las fases en el ORDEN indicado. No saltes fases.
- Después de CADA fase, corre el bloque "Verificación de la fase" que se detalla al final.
- Si encuentras una ambigüedad o un archivo no listado, DETENTE y reporta antes de actuar.
- Trabaja sobre una rama Git nueva llamada `refactor/estructura-profesional`.

---

## 2. CONTEXTO DEL PROYECTO

Stack:

- **Backend**: Spring Boot 3.2.5, Java 17, Maven, paquete `com.unab.dunab`. Spring Security + JWT (JJWT 0.12.5), Spring Data JPA + Hibernate, PostgreSQL (driver runtime), Flyway para migraciones, MapStruct + Lombok, Springdoc OpenAPI. API bajo `/api`, puerto `:8080`.
- **Frontend**: HTML/CSS/JS vanilla, single-file por página (sin framework). 14 páginas `.html`. Scripts compartidos en `js/` (`api.js`, `auth.js`, `theme.js`) y tema en `css/dunab-theme.css`. Imágenes PNG en raíz.
- **Infra**: `docker-compose.yml` con Postgres 16-alpine.
- **Scripts Windows**: `iniciar.bat`, `detener.bat`, `abrir-frontend.bat`, `instalar.ps1`.

Ubicación raíz del proyecto en el equipo del usuario:

```
C:\Users\santi\OneDrive\Documents\Claude\Projects\DUNAB - Gestión de Dinero UNAB
```

Login inicial seed: `admin@unab.edu.co` / `Admin1234!` (rol SUPERADMIN).

---

## 3. ESTRUCTURA ACTUAL (LO QUE EXISTE HOY)

```
DUNAB - Gestión de Dinero UNAB/
├── login.html, registro.html
├── dashboard.html, encuentros.html, feed.html, historial.html,
│   perfil.html, ranking.html, tienda.html, transferencias.html
├── gestionar-admins.html, gestionar-encuentros.html,
│   gestionar-estudiantes.html, gestionar-recompensas.html
├── js/  (api.js, auth.js, theme.js)
├── css/ (dunab-theme.css)
├── LogoUnabRojo.png
├── TemplateDashEstructura.png
├── iniciar.bat, detener.bat, abrir-frontend.bat, instalar.ps1
├── docker-compose.yml
└── dunab-backend/
    ├── pom.xml, mvnw, mvnw.cmd, .mvn/
    └── src/main/
        ├── java/com/unab/dunab/
        │   ├── DunabApplication.java
        │   ├── config/        (SecurityConfig.java)
        │   ├── controller/    (Auth, Usuario, Encuentro, Inscripcion,
        │   │                   Transaccion, Transferencia, Ranking,
        │   │                   Feed, Recompensa)
        │   ├── dto/request/   (7 archivos)
        │   ├── dto/response/  (8 archivos)
        │   ├── exception/     (DunabException, GlobalExceptionHandler)
        │   ├── model/         (entidades JPA + enums mezclados:
        │   │                   Usuario, Encuentro, Inscripcion, Transaccion,
        │   │                   Transferencia, Logro, UsuarioLogro,
        │   │                   FeedEvent, Recompensa, Canje,
        │   │                   Rol, EstadoEncuentro, TipoTransaccion,
        │   │                   FeedTipo)
        │   ├── repository/    (9 archivos)
        │   ├── security/      (JwtFilter, JwtUtil, UserDetailsServiceImpl)
        │   └── service/       (9 archivos)
        └── resources/
            ├── application.properties
            ├── application-dev.properties
            └── db/migration/V1..V7
```

---

## 4. ESTRUCTURA DESTINO (LO QUE DEBES DEJAR AL TERMINAR)

```
DUNAB - Gestión de Dinero UNAB/
├── README.md
├── .gitignore
├── docs/
│   └── arquitectura.md          (lo creas con un esquema de carpetas)
├── infra/
│   └── docker-compose.yml
├── scripts/
│   ├── iniciar.bat
│   ├── detener.bat
│   ├── abrir-frontend.bat
│   └── instalar.ps1
├── backend/                     (antes dunab-backend/)
│   ├── pom.xml, mvnw, mvnw.cmd, .mvn/
│   └── src/main/
│       ├── java/com/unab/dunab/
│       │   ├── DunabApplication.java
│       │   ├── config/          (SecurityConfig.java)
│       │   ├── security/        (JwtFilter, JwtUtil, UserDetailsServiceImpl)
│       │   ├── common/
│       │   │   └── exception/   (DunabException, GlobalExceptionHandler)
│       │   ├── domain/
│       │   │   ├── entity/      (Usuario, Encuentro, Inscripcion,
│       │   │   │                 Transaccion, Transferencia, Logro,
│       │   │   │                 UsuarioLogro, FeedEvent, Recompensa,
│       │   │   │                 Canje)
│       │   │   └── enums/       (Rol, EstadoEncuentro, TipoTransaccion,
│       │   │                     FeedTipo)
│       │   ├── repository/      (igual que antes)
│       │   ├── mapper/          (vacío por ahora si no había mappers
│       │   │                     extraídos; crear .gitkeep)
│       │   ├── service/         (igual que antes)
│       │   ├── dto/request/     (igual que antes)
│       │   ├── dto/response/    (igual que antes)
│       │   └── controller/      (igual que antes)
│       └── resources/
│           ├── application.properties
│           ├── application-dev.properties
│           └── db/migration/V1..V7   ← SIN TOCAR
└── frontend/
    ├── login.html
    ├── registro.html
    ├── dashboard.html, encuentros.html, feed.html, historial.html,
    │   perfil.html, ranking.html, tienda.html, transferencias.html
    ├── gestionar-admins.html, gestionar-encuentros.html,
    │   gestionar-estudiantes.html, gestionar-recompensas.html
    ├── assets/
    │   └── img/
    │       ├── LogoUnabRojo.png
    │       └── TemplateDashEstructura.png
    ├── css/
    │   └── dunab-theme.css
    └── js/
        ├── api.js
        ├── auth.js
        └── theme.js
```

**NOTA CRÍTICA**: dentro de `frontend/`, todos los `.html` quedan en el MISMO nivel (sin subcarpetas `auth/`, `student/`, `admin/`). Esto es intencional para no romper `resolveLoginPath()` en `api.js`, que asume que `login.html` está en el mismo directorio que la página actual.

---

## 5. REGLAS DE ORO (NUNCA ROMPER)

- **R1.** NO modifiques los archivos `.sql` de Flyway en `db/migration/`.
- **R2.** NO cambies nombres de tabla ni de columna en la base de datos.
- **R3.** NO cambies las rutas REST (`/auth/...`, `/usuarios/...`, etc.).
- **R4.** NO cambies las claves de localStorage `dunab_token` ni `dunab_user`.
- **R5.** NO toques `application.properties` salvo lo indicado en la Fase 6.
- **R6.** NO subdividas los `.html` por rol (todos planos dentro de `frontend/`).
- **R7.** NO uses rutas absolutas en HTML/CSS/JS; siempre relativas.
- **R8.** ANTES de cualquier `mv` o rename, haz `git status` y `git add -A && git commit -m "checkpoint pre-fase-N"` para tener punto de retorno.

---

## 6. PLAN DE EJECUCIÓN (FASES EN ORDEN)

### FASE 0 — Preparación

1. Confirma que estás en la raíz del proyecto.
2. Si no hay repo Git, ejecuta `git init && git add -A && git commit -m "snapshot pre-refactor"`.
3. Crea y muévete a la rama: `git checkout -b refactor/estructura-profesional`.
4. Lista los archivos en la raíz y guarda la lista; debe coincidir con la sección 3 de este prompt. Si falta o sobra algo, REPORTA y detente.

### FASE 1 — Crear carpetas de alto nivel

1. Crea (vacías) en la raíz: `docs/`, `infra/`, `scripts/`, `frontend/`, `frontend/assets/`, `frontend/assets/img/`.
2. NO muevas nada todavía. Solo crea las carpetas.

**Verificación**: las 6 carpetas existen y están vacías (o solo con `.gitkeep`).

### FASE 2 — Mover infra y scripts

1. Mueve `docker-compose.yml` → `infra/docker-compose.yml`.
2. Mueve los 4 scripts a `scripts/`: `iniciar.bat`, `detener.bat`, `abrir-frontend.bat`, `instalar.ps1`.

**Verificación**: los archivos ya NO están en la raíz; existen en sus nuevas carpetas; ningún archivo borrado.

### FASE 3 — Mover frontend

1. Mueve los 14 `.html` de la raíz a `frontend/`: `login.html`, `registro.html`, `dashboard.html`, `encuentros.html`, `feed.html`, `historial.html`, `perfil.html`, `ranking.html`, `tienda.html`, `transferencias.html`, `gestionar-admins.html`, `gestionar-encuentros.html`, `gestionar-estudiantes.html`, `gestionar-recompensas.html`.
2. Mueve la carpeta completa `js/` → `frontend/js/` (preservando `api.js`, `auth.js`, `theme.js`).
3. Mueve la carpeta completa `css/` → `frontend/css/` (preservando `dunab-theme.css`).
4. Mueve las imágenes:
   - `LogoUnabRojo.png` → `frontend/assets/img/LogoUnabRojo.png`
   - `TemplateDashEstructura.png` → `frontend/assets/img/TemplateDashEstructura.png`

**Verificación**:

- Raíz solo contiene: `backend` (aún `dunab-backend`), `docs`, `infra`, `scripts`, `frontend`, archivos meta (`.git`, `.gitignore`, `README*`).
- Dentro de `frontend/` hay 14 `.html`, `js/` con 3 archivos, `css/` con 1 archivo, y `assets/img/` con 2 PNG.

### FASE 4 — Ajustar referencias del FRONTEND

Las rutas relativas dentro de `frontend/` NO cambian (todos los `.html` siguen al mismo nivel que `js/` y `css/`). Pero las referencias a los PNG SÍ cambian porque las imágenes se movieron a `assets/img/`.

1. En `frontend/js/theme.js`, reemplaza TODAS las apariciones de:

   ```
   src="LogoUnabRojo.png"
   ```

   por:

   ```
   src="assets/img/LogoUnabRojo.png"
   ```

2. En los 14 `.html` de `frontend/`, busca cualquier referencia a `LogoUnabRojo.png` o `TemplateDashEstructura.png` (puede haberlas en `<img>`, en CSS inline o en JS inline) y reemplázalas por:
   - `LogoUnabRojo.png` → `assets/img/LogoUnabRojo.png`
   - `TemplateDashEstructura.png` → `assets/img/TemplateDashEstructura.png`

   Usa grep recursivo para encontrarlas; no asumas que solo están en `theme.js`.

3. Revisa `frontend/css/dunab-theme.css` por si hay `background-image: url(LogoUnabRojo.png)` o similares; ajústalas a `url(../assets/img/LogoUnabRojo.png)`. Si no hay ninguna, no toques el archivo.

4. NO modifiques las rutas a `js/api.js`, `js/auth.js`, `js/theme.js`, ni `css/dunab-theme.css` — siguen igual porque la jerarquía interna de `frontend/` replicó la de la raíz.

5. NO modifiques los `<a href="...html">` entre páginas — todos los `.html` están al mismo nivel.

6. NO toques `resolveLoginPath()` en `api.js` (línea ~40). Sigue funcionando porque `login.html` está en el mismo directorio que el resto.

**Verificación (Fase 4)**:

- `grep -rn "LogoUnabRojo.png" frontend/` solo debe mostrar líneas con la nueva ruta `assets/img/LogoUnabRojo.png`.
- `grep -rn "TemplateDashEstructura.png" frontend/` igual, con la nueva ruta.
- Abrir `frontend/login.html` en el navegador debe mostrar el logo UNAB correctamente (validar manualmente o reportar para validación humana).

### FASE 5 — Renombrar `dunab-backend/` → `backend/`

1. Renombra la carpeta: `dunab-backend/` → `backend/`. Mantén todo el contenido idéntico (`pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/`, `target/` si existe).

**Verificación**: existe `backend/pom.xml`; NO existe `dunab-backend/`.

### FASE 6 — Refactor interno de paquetes Java

Esta fase reorganiza paquetes dentro de `backend/src/main/java/com/unab/dunab/`. Es la fase con más riesgo de imports rotos. Hazla con cuidado y compila al final.

1. **Crear paquete `common/exception/`** y mover dentro:
   - `exception/DunabException.java` → `common/exception/DunabException.java`
   - `exception/GlobalExceptionHandler.java` → `common/exception/GlobalExceptionHandler.java`

   Elimina el paquete vacío `exception/`.

   Actualiza la sentencia `package com.unab.dunab.exception;` a `package com.unab.dunab.common.exception;` en ambos archivos.

2. **Crear paquete `domain/entity/`** y mover dentro las entidades JPA (las clases anotadas con `@Entity`):
   - `model/Usuario.java` → `domain/entity/Usuario.java`
   - `model/Encuentro.java` → `domain/entity/Encuentro.java`
   - `model/Inscripcion.java` → `domain/entity/Inscripcion.java`
   - `model/Transaccion.java` → `domain/entity/Transaccion.java`
   - `model/Transferencia.java` → `domain/entity/Transferencia.java`
   - `model/Logro.java` → `domain/entity/Logro.java`
   - `model/UsuarioLogro.java` → `domain/entity/UsuarioLogro.java`
   - `model/FeedEvent.java` → `domain/entity/FeedEvent.java`
   - `model/Recompensa.java` → `domain/entity/Recompensa.java`
   - `model/Canje.java` → `domain/entity/Canje.java`

   Actualiza `package com.unab.dunab.model;` → `package com.unab.dunab.domain.entity;` en cada uno.

3. **Crear paquete `domain/enums/`** y mover dentro los enums:
   - `model/Rol.java` → `domain/enums/Rol.java`
   - `model/EstadoEncuentro.java` → `domain/enums/EstadoEncuentro.java`
   - `model/TipoTransaccion.java` → `domain/enums/TipoTransaccion.java`
   - `model/FeedTipo.java` → `domain/enums/FeedTipo.java`

   Actualiza `package com.unab.dunab.model;` → `package com.unab.dunab.domain.enums;` en cada uno.

   Elimina el paquete vacío `model/`.

4. **Crear paquete `mapper/`** vacío con un archivo `.gitkeep` dentro (placeholder para mappers MapStruct futuros). Si encuentras interfaces MapStruct dentro de los servicios, NO las muevas en esta fase — déjalo para una fase posterior y repórtalo.

5. **ACTUALIZAR IMPORTS** en TODO el código Java:

   Reemplazo global recursivo dentro de `backend/src/main/java/`:

   - `import com.unab.dunab.exception.DunabException;` → `import com.unab.dunab.common.exception.DunabException;`
   - `import com.unab.dunab.exception.GlobalExceptionHandler;` → `import com.unab.dunab.common.exception.GlobalExceptionHandler;`
   - `import com.unab.dunab.model.Usuario;` → `import com.unab.dunab.domain.entity.Usuario;`
   - (repetir para las 10 entidades de 6.2)
   - `import com.unab.dunab.model.Rol;` → `import com.unab.dunab.domain.enums.Rol;`
   - (repetir para los 4 enums de 6.3)

   Usa búsqueda y reemplazo precisa, no regex laxa. Verifica que no queden imports `com.unab.dunab.model.` o `com.unab.dunab.exception.` en ningún `.java`.

6. NO modifiques `application.properties` ni los `.sql` de Flyway. Las migraciones referencian nombres de TABLA (`usuarios`, `encuentros`, etc.), no clases Java.

7. NO modifiques los `@Table(name="…")` ni los `@Column(name="…")` de las entidades. Los nombres físicos de DB no cambian.

**Verificación (Fase 6) — CRÍTICA**:

- **A.** `cd backend && ./mvnw clean compile` (en Windows: `mvnw.cmd clean compile`) debe terminar con `BUILD SUCCESS` y CERO errores de compilación. Si hay errores, son imports que no actualizaste; ve a 6.5 y completa.
- **B.** `grep -rn "com.unab.dunab.model" backend/src/main/java/` debe NO devolver ningún resultado.
- **C.** `grep -rn "com.unab.dunab.exception" backend/src/main/java/` debe NO devolver ningún resultado.
- **D.** Los paquetes `model/` y `exception/` dentro de `com/unab/dunab/` NO deben existir.

### FASE 7 — Ajustar scripts (.bat / .ps1)

Los scripts se movieron a `scripts/` y referencian rutas relativas a la raíz del proyecto. Hay que arreglarlos para que sigan funcionando desde su nueva ubicación.

1. `scripts/iniciar.bat`:
   - Cualquier `cd dunab-backend` o `cd /d dunab-backend` → `cd /d "%~dp0..\backend"`.
   - Cualquier `docker compose up -d` o `docker-compose up -d` → `docker compose -f "%~dp0..\infra\docker-compose.yml" up -d`.
   - Si llama `mvnw` o `mvnw.cmd`, mantén la llamada pero asegúrate de estar en `backend/` cuando se invoca.

2. `scripts/detener.bat`:
   - `docker compose down` → `docker compose -f "%~dp0..\infra\docker-compose.yml" down`.

3. `scripts/abrir-frontend.bat`:
   - Cualquier `start login.html` o `start "" login.html` → `start "" "%~dp0..\frontend\login.html"`.

4. `scripts/instalar.ps1`:
   - Si contiene `Set-Location dunab-backend` o `cd dunab-backend`, cámbialo a `Set-Location (Join-Path $PSScriptRoot "..\backend")`.
   - Si arranca docker-compose, ajusta la ruta a `..\infra\docker-compose.yml`.

5. Crea en la raíz un `README.md` mínimo con:
   - Descripción de DUNAB en 2-3 líneas.
   - "Cómo correr local": `scripts\iniciar.bat`.
   - "Cómo abrir el frontend": `scripts\abrir-frontend.bat`.
   - Login inicial: `admin@unab.edu.co` / `Admin1234!`.

**Verificación (Fase 7)**:

- Ejecutar `scripts\iniciar.bat` desde la raíz debe levantar Postgres (vía docker) y arrancar Spring Boot en `:8080` sin errores.
- Ejecutar `scripts\abrir-frontend.bat` debe abrir `login.html` en el navegador con el logo UNAB visible.

### FASE 8 — Documentación

1. Crea `docs/arquitectura.md` con el árbol final de carpetas, una explicación de 1 línea por carpeta de alto nivel, y la lista de reglas R1–R8 de este prompt.

### FASE 9 — Verificación integral (CHECKLIST FINAL)

Marca cada punto antes de declarar terminado el refactor:

- [ ] La raíz solo contiene: `backend/`, `frontend/`, `docs/`, `infra/`, `scripts/`, `README.md`, `.gitignore`, `.git/`.
- [ ] `cd backend && mvnw clean compile` → `BUILD SUCCESS`.
- [ ] `cd backend && mvnw spring-boot:run` arranca el backend en `:8080`. Validar `curl http://localhost:8080/api/swagger-ui.html` responde.
- [ ] Login: abrir `frontend/login.html`, autenticarse con `admin@unab.edu.co` / `Admin1234!` y llegar a `dashboard.html` sin errores en la consola del navegador.
- [ ] El logo UNAB se muestra en `login.html`, `dashboard.html` y al menos una página de admin.
- [ ] Navegar entre `dashboard`, `encuentros`, `ranking`, `transferencias`, `historial`, `tienda`, `feed`, `perfil` — todas cargan sin 404 en la pestaña Network del navegador.
- [ ] Como SUPERADMIN, navegar a `gestionar-admins.html`, `gestionar-estudiantes.html`, `gestionar-encuentros.html`, `gestionar-recompensas.html` — todas cargan sin 404.
- [ ] Logout redirige correctamente a `login.html`.
- [ ] `git status` muestra solo cambios esperados; no hay archivos perdidos.

Si TODAS las casillas están marcadas, haz commit:

```bash
git add -A
git commit -m "refactor: reestructura del proyecto a arquitectura profesional (backend/, frontend/, docs/, infra/, scripts/) y reorganización de paquetes Java (domain.entity, domain.enums, common.exception)"
```

Si alguna casilla falla, REPORTA el fallo específico, NO hagas commit, y propón el fix antes de continuar.

---

## 7. SALIDA ESPERADA

Al terminar, entrega un reporte con:

1. Lista de archivos movidos (origen → destino).
2. Lista de archivos modificados (con motivo: ruta de imagen, import Java, ajuste de script, etc.).
3. Resultado de cada bloque de verificación (PASS/FAIL).
4. Comando exacto para retomar trabajo (`git checkout refactor/estructura-profesional`).

---

**FIN DEL PROMPT**
