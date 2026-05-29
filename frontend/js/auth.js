/**
 * DUNAB — Guardia de autenticación y utilidades de sesión
 *
 * Incluir DESPUÉS de api.js en todas las páginas protegidas:
 *   <script src="../js/api.js"></script>
 *   <script src="../js/auth.js"></script>
 *
 * Para páginas de admin, añadir:
 *   <script>requireAdmin();</script>
 */

// ── Guard principal: redirige a login si no hay sesión ────────────────────────
(function guardAuth() {
  if (!Auth.isLoggedIn()) {
    window.location.replace(resolveLoginPath());
  }
})();

// ── Poblar datos del usuario en el sidebar/header ─────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  const user = Auth.getUser();
  if (!user) return;

  // ── Aplicar rol en la UI ──────────────────────────────────────────────────
  _applyUserToUI(user);

  // ── Visibilidad de nav según rol ─────────────────────────────────────────
  _applyRoleNav(user.rol);

  // ── Refrescar balance desde la API (silencioso) ──────────────────────────
  try {
    const perfil = await UsuarioAPI.getMiPerfil();
    const updated = {
      ...user,
      balanceDunab: perfil.balanceDunab,
      nombre: perfil.nombre,
      rol: perfil.rol,
      facultad: perfil.facultad || user.facultad || 'Facultad de Ciencias Jurídicas y Políticas',
      temaPreferencia: perfil.temaPreferencia || user.temaPreferencia || 'CLARO'
    };
    Auth.setUser(updated);
    // Sincronizar tema con backend si difiere
    if (typeof ThemeManager !== 'undefined' && updated.temaPreferencia !== ThemeManager.get()) {
      ThemeManager.set(updated.temaPreferencia);
    }
    document.querySelectorAll('[data-user-balance]').forEach(el => {
      el.textContent = fmtNum(updated.balanceDunab) + ' D';
    });
    document.querySelectorAll('[data-user-faculty]').forEach(el => {
      el.textContent = updated.facultad;
    });
  } catch (_) { /* silencioso — usamos datos cacheados */ }
});

function _applyUserToUI(user) {
  // Nombre en sidebar
  document.querySelectorAll('.user-name, .sidebar-user-name, [data-user-name]')
    .forEach(el => { el.textContent = user.nombre || 'Usuario'; });

  // Correo / rol
  document.querySelectorAll('.user-role, .sidebar-user-role, [data-user-role]')
    .forEach(el => { el.textContent = user.rol === 'ADMIN' ? 'Administrador' : 'Estudiante'; });

  // Facultad en el user-chip
  document.querySelectorAll('[data-user-faculty]')
    .forEach(el => { el.textContent = user.facultad || 'Facultad de Ciencias Jurídicas y Políticas'; });

  // Balance en sidebar
  document.querySelectorAll('[data-user-balance]')
    .forEach(el => { el.textContent = fmtNum(user.balanceDunab) + ' D'; });

  // Avatar con iniciales
  const initials = (user.nombre || 'U').split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
  document.querySelectorAll('.user-avatar, .sidebar-avatar, [data-user-avatar]').forEach(el => {
    if (el.tagName === 'IMG') {
      el.onerror = () => { el.style.display = 'none'; };
    } else {
      if (!el.querySelector('img') && !el.textContent.trim()) el.textContent = initials;
    }
  });

  // Botones de logout
  document.querySelectorAll('[data-logout], .logout-btn, #logoutBtn').forEach(btn => {
    btn.addEventListener('click', e => { e.preventDefault(); Auth.logout(); });
  });
}

function _applyRoleNav(rol) {
  const esAdmin     = rol === 'ADMIN' || rol === 'SUPERADMIN';
  const esSuperAdmin = rol === 'SUPERADMIN';

  // Mostrar/ocultar ítems de admin en el sidebar (funciona en todas las páginas)
  document.querySelectorAll('.nav-item-admin').forEach(el => {
    el.classList.toggle('visible', esAdmin);
  });

  // Mostrar/ocultar ítems exclusivos de superadmin
  document.querySelectorAll('.nav-item-super').forEach(el => {
    el.classList.toggle('visible', esSuperAdmin);
  });

  // Mostrar badge de rol en sidebar si existe el elemento
  document.querySelectorAll('.role-badge').forEach(el => {
    el.textContent = esAdmin ? '🔑 Admin' : '🎓 Estudiante';
    el.style.background = esAdmin ? '#6B1530' : '#1E40AF';
  });
}

// ── Guard de administrador ────────────────────────────────────────────────────
function requireAdmin() {
  const user = Auth.getUser();
  if (!user || (user.rol !== 'ADMIN' && user.rol !== 'SUPERADMIN')) {
    alert('Acceso restringido a administradores.');
    window.location.href = resolveLoginPath().replace('login.html', 'dashboard.html');
  }
}

// ── Refresca los datos del usuario desde la API y actualiza localStorage ──────
async function refreshUserData() {
  try {
    const perfil = await UsuarioAPI.getMiPerfil();
    Auth.setUser({
      id: perfil.id,
      nombre: perfil.nombre,
      correo: perfil.correo,
      rol: perfil.rol,
      balanceDunab: perfil.balanceDunab
    });
    return perfil;
  } catch (e) {
    console.warn('No se pudo refrescar el perfil:', e.message);
    return Auth.getUser();
  }
}

// ── Utilidad: lanzar logros desbloqueados como toasts ─────────────────────────
function mostrarLogrosNuevos(logros = []) {
  if (!logros.length) return;
  logros.forEach((l, i) => {
    setTimeout(() => {
      showToast(`${l.emoji || '🏅'} ¡Logro desbloqueado! <strong>${l.nombre}</strong>`, 'success');
    }, i * 800);
  });
}
