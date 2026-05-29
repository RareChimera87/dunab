-- V3: PIN de seguridad para transferencias
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pin_seguridad VARCHAR(255);
