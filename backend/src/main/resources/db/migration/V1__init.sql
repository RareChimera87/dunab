-- ═══════════════════════════════════════════════════════
--  DUNAB – Migración inicial de la base de datos
--  V1__init.sql
-- ═══════════════════════════════════════════════════════

-- ── Tabla de usuarios ────────────────────────────────
CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(120)        NOT NULL,
    correo          VARCHAR(120)        NOT NULL UNIQUE,
    contrasena      VARCHAR(255)        NOT NULL,
    codigo          VARCHAR(20)         NOT NULL UNIQUE,
    cedula          VARCHAR(30),
    celular         VARCHAR(20),
    carrera         VARCHAR(100),
    semestre        INTEGER             DEFAULT 1,
    rol             VARCHAR(20)         NOT NULL DEFAULT 'ESTUDIANTE',
    balance_dunab   INTEGER             NOT NULL DEFAULT 0,
    racha_dias      INTEGER             NOT NULL DEFAULT 0,
    ultima_actividad DATE,
    activo          BOOLEAN             NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP           NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ── Tabla de encuentros ───────────────────────────────
CREATE TABLE encuentros (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(200)        NOT NULL,
    descripcion     TEXT,
    lugar           VARCHAR(100)        NOT NULL,
    fecha           DATE                NOT NULL,
    hora_inicio     TIME                NOT NULL,
    hora_fin        TIME                NOT NULL,
    dunab_recompensa INTEGER            NOT NULL DEFAULT 0,
    dunab_penalizacion INTEGER          NOT NULL DEFAULT 0,
    cupos_max       INTEGER             NOT NULL DEFAULT 30,
    cupos_ocupados  INTEGER             NOT NULL DEFAULT 0,
    estado          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVO',
    visible         BOOLEAN             NOT NULL DEFAULT TRUE,
    creado_por      BIGINT              REFERENCES usuarios(id),
    creado_en       TIMESTAMP           NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ── Tabla de inscripciones ────────────────────────────
CREATE TABLE inscripciones (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT              NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    encuentro_id    BIGINT              NOT NULL REFERENCES encuentros(id) ON DELETE CASCADE,
    asistio         BOOLEAN,
    penalizado      BOOLEAN             NOT NULL DEFAULT FALSE,
    inscrito_en     TIMESTAMP           NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inscripcion UNIQUE (usuario_id, encuentro_id)
);

-- ── Tabla de transacciones ────────────────────────────
CREATE TABLE transacciones (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT              NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo            VARCHAR(30)         NOT NULL,  -- INGRESO, EGRESO, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECIBIDA, PENALIZACION
    monto           INTEGER             NOT NULL,
    balance_post    INTEGER             NOT NULL,
    descripcion     VARCHAR(300),
    referencia_id   BIGINT,                        -- ID de encuentro o transferencia relacionada
    creado_en       TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ── Tabla de transferencias P2P ───────────────────────
CREATE TABLE transferencias (
    id              BIGSERIAL PRIMARY KEY,
    remitente_id    BIGINT              NOT NULL REFERENCES usuarios(id),
    destinatario_id BIGINT              NOT NULL REFERENCES usuarios(id),
    monto           INTEGER             NOT NULL CHECK (monto > 0 AND monto <= 2000),
    nota            VARCHAR(300),
    estado          VARCHAR(20)         NOT NULL DEFAULT 'COMPLETADA',
    creado_en       TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ── Tabla de logros ───────────────────────────────────
CREATE TABLE logros (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(50)         NOT NULL UNIQUE,
    nombre          VARCHAR(120)        NOT NULL,
    descripcion     VARCHAR(300),
    emoji           VARCHAR(10),
    condicion_tipo  VARCHAR(50),        -- BALANCE, RACHA, ENCUENTROS, RANKING
    condicion_valor INTEGER
);

-- ── Tabla de logros obtenidos por usuario ─────────────
CREATE TABLE usuario_logros (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT              NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    logro_id        BIGINT              NOT NULL REFERENCES logros(id),
    obtenido_en     TIMESTAMP           NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_logro UNIQUE (usuario_id, logro_id)
);

-- ── Índices de rendimiento ────────────────────────────
CREATE INDEX idx_transacciones_usuario    ON transacciones(usuario_id);
CREATE INDEX idx_transacciones_tipo       ON transacciones(tipo);
CREATE INDEX idx_transacciones_fecha      ON transacciones(creado_en);
CREATE INDEX idx_inscripciones_usuario    ON inscripciones(usuario_id);
CREATE INDEX idx_inscripciones_encuentro  ON inscripciones(encuentro_id);
CREATE INDEX idx_encuentros_fecha         ON encuentros(fecha);
CREATE INDEX idx_encuentros_lugar         ON encuentros(lugar);
CREATE INDEX idx_usuarios_balance         ON usuarios(balance_dunab DESC);

-- ── Datos iniciales: logros del sistema ──────────────
INSERT INTO logros (codigo, nombre, descripcion, emoji, condicion_tipo, condicion_valor) VALUES
    ('PRIMER_PASO',    'Primer Paso',         'Asististe a tu primer encuentro',              '🎯', 'ENCUENTROS', 1),
    ('RACHA_7',        'Racha Semanal',        'Mantuviste una racha de 7 días',               '🔥', 'RACHA',      7),
    ('RACHA_30',       'Racha Mensual',        'Mantuviste una racha de 30 días',              '🔥', 'RACHA',      30),
    ('RACHA_60',       'Racha Bimestral',      'Mantuviste una racha de 60 días',              '🔥', 'RACHA',      60),
    ('BALANCE_1000',   'Primer Millar',        'Acumulaste 1.000 DUNAB',                       '💰', 'BALANCE',    1000),
    ('BALANCE_5000',   'A Mitad del Camino',   'Acumulaste 5.000 DUNAB',                       '⭐', 'BALANCE',    5000),
    ('BALANCE_10000',  'Graduado',             'Alcanzaste la meta de 10.000 DUNAB',           '🎓', 'BALANCE',    10000),
    ('ENCUENTROS_10',  '10 Encuentros',        'Asististe a 10 encuentros',                    '🏛️', 'ENCUENTROS', 10),
    ('ENCUENTROS_50',  '50 Encuentros',        'Asististe a 50 encuentros',                    '🏅', 'ENCUENTROS', 50),
    ('TOP_20',         'Top 20',               'Llegaste al top 20 del ranking',               '⭐', 'RANKING',    20),
    ('TOP_5',          'Top 5',                'Llegaste al top 5 del ranking',                '👑', 'RANKING',    5),
    ('PRIMERA_TX',     'Primera Transferencia','Realizaste tu primera transferencia P2P',      '💸', 'BALANCE',    0);

-- ── Usuario administrador por defecto ─────────────────
-- Correo: admin@unab.edu.co  |  Contraseña: Admin1234!
INSERT INTO usuarios (nombre, correo, contrasena, codigo, carrera, semestre, rol, balance_dunab) VALUES
    ('Administrador DUNAB', 'admin@unab.edu.co',
     '$2b$10$hLzo3s4zgylri9w.h/0iCe1XDxkB.6H9ldxQN22b0P/lZPns3XHam',
     'ADMIN001', 'Administración', 1, 'ADMIN', 0);
