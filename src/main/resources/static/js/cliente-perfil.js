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
    const me = await ClienteAuth.me();
    document.getElementById('nomeCompleto').value = me.nomeCompleto || '';
    document.getElementById('email').value = me.email || '';
    document.getElementById('genero').value = me.genero || '';
    document.getElementById('dataNascimento').value = me.dataNascimento || '';
  }
  formPerfil.addEventListener('submit', async (e)=>{
    e.preventDefault();
    try{
      const payload = {
        nomeCompleto: document.getElementById('nomeCompleto').value.trim(),
        genero: document.getElementById('genero').value || null,
        dataNascimento: document.getElementById('dataNascimento').value || null,
      };
      const resp = await fetch('http://localhost:8080/api/clientes/me', {
        method:'PUT',
        headers:{ 'Content-Type':'application/json', ...ClienteAuth.authHeader() },
        body: JSON.stringify(payload)
      });
      if(!resp.ok) throw await resp.json().catch(()=>({detail:'Erro ao salvar'}));
      showMsg('Perfil atualizado!', true);
      await loadPerfil();
    }catch(err){ showMsg(err.detail || 'Falha ao salvar'); }
  });

  // ===== ENDEREÇOS =====
  const listaEnd = document.getElementById('listaEnd');
  const modalEnd = document.getElementById('modalEnd');
  document.getElementById('btnNovoEnd').addEventListener('click', ()=>modalEnd.classList.remove('hidden'));
  document.getElementById('btnCancelEnd').addEventListener('click', ()=>modalEnd.classList.add('hidden'));

  async function loadEnderecos(){
    const resp = await fetch('http://localhost:8080/api/clientes/enderecos', { headers: { ...ClienteAuth.authHeader() }});
    if(!resp.ok){ listaEnd.innerHTML = '<div class="item">Erro ao carregar endereços</div>'; return; }
    const arr = await resp.json();
    if(!arr.length){ listaEnd.innerHTML = '<div class="item"><span class="muted">Nenhum endereço cadastrado.</span></div>'; return; }
    listaEnd.innerHTML = arr.map(e=>`
      <div class="item">
        <div>
          <div><strong>${e.logradouro}, ${e.numero}</strong> ${e.complemento?`- ${e.complemento}`:''}</div>
          <div class="muted">${e.bairro} • ${e.cidade}/${e.uf} • CEP ${e.cep}</div>
        </div>
        <span class="tag ${e.padrao && e.tipo==='ENTREGA' ? 'primary':''}">
          ${e.tipo}${e.padrao && e.tipo==='ENTREGA' ? ' • Padrão':''}
        </span>
        <div style="display:flex;gap:6px">
          ${e.tipo==='ENTREGA' ? `<button class="btn secondary" onclick="setPadrao(${e.id})">Definir padrão</button>`:''}
          <button class="btn danger" onclick="removerEnd(${e.id})">Remover</button>
        </div>
      </div>
    `).join('');
  }

  window.setPadrao = async (id)=>{
    const resp = await fetch(`http://localhost:8080/api/clientes/enderecos/${id}/padrao?padrao=true`, {
      method:'PATCH', headers:{ ...ClienteAuth.authHeader() }
    });
    if(!resp.ok){ showMsg('Não foi possível definir padrão'); return; }
    showMsg('Endereço definido como padrão!', true);
    loadEnderecos();
  };
  window.removerEnd = async (id)=>{
    const resp = await fetch(`http://localhost:8080/api/clientes/enderecos/${id}`, {
      method:'DELETE', headers:{ ...ClienteAuth.authHeader() }
    });
    if(!resp.ok){ showMsg('Falha ao remover endereço'); return; }
    loadEnderecos();
  };

  // criar endereço
  document.getElementById('formEnd').addEventListener('submit', async (e)=>{
    e.preventDefault();
    try{
      const payload = {
        tipo: document.getElementById('tipo').value,
        cep: document.getElementById('cep').value.replace(/\D/g,''),
        numero: document.getElementById('numero').value.trim(),
        complemento: document.getElementById('complemento').value.trim() || null,
        padrao: document.getElementById('padrao').checked
      };
      const resp = await fetch('http://localhost:8080/api/clientes/enderecos', {
        method:'POST',
        headers:{ 'Content-Type':'application/json', ...ClienteAuth.authHeader() },
        body: JSON.stringify(payload)
      });
      if(!resp.ok) throw await resp.json().catch(()=>({detail:'Erro ao salvar endereço'}));
      showMsg('Endereço adicionado!', true);
      modalEnd.classList.add('hidden');
      e.target.reset();
      loadEnderecos();
    }catch(err){ showMsg(err.detail || 'Falha ao adicionar endereço'); }
  });

  // bootstrap
  (async ()=>{
    if(!requireLogin()) return;
    await loadPerfil();
    await loadEnderecos();
  })();

})();
