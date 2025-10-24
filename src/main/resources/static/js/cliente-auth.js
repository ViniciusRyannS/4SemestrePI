(function (global) {
  const STORAGE_KEY = 'CLIENTE_AUTH'; // <— Home procura por essa chave
  const API_BASE = '/api/clientes';

  function saveAuth(obj) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(obj || {}));
    } catch (e) { /* ignore */ }
  }
  function getAuth() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const o = JSON.parse(raw);
      if (o && o.token) return o;
      return null;
    } catch (e) { return null; }
  }
  function logout() {
    try { localStorage.removeItem(STORAGE_KEY); } catch (e) {}
  }
  function authHeader() {
    const a = getAuth();
    return a?.token ? { 'Authorization': 'Bearer ' + a.token } : {};
  }

  // ---- chamadas HTTP
  async function postJson(url, body) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(body || {})
    });
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch (e) {}
    if (!res.ok) {
      const msg = data?.detail || data?.message || 'Erro na requisição';
      throw new Error(msg);
    }
    return data;
  }
  async function putJson(url, body) {
    const res = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(body || {})
    });
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch (e) {}
    if (!res.ok) {
      const msg = data?.detail || data?.message || 'Erro na requisição';
      throw new Error(msg);
    }
    return data;
  }
  async function getJson(url) {
    const res = await fetch(url, { headers: { ...authHeader() } });
    const data = await res.json();
    if (!res.ok) {
      const msg = data?.detail || data?.message || 'Erro na requisição';
      throw new Error(msg);
    }
    return data;
  }

  // ---- API pública
  const ClienteAuth = {
    getAuth,
    saveAuth,
    logout,
    isLogged: () => !!getAuth()?.token,
    me: () => getJson(`${API_BASE}/me`),

    async login(email, senha) {
      const data = await postJson(`${API_BASE}/auth/login`, { email, senha });
      // Esperado: { token, expiresAt, nome, email, tipo }
      saveAuth(data);
      return data;
    },

    async register(dto) {
      // dto: {nomeCompleto,email,cpf,dataNascimento,genero,senha,confirmaSenha,fatCep,fatNumero,fatComplemento,entCep,entNumero,entComplemento}
      await postJson(`${API_BASE}/auth/register`, dto);
      // após cadastrar, NÂO loga automaticamente — volta para aba login
      return true;
    },

    async changePassword(senhaAtual, novaSenha) {
      await putJson(`${API_BASE}/me/senha`, { senhaAtual, novaSenha });
      return true;
    }
  };

  // ---- Util: obter returnUrl da querystring
  function getReturnUrl() {
    const p = new URLSearchParams(location.search);
    const ret = p.get('returnUrl');
    if (ret && /^\/[^\s]*$/.test(ret)) return ret; // só caminhos internos
    return '/';
  }

  // ---- inicialização da página cliente-login.html (liga botões/abas)
  function initLoginPage() {
    const $ = (id) => document.getElementById(id);

    const tabLogin = $('tabLogin');
    const tabReg   = $('tabReg');
    const viewLogin = $('viewLogin');
    const viewReg   = $('viewReg');
    const msg = $('msg');

    function showMsg(text, ok = true) {
      if (!msg) return;
      msg.textContent = text;
      msg.className = 'alert ' + (ok ? 'success' : 'error');
      msg.classList.remove('hidden');
      setTimeout(() => msg.classList.add('hidden'), 4000);
    }
    function switchTab(name) {
      if (!tabLogin || !tabReg || !viewLogin || !viewReg) return;
      const loginActive = name === 'login';
      tabLogin.classList.toggle('active', loginActive);
      tabReg.classList.toggle('active', !loginActive);
      viewLogin.classList.toggle('hidden', !loginActive);
      viewReg.classList.toggle('hidden', loginActive);
    }

    // Troca de abas
    if (tabLogin) tabLogin.addEventListener('click', () => switchTab('login'));
    if (tabReg) tabReg.addEventListener('click', () => switchTab('reg'));

    // ---- LOGIN
    const formLogin = $('formLogin');
    if (formLogin) {
      formLogin.addEventListener('submit', async (e) => {
        e.preventDefault(); // <— impede refresh
        const email = $('loginEmail')?.value?.trim();
        const senha = $('loginSenha')?.value?.trim();
        if (!email || !senha) {
          showMsg('Informe e-mail e senha.', false);
          return;
        }
        try {
          await ClienteAuth.login(email, senha);
          // redireciona para returnUrl OU home
          const ret = getReturnUrl();
          location.replace(ret);
        } catch (err) {
          showMsg(err.message || 'Falha no login.', false);
        }
      });
    }

    // ---- REGISTRO
    const formReg = $('formRegister');
    if (formReg) {
      formReg.addEventListener('submit', async (e) => {
        e.preventDefault(); // <— impede refresh
        const payload = {
          nomeCompleto: $('regNome')?.value?.trim(),
          email:        $('regEmail')?.value?.trim(),
          cpf:          $('regCpf')?.value?.trim(),
          dataNascimento: $('regNasc')?.value || null,
          genero:         $('regGenero')?.value || null,
          senha:          $('regSenha')?.value || '',
          confirmaSenha:  $('regConfSenha')?.value || '',
          fatCep:         $('fatCep')?.value?.trim(),
          fatNumero:      $('fatNumero')?.value?.trim(),
          fatComplemento: $('fatComp')?.value?.trim(),
          entCep:         $('entCep')?.value?.trim(),
          entNumero:      $('entNumero')?.value?.trim(),
          entComplemento: $('entComp')?.value?.trim()
        };
        try {
          await ClienteAuth.register(payload);
          showMsg('Cadastro realizado! Faça login para continuar.', true);
          switchTab('login'); // <— volta para a aba de login
          // opcional: foca e-mail
          $('loginEmail')?.focus();
        } catch (err) {
          showMsg(err.message || 'Não foi possível cadastrar.', false);
        }
      });
    }

    // ---- ESQUECI MINHA SENHA (form de troca por senha atual)
    const forgotLink = $('lnkForgot');
    const forgotBox  = $('forgotBox');
    const formForgot = $('formForgot');
    if (forgotLink && forgotBox) {
      forgotLink.addEventListener('click', (e) => {
        e.preventDefault();
        forgotBox.classList.toggle('hidden');
      });
    }
    if (formForgot) {
      formForgot.addEventListener('submit', async (e) => {
        e.preventDefault();
        const atual = $('fgSenhaAtual')?.value || '';
        const nova  = $('fgNovaSenha')?.value || '';
        const conf  = $('fgConfSenha')?.value || '';
        if (!atual || !nova) { showMsg('Informe as senhas.', false); return; }
        if (nova !== conf)   { showMsg('Senhas não conferem.', false); return; }
        try {
          await ClienteAuth.changePassword(atual, nova);
          showMsg('Senha alterada com sucesso!', true);
          forgotBox.classList.add('hidden');
          formForgot.reset();
        } catch (err) {
          showMsg(err.message || 'Não foi possível alterar a senha.', false);
        }
      });
    }

    // garante aba de login por padrão
    switchTab('login');
  }

  // expõe no global
  global.ClienteAuth = ClienteAuth;
  global.__initClienteLoginPage = initLoginPage;

})(window);