/**
 * DUNAB — Cliente centralizado de la API REST
 * Todos los módulos del frontend deben importar este archivo.
 *
 * Uso:
 *   <script src="../js/api.js"></script>  (ajusta la ruta según la profundidad)
 */

// ── Configuración base ────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

// ── Gestión del token JWT en localStorage ─────────────────────────────────────
const Auth = {
  getToken:   ()      => localStorage.getItem('dunab_token'),
  setToken:   (t)     => localStorage.setItem('dunab_token', t),
  clearToken: ()      => localStorage.removeItem('dunab_token'),

  getUser:    ()      => {
    const raw = localStorage.getItem('dunab_user');
    return raw ? JSON.parse(raw) : null;
  },
  setUser: (u) => localStorage.setItem('dunab_user', JSON.stringify(u)),
  clearUser: () => localStorage.removeItem('dunab_user'),

  isLoggedIn: ()      => !!localStorage.getItem('dunab_token'),

  logout: () => {
    localStorage.removeItem('dunab_token');
    localStorage.removeItem('dunab_user');
    window.location.href = resolveLoginPath();
  }
};

/**
 * Devuelve la URL absoluta a login.html.
 * Funciona tanto con file:// (Windows/Mac) como con http://.
 * Todos los HTML están en el mismo directorio, así que basta con
 * reemplazar el último segmento del href actual.
 */
function resolveLoginPath() {
  const href = window.location.href.split('?')[0].split('#')[0];
  const base = href.substring(0, href.lastIndexOf('/') + 1);
  return base + 'login.html';
}

// ── Fetch genérico con headers JWT ────────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const token = Auth.getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...(options.headers || {})
  };

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401) {
    Auth.logout();
    return;
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : {};

  if (!res.ok) {
    // Si el backend devuelve errores de campo, construir mensaje detallado
    let msg = data.mensaje || data.error || 'Error del servidor';
    if (data.campos && typeof data.campos === 'object') {
      const detalle = Object.entries(data.campos)
        .map(([campo, err]) => `${campo}: ${err}`)
        .join(' | ');
      msg = detalle || msg;
    }
    throw Object.assign(new Error(msg), { status: res.status, data });
  }

  return data;
}

const apiGet    = (path)         => apiFetch(path, { method: 'GET' });
const apiPost   = (path, body)   => apiFetch(path, { method: 'POST',   body: JSON.stringify(body) });
const apiPut    = (path, body)   => apiFetch(path, { method: 'PUT',    body: JSON.stringify(body) });
const apiDelete = (path)         => apiFetch(path, { method: 'DELETE' });

// ═════════════════════════════════════════════════════════════════════════════
//  AUTENTICACIÓN
// ═════════════════════════════════════════════════════════════════════════════
const AuthAPI = {
  async login(correo, contrasena) {
    const data = await apiPost('/auth/login', { correo, contrasena });
    Auth.setToken(data.token);
    Auth.setUser({
      id: data.usuarioId, nombre: data.nombre, correo: data.correo,
      rol: data.rol, balanceDunab: data.balanceDunab,
      facultad: data.facultad || 'Facultad de Ciencias Jurídicas y Políticas',
      temaPreferencia: data.temaPreferencia || 'CLARO'
    });
    // Aplicar el tema del usuario inmediatamente tras el login
    if (typeof ThemeManager !== 'undefined') ThemeManager.set(data.temaPreferencia || 'CLARO');
    return data;
  },

  async register(payload) {
    const data = await apiPost('/auth/register', payload);
    Auth.setToken(data.token);
    Auth.setUser({
      id: data.usuarioId, nombre: data.nombre, correo: data.correo,
      rol: data.rol, balanceDunab: data.balanceDunab,
      facultad: data.facultad || 'Facultad de Ciencias Jurídicas y Políticas',
      temaPreferencia: data.temaPreferencia || 'CLARO'
    });
    if (typeof ThemeManager !== 'undefined') ThemeManager.set(data.temaPreferencia || 'CLARO');
    return data;
  }
};

// ═════════════════════════════════════════════════════════════════════════════
//  USUARIO / PERFIL
// ═════════════════════════════════════════════════════════════════════════════
const UsuarioAPI = {
  getMiPerfil:       ()        => apiGet('/usuarios/me'),
  actualizarPerfil:  (payload) => apiPut('/usuarios/me', payload),
  cambiarContrasena: (actual, nueva) =>
    apiPut('/usuarios/me/contrasena', { contrasenaActual: actual, nuevaContrasena: nueva }),
  getMisLogros:      ()        => apiGet('/usuarios/me/logros'),
  getPerfilById:     (id)      => apiGet(`/usuarios/${id}`),

  // Admin / Superadmin
  listarTodos:    ()             => apiGet('/usuarios/admin/todos'),
  listarEstudiantes: ()         => apiGet('/usuarios/admin/estudiantes'),
  editarEstudiante: (id, payload) => apiPut(`/usuarios/admin/${id}`, payload),
  eliminarEstudiante: (id)      => apiDelete(`/usuarios/admin/${id}`),
  ajustarDunab:   (id, monto, motivo) =>
    apiPost(`/usuarios/admin/${id}/ajustar-dunab`, { monto, motivo }),
  toggleActivo:   (id)          => apiPut(`/usuarios/admin/${id}/toggle`),
  acreditarMe:    (cantidad)    => apiPost('/usuarios/admin/acreditar-me', { cantidad }),

  // Solo Superadmin
  listarAdmins:   ()             => apiGet('/usuarios/admin/admins'),
  crearAdmin:     (payload)     => apiPost('/usuarios/admin/crear', payload),
  acreditarUsuario: (id, cant)  => apiPost(`/usuarios/admin/${id}/acreditar`, { cantidad: cant }),

  // PIN de seguridad para transferencias
  estadoPin:      ()                    => apiGet('/usuarios/me/pin/estado'),
  establecerPin:  (pinActual, pinNuevo) => apiPut('/usuarios/me/pin', { pinActual, pinNuevo }),
  validarPin:     (pin)                 => apiPost('/usuarios/me/pin/validar', { pin }),

  // Preferencia de tema (claro/oscuro) — persistida en backend
  actualizarTema: (tema)                => apiPut('/usuarios/me/tema', { tema })
};

// ═════════════════════════════════════════════════════════════════════════════
//  ENCUENTROS
// ═════════════════════════════════════════════════════════════════════════════
const EncuentroAPI = {
  listar: (filtros = {}) => {
    const params = new URLSearchParams();
    if (filtros.lugar)  params.append('lugar',  filtros.lugar);
    if (filtros.estado) params.append('estado', filtros.estado);
    if (filtros.desde)  params.append('desde',  filtros.desde);
    if (filtros.hasta)  params.append('hasta',  filtros.hasta);
    const q = params.toString();
    return apiGet('/encuentros' + (q ? '?' + q : ''));
  },

  getById:           (id)      => apiGet(`/encuentros/${id}`),

  // Admin / Superadmin
  listarTodos:       ()        => apiGet('/encuentros/admin/todos'),
  getInscritos:      (id)      => apiGet(`/encuentros/admin/${id}/inscritos`),
  crear:             (payload) => apiPost('/encuentros/admin', payload),
  actualizar:        (id, p)   => apiPut(`/encuentros/admin/${id}`, p),
  eliminar:          (id)      => apiDelete(`/encuentros/admin/${id}`),
  toggleVisibilidad: (id)      => apiPut(`/encuentros/admin/${id}/visibilidad`),
  penalizar:         (id)      => apiPost(`/encuentros/admin/${id}/penalizar`),
};

// ═════════════════════════════════════════════════════════════════════════════
//  INSCRIPCIONES
// ═════════════════════════════════════════════════════════════════════════════
const InscripcionAPI = {
  inscribir:  (encuentroId) => apiPost(`/inscripciones/${encuentroId}`),
  cancelar:   (encuentroId) => apiDelete(`/inscripciones/${encuentroId}`),

  // Admin
  registrarAsistencia: (encuentroId, usuarioId, asistio) =>
    apiPost(`/inscripciones/admin/${encuentroId}/asistencia`, { usuarioId, asistio })
};

// ═════════════════════════════════════════════════════════════════════════════
//  TRANSACCIONES / ESTADÍSTICAS
// ═════════════════════════════════════════════════════════════════════════════
const TransaccionAPI = {
  getHistorial:    () => apiGet('/transacciones/me'),
  getEstadisticas: () => apiGet('/estadisticas/me')
};

// ═════════════════════════════════════════════════════════════════════════════
//  TRANSFERENCIAS P2P
// ═════════════════════════════════════════════════════════════════════════════
const TransferenciaAPI = {
  enviar:          (destinatarioId, monto, nota) =>
    apiPost('/transferencias', { destinatarioId, monto, nota }),
  historialCompleto: () => apiGet('/transferencias/me'),
  enviadas:          () => apiGet('/transferencias/me/enviadas'),
  recibidas:         () => apiGet('/transferencias/me/recibidas'),
  buscarDestinatario: (q) => apiGet(`/transferencias/buscar?q=${encodeURIComponent(q)}`)
};

// ═════════════════════════════════════════════════════════════════════════════
//  RANKING
// ═════════════════════════════════════════════════════════════════════════════
const RankingAPI = {
  getRanking:   ()      => apiGet('/ranking'),
  getTop:       (n)     => apiGet(`/ranking/top?limite=${n}`),
  getMiPosicion: ()     => apiGet('/ranking/mi-posicion')
};

// ═════════════════════════════════════════════════════════════════════════════
//  UTILIDADES DE UI
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Muestra un toast de notificación (requiere que la página tenga el
 * contenedor #toast-container o lo crea dinámicamente).
 */
function showToast(msg, tipo = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.style.cssText =
      'position:fixed;top:20px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:8px';
    document.body.appendChild(container);
  }

  const colors = {
    success: '#22c55e',
    error:   '#ef4444',
    info:    '#3b82f6',
    warning: '#f59e0b'
  };

  const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };

  const toast = document.createElement('div');
  toast.style.cssText = `
    background:#fff;border-left:4px solid ${colors[tipo] || colors.info};
    padding:12px 16px;border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,.15);
    display:flex;align-items:center;gap:10px;min-width:260px;max-width:360px;
    font-family:Inter,sans-serif;font-size:14px;animation:slideInToast .3s ease;
  `;
  toast.innerHTML = `
    <span style="color:${colors[tipo]};font-weight:700;font-size:16px">${icons[tipo] || icons.info}</span>
    <span style="color:#1f2937;flex:1">${msg}</span>
  `;

  if (!document.querySelector('#dunab-toast-style')) {
    const style = document.createElement('style');
    style.id = 'dunab-toast-style';
    style.textContent = `
      @keyframes slideInToast { from { transform:translateX(110%); opacity:0; } to { transform:translateX(0); opacity:1; } }
    `;
    document.head.appendChild(style);
  }

  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity .3s'; setTimeout(() => toast.remove(), 300); }, 3500);
}

/** Formatea número con separadores de miles */
function fmtNum(n) {
  return Number(n || 0).toLocaleString('es-CO');
}

/** Formatea fecha ISO a string legible */
function fmtFecha(isoStr) {
  if (!isoStr) return '—';
  const d = new Date(isoStr);
  return d.toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });
}

/** Emoji para tipo de encuentro según lugar */
function emojiLugar(lugar = '') {
  const l = lugar.toLowerCase();
  if (l.includes('cafet'))    return '☕';
  if (l.includes('clase'))    return '📚';
  if (l.includes('play'))     return '🎮';
  if (l.includes('bibliote')) return '🏛️';
  if (l.includes('csu'))      return '🎓';
  return '📍';
}
