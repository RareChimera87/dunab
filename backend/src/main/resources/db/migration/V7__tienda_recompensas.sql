-- V7: Tienda de recompensas DUNAB
-- Catálogo de recompensas creadas por el admin y registro de canjes de estudiantes.

-- ── Catálogo de recompensas ───────────────────────────────────────────────────
CREATE TABLE recompensas (
    id           BIGSERIAL    PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  TEXT,
    emoji        VARCHAR(10)  NOT NULL DEFAULT '🎁',
    categoria    VARCHAR(50)  NOT NULL DEFAULT 'GENERAL',
    costo_dunab  INTEGER      NOT NULL CHECK (costo_dunab > 0),
    stock        INTEGER      NOT NULL DEFAULT -1,   -- -1 = ilimitado
    activa       BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por   BIGINT       REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en    TIMESTAMP    NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- ── Historial de canjes ───────────────────────────────────────────────────────
CREATE TABLE canjes (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    recompensa_id   BIGINT       NOT NULL REFERENCES recompensas(id),
    costo_dunab     INTEGER      NOT NULL,   -- snapshot del costo al momento del canje
    codigo_canje    VARCHAR(20)  NOT NULL UNIQUE,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE | ENTREGADO | CANCELADO
    canjeado_en     TIMESTAMP    NOT NULL DEFAULT NOW(),
    entregado_en    TIMESTAMP
);

-- Índices
CREATE INDEX idx_recompensas_activa   ON recompensas (activa);
CREATE INDEX idx_canjes_usuario       ON canjes (usuario_id);
CREATE INDEX idx_canjes_recompensa    ON canjes (recompensa_id);
CREATE INDEX idx_canjes_estado        ON canjes (estado);
CREATE INDEX idx_canjes_codigo        ON canjes (codigo_canje);
