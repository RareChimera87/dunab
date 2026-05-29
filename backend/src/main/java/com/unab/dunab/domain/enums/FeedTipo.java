package com.unab.dunab.domain.enums;

/**
 * Tipos de eventos que pueden aparecer en el feed de actividad.
 */
public enum FeedTipo {
    INSCRIPCION,  // Se inscribió a un encuentro
    LOGRO,        // Desbloqueó un badge
    HITO,         // Alcanzó hito de saldo (1k, 2.5k, 5k, 7.5k, 10k)
    PUBLICACION   // Publicación libre del usuario
}
