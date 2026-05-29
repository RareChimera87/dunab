-- V6: Convertir columna "tipo" de feed_events de native enum a VARCHAR(50)
-- Hibernate 6 serializa los @Enumerated(EnumType.STRING) como character varying,
-- lo que PostgreSQL rechaza cuando el tipo de la columna es un enum nativo.
-- Solución: convertir la columna a VARCHAR y eliminar el tipo nativo.

ALTER TABLE feed_events
    ALTER COLUMN tipo TYPE VARCHAR(50) USING tipo::VARCHAR;

DROP TYPE feed_tipo;
