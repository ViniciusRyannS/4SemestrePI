(function(){
  const msg = document.getElementById('msg');
  const viewPerfil = document.getElementById('viewPerfil');
  const viewEnd = document.getElementById('viewEnd');
  const tabPerfil = document.getElementById('tabPerfil');
  const tabEnd = document.getElementById('tabEnd');

  function showMsg(text, ok=false){
    msg.className = 'alert ' + (ok ? 'success':'error');
    msg.textContent = text;
    msg.classList.remove('hidden');
    setTimeout(()=>msg.classList.add('hidden'), 5000);
  }
  function requireLogin(){
    if(!ClienteAuth.isLogged()){
      location.href = 'cliente-login.html?returnUrl=' + encodeURIComponent(location.pathname);
      return false;
    }
    return true;
  }
  function activate(tab){
    [tabPerfil, tabEnd].forEach(t => t.classList.remove('active'));
    [viewPerfil, viewEnd].forEach(v => v.classList.add('hidden'));
    if (tab === 'perfil'){ tabPerfil.classList.add('active'); viewPerfil.classList.remove('hidden'); }
    else { tabEnd.classList.add('active'); viewEnd.classList.remove('hidden'); }
  }

  tabPerfil.addEventListener('click', ()=>activate('perfil'));
  tabEnd.addEventListener('click', ()=>activate('end'));

  // ===== PERFIL =====
  const formPerfil = document.getElementById('formPerfil');
  async function loadPerfil(){
    try{
      const me = await ClienteAuth.me();
      document.getElementById('nomeCompleto').value   = me.nomeCompleto || '';
      document.getElementById('email').value          = me.email || '';
      document.getElementById('genero').value         = me.genero || '';
      document.getElementById('dataNascimento').value = me.dataNascimento || '';
    }catch(err){
      console.error('[perfil] erro loadPerfil', err);
      showMsg('Não foi possível carregar o perfil');
    }
  }
  formPerfil.addEventListener('submit', async (e)=>{
    e.preventDefault();
    try{
      const payload = {
        nomeCompleto: document.getElementById('nomeCompleto').value.trim(),
        genero: document.getElementById('genero').value || null,
        dataNascimento: document.getElementById('dataNascimento').value || null,
      };
      const resp = await fetch('/api/clientes/me', {
        method:'PUT',
        headers:{ 'Content-Type':'application/json', ...ClienteAuth.authHeader() },
        body: JSON.stringify(payload)
      });
      if(!resp.ok) throw await resp.json().catch(()=>({detail:'Erro ao salvar'}));
      showMsg('Perfil atualizado!', true);
      await loadPerfil();
    }catch(err){
      console.error('[perfil] erro salvar', err);
      showMsg(err.detail || 'Falha ao salvar');
    }
  });

  // ===== ENDEREÇOS =====
  const listaEnd  = document.getElementById('listaEnd');
  const modalEnd  = document.getElementById('modalEnd');
  const formEnd   = document.getElementById('formEnd');
  const tipoSel   = document.getElementById('tipo');
  const chkPadrao = document.getElementById('padrao');

  // cache local dos endereços
  let enderecosCache = [];

  function renderEnderecos(arr){
    enderecosCache = Array.isArray(arr) ? arr.slice() : [];
    if(!enderecosCache.length){
      listaEnd.innerHTML = '<div class="item"><span class="muted">Nenhum endereço cadastrado.</span></div>';
      return;
    }
    listaEnd.innerHTML = enderecosCache.map(e=>`
      <div class="item">
        <div>
          <div><strong>${e.tipo || ''}</strong> — ${e.logradouro || ''}${e.logradouro ? ', ' : ''}${e.numero || ''}${e.complemento?` - ${e.complemento}`:''}</div>
          <div class="muted">${e.bairro || ''} • ${e.cidade || ''}/${e.uf || ''} • CEP ${e.cep || ''}</div>
        </div>
        <span class="tag ${e.padrao && e.tipo==='ENTREGA' ? 'primary':''}">
          ${e.padrao && e.tipo==='ENTREGA' ? 'padrão' : '—'}
        </span>
        <div style="display:flex;gap:6px">
          ${e.tipo==='ENTREGA' && !e.padrao ? `<button class="btn secondary btn-def" data-id="${e.id}">Tornar padrão</button>`:''}
          <button class="btn danger btn-del" data-id="${e.id}">Remover</button>
        </div>
      </div>
    `).join('');

    // bind actions
    listaEnd.querySelectorAll('.btn-def').forEach(btn=>{
      btn.addEventListener('click', async ()=>{
        const id = btn.getAttribute('data-id');
        try{
          const resp = await fetch(`/api/clientes/enderecos/${id}/padrao?padrao=true`, {
            method:'PATCH', headers:{ ...ClienteAuth.authHeader() }
          });
          if(!resp.ok){
            const e = await resp.json().catch(()=>({detail:'Falha ao definir padrão'}));
            throw new Error(e.detail);
          }
          showMsg('Endereço definido como padrão!', true);
          await loadEnderecos(); // sincroniza
        }catch(err){
          console.error('[enderecos] setPadrao', err);
          showMsg(err.message || 'Não foi possível definir padrão');
        }
      });
    });
    listaEnd.querySelectorAll('.btn-del').forEach(btn=>{
      btn.addEventListener('click', async ()=>{
        const id = btn.getAttribute('data-id');
        if(!confirm('Remover este endereço?')) return;
        try{
          const resp = await fetch(`/api/clientes/enderecos/${id}`, {
            method:'DELETE', headers:{ ...ClienteAuth.authHeader() }
          });
          if(!resp.ok){
            const e = await resp.json().catch(()=>({detail:'Falha ao remover'}));
            throw new Error(e.detail);
          }
          // remove otimista
          renderEnderecos(enderecosCache.filter(x=>String(x.id)!==String(id)));
          showMsg('Endereço removido.', true);
          // e ainda sincroniza com o backend
          await loadEnderecos();
        }catch(err){
          console.error('[enderecos] remover', err);
          showMsg(err.message || 'Não foi possível remover endereço');
        }
      });
    });
  }

  async function loadEnderecos(){
    try{
      const resp = await fetch(`/api/clientes/enderecos?v=${Date.now()}`, {
        headers: { ...ClienteAuth.authHeader() }
      });
      console.log('[enderecos] GET status', resp.status);
      if(!resp.ok){
        listaEnd.innerHTML = '<div class="item">Erro ao carregar endereços</div>';
        return;
      }
      const arr = await resp.json();
      console.log('[enderecos] GET payload', arr);
      renderEnderecos(Array.isArray(arr) ? arr : []);
    }catch(err){
      console.error('[enderecos] loadEnderecos', err);
      listaEnd.innerHTML = '<div class="item">Erro ao listar endereços.</div>';
      showMsg('Erro ao listar endereços');
    }
  }

  // Habilita "padrão" somente para ENTREGA
  function syncPadraoToggle(){
    const enable = (tipoSel.value === 'ENTREGA');
    chkPadrao.disabled = !enable;
    if (!enable) chkPadrao.checked = false;
  }
  tipoSel.addEventListener('change', syncPadraoToggle);
  syncPadraoToggle();

  document.getElementById('btnNovoEnd').addEventListener('click', ()=>{
    formEnd.reset();
    syncPadraoToggle();
    modalEnd.classList.remove('hidden');
  });
  document.getElementById('btnCancelEnd').addEventListener('click', ()=>{
    modalEnd.classList.add('hidden');
  });

  // criar endereço (POST)
  formEnd.addEventListener('submit', async (e)=>{
    e.preventDefault();
    try{
      const tipo   = document.getElementById('tipo').value;
      const cep    = document.getElementById('cep').value.replace(/\D/g,'');
      const numero = document.getElementById('numero').value.trim();
      const complemento = document.getElementById('complemento').value.trim() || null;
      const padrao = (document.getElementById('padrao').checked && tipo === 'ENTREGA');

      if(cep.length !== 8){ showMsg('CEP deve ter 8 dígitos'); return; }
      if(!numero){ showMsg('Informe o número'); return; }

      const payload = { tipo, cep, numero, complemento, padrao };
      console.log('[enderecos] POST payload', payload);

      const resp = await fetch('/api/clientes/enderecos', {
        method:'POST',
        headers:{ 'Content-Type':'application/json', ...ClienteAuth.authHeader() },
        body: JSON.stringify(payload)
      });
      const maybeJson = await resp.json().catch(()=>null);
      console.log('[enderecos] POST status', resp.status, 'body', maybeJson);

      if(!resp.ok){
        const detail = (maybeJson && (maybeJson.detail || maybeJson.message)) || 'Erro ao salvar endereço';
        throw new Error(detail);
      }

      // Otimista: se o backend devolveu o endereço criado, injeta na lista
      if (maybeJson && typeof maybeJson === 'object' && (maybeJson.id || maybeJson.cep)) {
        const novo = maybeJson;
        // normaliza minimos
        if(!novo.tipo) novo.tipo = tipo;
        renderEnderecos([novo, ...enderecosCache]); // coloca no topo
      }

      showMsg('Endereço adicionado!', true);
      modalEnd.classList.add('hidden');
      e.target.reset();
      syncPadraoToggle();

      // E sincroniza com o servidor para garantir consistência
      await loadEnderecos();
    }catch(err){
      console.error('[enderecos] POST erro', err);
      showMsg(err.message || 'Falha ao adicionar endereço');
    }
  });

  // bootstrap
  (async ()=>{
    if(!requireLogin()) return;
    await loadPerfil();
    await loadEnderecos();
  })();

})();
