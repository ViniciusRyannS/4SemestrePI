/* ===== Auth do CLIENTE (padronizado) =====
   Exponho window.ClienteAuth com: getAuth, isLogged, login, register,
   logout, me, changePassword, apiFetch
   + Helpers de checkout: selecionarEnderecoEntrega, selecionarFormaPagamento,
     finalizarCompra, buscarPedido
*/
(function(){
  const KEY = 'CLIENTE_AUTH';
  const API_BASE = '/api/clientes';

  function load(){
    try{
      const raw = localStorage.getItem(KEY);
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }
  function save(auth){
    auth?.token ? localStorage.setItem(KEY, JSON.stringify(auth))
                : localStorage.removeItem(KEY);
  }
  function clear(){ localStorage.removeItem(KEY); }

  async function apiFetch(url, opts={}){
    const auth = load();
    const headers = new Headers(opts.headers || {});
    if (auth?.token) headers.set('Authorization', 'Bearer ' + auth.token);
    return fetch(url, { ...opts, headers });
  }

  async function login(email, senha){
    const res = await fetch(`${API_BASE}/auth/login`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ email, senha })
    });
    if(!res.ok){
      const e = await res.json().catch(()=>({detail:'Usuário/senha inválidos'}));
      throw new Error(e.detail || 'Usuário/senha inválidos');
    }
    const data = await res.json();
    const auth = {
      token: data.token,
      expiresAt: data.expiresAt || data.expiraEm || null,
      nome: data.nome || '',
      email: data.email || email,
      tipo: data.tipo || 'CLIENTE'
    };
    save(auth);
    return auth;
  }

  async function register(payload){
    const res = await fetch(`${API_BASE}/auth/register`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){
      const e = await res.json().catch(()=>({detail:'Não foi possível registrar'}));
      throw new Error(e.detail || 'Não foi possível registrar');
    }
    return res.json().catch(()=>({ok:true}));
  }

  async function me(){
    const res = await apiFetch(`${API_BASE}/me`);
    if(!res.ok) throw new Error('Não autenticado');
    return res.json();
  }

  async function changePassword(senhaAtual, novaSenha){
    const res = await apiFetch(`${API_BASE}/me/senha`, {
      method:'PUT',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ senhaAtual, novaSenha })
    });
    if(!res.ok){
      const e = await res.json().catch(()=>({detail:'Falha ao alterar senha'}));
      throw new Error(e.detail || 'Falha ao alterar senha');
    }
    return true;
  }

  /** Define endereço de ENTREGA no carrinho (id de EnderecoCliente). */
  async function selecionarEnderecoEntrega(enderecoId){
    // endpoint correto no backend: POST /api/carrinho/endereco?enderecoId=...
    const res = await apiFetch(`/api/carrinho/endereco?enderecoId=${encodeURIComponent(enderecoId)}`, {
      method:'POST'
    });
    if(!res.ok){
      const e = await res.json().catch(()=>({detail:'Falha ao definir endereço de entrega'}));
      throw new Error(e.detail || 'Falha ao definir endereço de entrega');
    }
    return res.json().catch(()=>({ok:true}));
  }

  /** Seleciona forma de pagamento. Se for CARTAO, envia body JSON com dados/parcelas. */
  async function selecionarFormaPagamento(forma, dadosCartao){
    const f = String(forma||'').trim().toUpperCase();
    if(!f) throw new Error('Forma de pagamento é obrigatória');

    const opts = { method:'POST' };
    if(f === 'CARTAO'){
      const body = {
        numero:   dadosCartao?.numero   || '',
        nome:     dadosCartao?.nome     || '',
        validade: dadosCartao?.validade || '',
        cvv:      dadosCartao?.cvv      || '',
        parcelas: Number(dadosCartao?.parcelas || 0)
      };
      if(!body.parcelas) throw new Error('Informe as parcelas do cartão');
      opts.headers = {'Content-Type':'application/json'};
      opts.body = JSON.stringify(body);
    }

    const res = await apiFetch(`/api/carrinho/pagamento?forma=${encodeURIComponent(f)}`, opts);
    if(!res.ok){
      const e = await res.json().catch(()=>({detail:'Falha ao selecionar forma de pagamento'}));
      throw new Error(e.detail || 'Falha ao selecionar forma de pagamento');
    }
    return res.json().catch(()=>({ok:true}));
  }

  /** Finaliza compra: cria Pedido e retorna o recibo (com idPedido). */
  async function finalizarCompra(){
    const res = await apiFetch('/api/carrinho/finalizar', { method:'POST' });
    if(!res.ok){
      const msg = await res.text();
      throw new Error(msg || 'Falha ao finalizar compra');
    }
    // ReciboCompra: { idPedido, criadoEm, itens, modalidadeFrete, subtotal, frete, total, enderecoEntregaId, formaPagamento }
    return res.json();
  }

  /** Busca um pedido do cliente logado. */
  async function buscarPedido(id){
    const res = await apiFetch(`/api/clientes/pedidos/${encodeURIComponent(id)}`);
    if(res.status === 404) throw new Error('Pedido não encontrado');
    if(!res.ok){
      const txt = await res.text();
      throw new Error(txt || 'Falha ao carregar pedido');
    }
    return res.json();
  }

  function logout(){ clear(); }
  function getAuth(){ return load(); }
  function isLogged(){ return !!load()?.token; }

  window.ClienteAuth = {
    KEY, getAuth, isLogged, login, register, logout, me, changePassword, apiFetch,
    selecionarEnderecoEntrega, selecionarFormaPagamento, finalizarCompra, buscarPedido
  };
})();
