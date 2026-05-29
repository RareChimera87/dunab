-- V8: agrega columna facultad a la tabla usuarios
-- Por defecto: Facultad de Ciencias Jurídicas y Políticas

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS facultad VARCHAR(150)
        NOT NULL DEFAULT 'Facultad de Ciencias Jurídicas y Políticas';
