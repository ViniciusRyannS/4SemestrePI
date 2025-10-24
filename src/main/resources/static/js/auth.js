// /static/js/auth.js
const AUTH_KEY = 'auth.v1';

// Normaliza e salva {token, nome, grupo, email, expiresAt}
function setAuth(obj = {}) {
  const token     = obj.token     || obj.jwt || null;
  const nome      = obj.nome      || obj.name || null;
  const grupo     = obj.grupo     || obj.role || null; // aceita "role" mas salva como "grupo"
  const email     = obj.email     || null;
  const expiresAt = obj.expiresAt || null;

  localStorage.setItem(AUTH_KEY, JSON.stringify({ token, nome, grupo, email, expiresAt }));
}

function getAuth() {
  try { return JSON.parse(localStorage.getItem(AUTH_KEY) || 'null'); }
  catch { return null; }
}

function clearAuth() { localStorage.removeItem(AUTH_KEY); }
function isLogged()  { return !!(getAuth()?.token); }
function getGrupo()  { return getAuth()?.grupo || null; }
// compat
function getRole()   { return getGrupo(); }

function isAdmin() {
  const g = getGrupo();
  return g === 'ADMINISTRADOR' || g === 'ROLE_ADMIN' || g === 'ADMIN';
}

function authHeader() {
  const t = getAuth()?.token;
  return t ? { 'Authorization': 'Bearer ' + t } : {};
}

// Wrapper de fetch que injeta Authorization e trata 401
async function apiFetch(url, options = {}) {
  const headers = { ...(options.headers || {}), ...authHeader() };
  const res = await fetch(url, { ...options, headers });

  if (res.status === 401) {
    clearAuth();
    if (!location.pathname.endsWith('/login.html')) {
      const ret = encodeURIComponent(location.pathname + location.search);
      location.href = '/login.html?returnUrl=' + ret;
    }
    throw new Error('Não autorizado');
  }
  return res;
}

// expõe global
window.setAuth   = setAuth;
window.getAuth   = getAuth;
window.clearAuth = clearAuth;
window.isAdmin   = isAdmin;
window.isLogged  = isLogged;
window.apiFetch  = apiFetch;
window.getGrupo  = getGrupo;
window.getRole   = getRole;
