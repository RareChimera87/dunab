-- V5: Feed de actividad global
-- Registra eventos del sistema y publicaciones de usuarios.

CREATE TYPE feed_tipo AS ENUM (
    'INSCRIPCION',   -- Usuario se inscribió a un encuentro
    'LOGRO',         -- Usuario desbloqueó un badge
    'HITO',          -- Usuario alcanzó un hito de saldo (1k, 2.5k, 5k, 7.5k, 10k)
    'PUBLICACION'    -- Publicación libre del usuario
);

CREATE TABLE feed_events (
    id               BIGSERIAL PRIMARY KEY,
    tipo             feed_tipo   NOT NULL,
    usuario_id       BIGINT      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    -- Para eventos que involucran a un segundo usuario (futuro: reacciones, etc.)
    target_id        BIGINT      REFERENCES usuarios(id) ON DELETE SET NULL,
    -- Mensaje generado automáticamente para INSCRIPCION / LOGRO / HITO
    mensaje          VARCHAR(255),
    -- Cuerpo libre solo para PUBLICACION
    cuerpo           TEXT,
    -- Metadata JSON opcional (ej: {"encuentroId": 3, "lugar": "Cafetería del L"})
    metadata         JSONB       DEFAULT '{}',
    -- Soft-delete (el usuario puede borrar su publicación)
    eliminado        BOOLEAN     NOT NULL DEFAULT FALSE,
    creado_en        TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Índices para las consultas del feed (más recientes primero, por usuario)
CREATE INDEX idx_feed_creado   ON feed_events (creado_en DESC);
CREATE INDEX idx_feed_usuario  ON feed_events (usuario_id);
CREATE INDEX idx_feed_tipo     ON feed_events (tipo);
