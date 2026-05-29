/**
 * DUNAB — Sistema de tema + logo + íconos de navegación
 *
 * Rebranding 2026 · Incluye:
 *  - Logo oficial UNAB (PNG)
 *  - ThemeManager (claro / oscuro)
 *  - Inyección de íconos SVG en el nav
 *  - Etiquetas de sección MENÚ / ADMINISTRACIÓN
 *  - Toggle de tema auto-inyectado en topnav
 *
 * Carga al final del <body> en todas las páginas.
 * El fragmento anti-flash va en el <head>.
 */

// ─────────────────────────────────────────────────────────────────────────────
// LOGO  —  usa el PNG oficial de la UNAB
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Devuelve el logo UNAB como etiqueta <img> apuntando al PNG oficial.
 * El PNG debe estar en la raíz del proyecto (mismo nivel que los .html).
 */
function dunabLogoSVG({ width = 200 } = {}) {
  return `<img
    src="assets/img/LogoUnabRojo.png"
    alt="UNAB · Facultad de Ciencias Jurídicas y Políticas"
    class="dunab-logo-img"
    style="max-width:${width}px;width:100%;height:auto;display:block;"
    draggable="false"
  />`;
}

/** Mini logo (cuadrado) — mantiene compatibilidad con llamadas antiguas. */
function dunabLogoMini({ size = 36 } = {}) {
  return `<img
    src="assets/img/LogoUnabRojo.png"
    alt="UNAB"
    style="width:${size}px;height:auto;display:block;"
    draggable="false"
  />`;
}

// ─────────────────────────────────────────────────────────────────────────────
// ÍCONOS SVG POR PÁGINA
// ─────────────────────────────────────────────────────────────────────────────
const _NAV_ICONS = {
  'dashboard.html': `<svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></svg>`,

  'encuentros.html': `<svg viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,

  'ranking.html': `<svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`,

  'historial.html': `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,

  'transferencias.html': `<svg viewBox="0 0 24 24"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 014-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 01-4 4H3"/></svg>`,

  'gestionar-estudiantes.html': `<svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>`,

  'gestionar-encuentros.html': `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>`,

  'gestionar-admins.html': `<svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,

  'tienda.html': `<svg viewBox="0 0 24 24"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></svg>`,

  'feed.html': `<svg viewBox="0 0 24 24"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0"/></svg>`,

  'gestionar-recompensas.html': `<svg viewBox="0 0 24 24"><polyline points="20 12 20 22 4 22 4 12"/><rect x="2" y="7" width="20" height="5"/><path d="M12 22V7"/><path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z"/></svg>`,

  'perfil.html': `<svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`
};

// ─────────────────────────────────────────────────────────────────────────────
// THEME MANAGER
// ─────────────────────────────────────────────────────────────────────────────
const ThemeManager = (function () {
  const STORAGE_KEY = 'dunab_theme';
  const VALID = ['CLARO', 'OSCURO'];

  function _read() {
    try {
      const u = JSON.parse(localStorage.getItem('dunab_user') || 'null');
      if (u && VALID.includes(u.temaPreferencia)) return u.temaPreferencia;
    } catch (_) {}
    const t = localStorage.getItem(STORAGE_KEY);
    return VALID.includes(t) ? t : 'CLARO';
  }

  function _apply(tema) {
    if (tema === 'OSCURO') {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
    document.dispatchEvent(new CustomEvent('dunab:theme-change', { detail: { tema } }));
  }

  function _persistLocal(tema) {
    localStorage.setItem(STORAGE_KEY, tema);
    try {
      const u = JSON.parse(localStorage.getItem('dunab_user') || 'null');
      if (u) {
        u.temaPreferencia = tema;
        localStorage.setItem('dunab_user', JSON.stringify(u));
      }
    } catch (_) {}
  }

  async function _persistBackend(tema) {
    try {
      if (
        typeof UsuarioAPI !== 'undefined' &&
        UsuarioAPI.actualizarTema &&
        (typeof Auth === 'undefined' || Auth.isLoggedIn())
      ) {
        await UsuarioAPI.actualizarTema(tema);
      }
    } catch (e) {
      console.warn('No se pudo sincronizar el tema con el backend:', e.message);
    }
  }

  function get()  { return _read(); }

  function set(tema) {
    if (!VALID.includes(tema)) return;
    _apply(tema);
    _persistLocal(tema);
    _persistBackend(tema);
  }

  function toggle() {
    set(get() === 'CLARO' ? 'OSCURO' : 'CLARO');
  }

  function init() { _apply(_read()); }

  return { get, set, toggle, init };
})();

// Aplicar tema de inmediato si no se llamó en <head>
ThemeManager.init();

// ─────────────────────────────────────────────────────────────────────────────
// INYECCIONES DOM
// ─────────────────────────────────────────────────────────────────────────────

/** Inyecta el logo PNG en el slot de la sidebar. */
function _injectSidebarLogo() {
  const slot = document.querySelector('.sidebar-logo[data-auto-logo]');
  if (!slot || slot.innerHTML.trim()) return;
  slot.innerHTML = dunabLogoSVG({ width: 190 });
}

/** Reemplaza los íconos emoji de los nav-item por SVG limpios. */
function _injectNavIcons() {
  document.querySelectorAll('.nav-item[href]').forEach(link => {
    const href = link.getAttribute('href') || '';
    // Extrae solo el nombre del archivo (sin ?params)
    const page = href.split('?')[0].split('/').pop();
    const iconSVG = _NAV_ICONS[page];
    if (!iconSVG) return;
    const iconSpan = link.querySelector('.nav-icon');
    if (iconSpan) iconSpan.innerHTML = iconSVG;
  });
}

/**
 * Inyecta etiquetas de sección (MENÚ / ADMINISTRACIÓN) en la sidebar.
 * Usa MutationObserver para mostrar la sección admin cuando corresponda.
 */
function _injectNavSectionLabels() {
  const nav = document.querySelector('.sidebar-nav');
  if (!nav) return;

  // ── Etiqueta MENÚ ────────────────────────────────────────────────────────
  const firstItem = nav.querySelector(
    '.nav-item:not(.nav-item-admin):not(.nav-item-super)'
  );
  if (firstItem && !nav.querySelector('.sidebar-menu-label')) {
    const lbl = document.createElement('div');
    lbl.className = 'sidebar-section-label sidebar-menu-label';
    lbl.textContent = 'MENÚ';
    nav.insertBefore(lbl, firstItem);
  }

  // ── Separador + etiqueta ADMINISTRACIÓN ─────────────────────────────────
  const firstAdmin = nav.querySelector('.nav-item-admin');
  if (firstAdmin && !nav.querySelector('.sidebar-admin-label')) {
    const sep = document.createElement('div');
    sep.className = 'sidebar-section-sep sidebar-admin-sep';

    const lbl = document.createElement('div');
    lbl.className = 'sidebar-section-label sidebar-admin-label';
    lbl.textContent = 'ADMINISTRACIÓN';

    nav.insertBefore(sep, firstAdmin);
    nav.insertBefore(lbl, firstAdmin);
  }

  // ── MutationObserver: muestra/oculta la sección admin ───────────────────
  const adminItems = nav.querySelectorAll('.nav-item-admin, .nav-item-super');
  if (!adminItems.length) return;

  function _syncAdminSection() {
    const anyVisible = nav.querySelector(
      '.nav-item-admin.visible, .nav-item-super.visible'
    );
    nav.querySelectorAll('.sidebar-admin-label, .sidebar-admin-sep').forEach(el => {
      el.classList.toggle('visible', !!anyVisible);
    });
  }

  const observer = new MutationObserver(_syncAdminSection);
  adminItems.forEach(el =>
    observer.observe(el, { attributes: true, attributeFilter: ['class'] })
  );
  _syncAdminSection(); // ejecutar una vez al inicio
}

/** Inyecta el botón de toggle de tema en la topnav si no existe ya. */
function _injectThemeToggle() {
  if (document.querySelector('.theme-toggle')) return;
  const right = document.querySelector('.topnav .topnav-right');
  if (!right) return;

  const btn = document.createElement('button');
  btn.className = 'icon-btn theme-toggle';
  btn.type = 'button';
  btn.title = 'Cambiar tema claro / oscuro';
  btn.setAttribute('aria-label', 'Cambiar tema');
  btn.innerHTML = `
    <svg class="icon-sun" fill="none" stroke="currentColor" stroke-width="2"
         stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="4"/>
      <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4
               M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>
    </svg>
    <svg class="icon-moon" fill="none" stroke="currentColor" stroke-width="2"
         stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
    </svg>`;
  btn.addEventListener('click', () => ThemeManager.toggle());

  // Insertar antes del botón de notificaciones
  const notif = right.querySelector('.notif-btn');
  if (notif) right.insertBefore(btn, notif);
  else       right.insertBefore(btn, right.firstChild);
}

// ─────────────────────────────────────────────────────────────────────────────
// BOOTSTRAP DOM
// ─────────────────────────────────────────────────────────────────────────────
function _boot() {
  _injectSidebarLogo();
  _injectNavIcons();
  _injectNavSectionLabels();
  _injectThemeToggle();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', _boot);
} else {
  _boot();
}
