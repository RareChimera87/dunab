-- V4: Preferencia de tema (claro/oscuro) por usuario
-- Persistimos por cuenta para que la preferencia viaje entre dispositivos.
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS tema_preferencia VARCHAR(10) NOT NULL DEFAULT 'CLARO';
