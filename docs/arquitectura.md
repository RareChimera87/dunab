# Arquitectura del Proyecto DUNAB

## Árbol de carpetas

```
DUNAB - Gestión de Dinero UNAB/
│
├── README.md                        Descripción general y guía de inicio rápido
├── .gitignore
│
├── docs/                            Documentación del proyecto
│   └── arquitectura.md              Este archivo
│
├── infra/                           Infraestructura y contenedores
│   └── docker-compose.yml           PostgreSQL 16-alpine en puerto 5432
│
├── scripts/                         Scripts de automatización para Windows
│   ├── iniciar.bat                  Levanta Docker + Spring Boot
│   ├── detener.bat                  Detiene backend y contenedores
│   ├── abrir-frontend.bat           Abre login.html en el navegador
│   └── instalar.ps1                 Instala Java 17, Maven y Docker Desktop
│
├── frontend/                        Interfaz de usuario (HTML/CSS/JS vanilla)
│   ├── login.html
│   ├── registro.html
│   ├── dashboard.html
│   ├── encuentros.html
│   ├── feed.html
│   ├── historial.html
│   ├── perfil.html
│   ├── ranking.html
│   ├── tienda.html
│   ├── transferencias.html
│   ├── gestionar-admins.html
│   ├── gestionar-encuentros.html
│   ├── gestionar-estudiantes.html
│   ├── gestionar-recompensas.html
│   ├── assets/
│   │   └── img/
│   │       ├── LogoUnabRojo.png
│   │       └── TemplateDashEstructura.png
│   ├── css/
│   │   └── dunab-theme.css          Tema global: variables, componentes, utilidades
│   └── js/
│       ├── api.js                   Cliente HTTP + autenticación JWT + resolveLoginPath()
│       ├── auth.js                  Guardia de rutas, manejo de sesión
│       └── theme.js                 Header, sidebar, logo, notificaciones visuales
│
└── backend/                         Servidor Spring Boot 3.2.5 / Java 17
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    └── src/main/
        ├── java/com/unab/dunab/
        │   ├── DunabApplication.java
        │   ├── config/              Configuración de seguridad (Spring Security + CORS)
        │   │   └── SecurityConfig.java
        │   ├── security/            JWT: filtro, utilidades, UserDetailsService
        │   │   ├── JwtFilter.java
        │   │   ├── JwtUtil.java
        │   │   └── UserDetailsServiceImpl.java
        │   ├── common/
        │   │   └── exception/       Excepciones de negocio y manejador global
        │   │       ├── DunabException.java
        │   │       └── GlobalExceptionHandler.java
        │   ├── domain/
        │   │   ├── entity/          Entidades JPA (@Entity): tablas de base de datos
        │   │   │   ├── Usuario.java
        │   │   │   ├── Encuentro.java
        │   │   │   ├── Inscripcion.java
        │   │   │   ├── Transaccion.java
        │   │   │   ├── Transferencia.java
        │   │   │   ├── Logro.java
        │   │   │   ├── UsuarioLogro.java
        │   │   │   ├── FeedEvent.java
        │   │   │   ├── Recompensa.java
        │   │   │   └── Canje.java
        │   │   └── enums/           Enumeraciones del dominio
        │   │       ├── Rol.java              (ESTUDIANTE, ADMIN, SUPERADMIN)
        │   │       ├── EstadoEncuentro.java
        │   │       ├── TipoTransaccion.java
        │   │       └── FeedTipo.java
        │   ├── repository/          Interfaces Spring Data JPA (acceso a datos)
        │   ├── service/             Lógica de negocio (9 servicios)
        │   ├── controller/          Controladores REST bajo /api (9 controladores)
        │   ├── dto/
        │   │   ├── request/         Objetos de entrada de la API (7 DTOs)
        │   │   └── response/        Objetos de salida de la API (8 DTOs)
        │   └── mapper/              Placeholder para mappers MapStruct futuros
        └── resources/
            ├── application.properties
            ├── application-dev.properties   Perfil H2 (sin Docker)
            └── db/migration/               Migraciones Flyway V1–V7 (NO TOCAR)
```

---

## Reglas de oro (R1–R8)

Estas reglas deben respetarse en **todo cambio futuro** al proyecto:

| # | Regla | Motivo |
|---|-------|--------|
| R1 | NO modificar archivos `.sql` en `db/migration/` | Las migraciones Flyway son inmutables; cambiarlas corrompe el esquema |
| R2 | NO cambiar nombres de tabla ni de columna en la BD | Ruptura de migraciones aplicadas y datos existentes |
| R3 | NO cambiar las rutas REST (`/auth/…`, `/usuarios/…`, etc.) | El frontend llama a estas rutas sin versionado |
| R4 | NO cambiar las claves de localStorage `dunab_token` ni `dunab_user` | Todas las páginas leen estas claves directamente en `auth.js` y `api.js` |
| R5 | NO tocar `application.properties` salvo cambios de entorno documentados | Configuración crítica de JPA, JWT y Flyway |
| R6 | NO subdividir los `.html` por rol dentro de `frontend/` | `resolveLoginPath()` en `api.js` asume que `login.html` está al mismo nivel |
| R7 | NO usar rutas absolutas en HTML/CSS/JS; siempre relativas | Portabilidad entre equipos y servidores |
| R8 | Antes de cualquier rename/move, hacer `git add -A && git commit -m "checkpoint"` | Punto de retorno ante errores de refactor |

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.2.5 · Java 17 · Maven |
| Seguridad | Spring Security · JJWT 0.12.5 (JWT) |
| Persistencia | Spring Data JPA · Hibernate · Flyway |
| Base de datos | PostgreSQL 16 (Docker) / H2 (perfil dev) |
| Utilidades | Lombok · MapStruct · Springdoc OpenAPI |
| Frontend | HTML5 · CSS3 · JavaScript ES2020 (vanilla) |
| Contenedores | Docker Compose |

---

## Credenciales de inicio

| Campo | Valor |
|-------|-------|
| Email | admin@unab.edu.co |
| Password | Admin1234! |
| Rol | SUPERADMIN |
