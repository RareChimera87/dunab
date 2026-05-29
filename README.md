# DUNAB — Gestión de Dinero UNAB

Sistema de gestión de DUNAB (moneda/puntos académicos) para estudiantes de la Universidad Autónoma de Bucaramanga. Permite acumular, transferir y canjear DUNAB obtenidos en encuentros académicos, con ranking, logros, historial de transacciones y meta de graduación (10.000 DUNAB).

## Estructura del proyecto

```
DUNAB/
├── backend/      Spring Boot 3.2.5 + Java 17 + PostgreSQL
├── frontend/     HTML/CSS/JS vanilla (14 páginas)
├── docs/         Documentación de arquitectura
├── infra/        docker-compose.yml (PostgreSQL 16)
└── scripts/      Scripts de inicio/detención para Windows
```



## Login inicial

| Campo    | Valor                  |
|----------|------------------------|
| Email    | admin@unab.edu.co      |
| Password | Admin1234!             |
| Rol      | SUPERADMIN             |

## API

- REST API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
