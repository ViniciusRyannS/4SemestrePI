const AUTH_KEY = 'auth.v1';


function setAuth(obj = {}) {
  const token     = obj.token     || obj.jwt || null;
  const nome      = obj.nome      || obj.name || null;
  const grupo     = obj.grupo     || obj.role || null; 
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
function getRole()   { return getGrupo(); }

function isAdmin() {
  const g = getGrupo();
  return g === 'ADMINISTRADOR' || g === 'ROLE_ADMIN' || g === 'ADMIN';
}

function authHeader() {
  const t = getAuth()?.token;
  return t ? { 'Authorization': 'Bearer ' + t } : {};
}

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

window.setAuth   = setAuth;
window.getAuth   = getAuth;
window.clearAuth = clearAuth;
window.isAdmin   = isAdmin;
window.isLogged  = isLogged;
window.apiFetch  = apiFetch;
window.getGrupo  = getGrupo;
window.getRole   = getRole;
