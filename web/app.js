let currentRole = 'admin'; // 'admin' o 'guest'
let isRegistered = false;
let globalProperties = []; // Para la barra de búsqueda
let globalClients = [];

document.addEventListener('DOMContentLoaded', () => {
    fetchData();
    setupNavigation();
    setupInteractions();
});

function setupNavigation() {
    const navItems = document.querySelectorAll('nav li');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const viewName = item.getAttribute('data-view');
            switchView(viewName);
            navItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

function setupInteractions() {
    // Selector de Rol
    const roleSwitcher = document.getElementById('role-switcher');
    roleSwitcher.addEventListener('change', (e) => {
        if (e.target.checked) {
            // Intentando entrar como admin -> Pedir clave
            document.getElementById('modal-admin-login').style.display = 'flex';
            document.getElementById('admin-pwd').focus();
        } else {
            // Saliendo a modo guest
            currentRole = 'guest';
            document.getElementById('role-label').textContent = 'Invitado';
            applyRoleRestrictions();
        }
    });

    // Verificación de Clave Admin a través de API
    document.getElementById('form-admin-login').onsubmit = async (e) => {
        e.preventDefault();
        const pwd = document.getElementById('admin-pwd').value;
        try {
            const res = await fetch(`/api/login?pwd=${encodeURIComponent(pwd)}`);
            const result = await res.json();
            if (result.status === 'ok') {
                currentRole = 'admin';
                document.getElementById('role-label').textContent = 'Administrador';
                document.getElementById('modal-admin-login').style.display = 'none';
                e.target.reset();
                applyRoleRestrictions();
            } else {
                alert(result.message || 'Clave incorrecta. Acceso denegado.');
                document.getElementById('admin-pwd').value = '';
            }
        } catch (err) {
            alert('Error validando clave: ' + err.message);
            document.getElementById('admin-pwd').value = '';
        }
    };

    document.getElementById('btn-cancel-login').onclick = () => {
        document.getElementById('modal-admin-login').style.display = 'none';
        roleSwitcher.checked = false;
    };

    document.getElementById('close-admin-login').onclick = () => {
        document.getElementById('modal-admin-login').style.display = 'none';
        roleSwitcher.checked = false;
    };

    // Registro de cliente
    const formRegister = document.getElementById('form-register');
    formRegister.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(formRegister);
        const params = new URLSearchParams(fd).toString();
        try {
            await fetch(`/api/clientes/register?${params}&tip=Invitado&pre=0&zon=Norte&itm=Generico`);
            isRegistered = true;
            document.getElementById('modal-register').style.display = 'none';
            fetchData();
        } catch (err) { alert('Error al registrar'); }
    });

    // Función auxiliar para leer archivos como Base64
    const fileToBase64 = (file) => new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });

    // Agregar Inmueble (Modal)
    document.getElementById('btn-add-prop').addEventListener('click', () => {
        document.getElementById('modal-add-prop').style.display = 'flex';
        document.getElementById('form-add-prop').reset();
        document.querySelector('#form-add-prop [name="cod"]').readOnly = false;
        document.getElementById('modal-add-prop').querySelector('h2').textContent = 'Agregar Nuevo Inmueble';
        document.querySelector('#form-add-prop button[type="submit"]').textContent = 'Registrar Propiedad';
    });

    document.getElementById('close-add-prop').addEventListener('click', () => {
        document.getElementById('modal-add-prop').style.display = 'none';
    });

    document.getElementById('form-add-prop').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const data = Object.fromEntries(fd.entries());
        
        // Procesar archivos locales
        const fileInput = document.getElementById('photo-file-input');
        const photoPromises = Array.from(fileInput.files).map(file => fileToBase64(file));
        const photosArray = await Promise.all(photoPromises);
        data.fts = photosArray.join('|SEP|'); // Nuevo separador para no romper el Base64

        try {
            const res = await fetch('/api/inmuebles/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const result = await res.json();
            if (result.status === 'ok') {
                document.getElementById('modal-add-prop').style.display = 'none';
                e.target.reset();
                fetchData();
            } else {
                alert('Error: ' + result.message);
            }
        } catch (err) { alert('Error al guardar: ' + err.message); }
    });

    // Control de posicionamiento de imagen
    const slider = document.getElementById('img-pos-slider');
    slider.oninput = (e) => {
        const mainImg = document.getElementById('main-detail-img');
        mainImg.style.backgroundPosition = `50% ${e.target.value}%`;
    };

    // Cerrar detalles
    document.querySelector('#modal-details .close-btn').onclick = () => {
        document.getElementById('modal-details').style.display = 'none';
    };

    // Agendar Cita (Modal)
    document.getElementById('btn-schedule-visit').onclick = () => {
        if (!isRegistered && currentRole === 'guest') {
            alert('Por favor regístrate primero.');
            document.getElementById('modal-register').style.display = 'flex';
            return;
        }
        document.getElementById('modal-schedule').style.display = 'flex';
    };

    document.getElementById('close-schedule').onclick = () => {
        document.getElementById('modal-schedule').style.display = 'none';
    };

    document.getElementById('form-schedule').onsubmit = async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        
        if (!fd.get('hor')) {
            alert('Por favor selecciona un horario.');
            return;
        }

        const params = new URLSearchParams(fd);
        
        // Agregar ID del cliente actual (simulado o del registro)
        const clienteId = document.querySelector('#form-register input[name="id"]').value || "C-DEMO";
        params.append('cli', clienteId);

        try {
            const res = await fetch(`/api/visitas/agendar?${params.toString()}`);
            const result = await res.json();
            if (result.status === 'ok') {
                alert('Cita agendada con éxito!');
                document.getElementById('modal-schedule').style.display = 'none';
                e.target.reset();
                document.getElementById('slots-container').innerHTML = '<p style="font-size: 0.8rem; color: var(--text-muted);">Selecciona una fecha para ver horarios.</p>';
            } else {
                alert('Error: ' + result.message);
            }
        } catch (err) { alert('Error al agendar cita'); }
    };

    // Lógica de Horarios (Slots)
    document.getElementById('schedule-date').onchange = async (e) => {
        const date = e.target.value;
        const propId = document.getElementById('schedule-cod').value;
        const container = document.getElementById('slots-container');
        
        container.innerHTML = '<p>Cargando disponibilidad...</p>';
        
        try {
            const res = await fetch(`/api/visitas/slots?cod=${propId}&fec=${date}`);
            const slots = await res.json();
            
            container.innerHTML = '';
            if (slots.length === 0) {
                container.innerHTML = '<p>No hay horarios disponibles para este día.</p>';
                return;
            }

            slots.forEach(s => {
                const btn = document.createElement('div');
                btn.className = 'slot-btn';
                btn.textContent = s;
                btn.onclick = () => {
                    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
                    btn.classList.add('selected');
                    document.getElementById('schedule-time').value = s;
                };
                container.appendChild(btn);
            });
        } catch (err) {
            container.innerHTML = '<p>Error al cargar horarios.</p>';
        }
    };

    // Barra de búsqueda global
    const searchInput = document.querySelector('.search-bar input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            
            // Si está vacío, mostrar todo
            if (!query) {
                renderProperties(globalProperties, 'featured-props');
                renderProperties(globalProperties, 'all-props-grid');
                renderClients(globalClients);
                return;
            }

            // Filtrar inmuebles
            const filteredProps = globalProperties.filter(p => 
                (p.codigo && p.codigo.toLowerCase().includes(query)) ||
                (p.direccion && p.direccion.toLowerCase().includes(query)) ||
                (p.ciudad && p.ciudad.toLowerCase().includes(query)) ||
                (p.zona && p.zona.toLowerCase().includes(query)) ||
                (p.tipo && p.tipo.toLowerCase().includes(query)) ||
                (p.estado && p.estado.toLowerCase().includes(query))
            );

            // Filtrar clientes
            const filteredClients = globalClients.filter(c => 
                (c.identificacion && c.identificacion.toLowerCase().includes(query)) ||
                (c.nombre && c.nombre.toLowerCase().includes(query)) ||
                (c.correo && c.correo.toLowerCase().includes(query))
            );

            renderProperties(filteredProps, 'featured-props');
            renderProperties(filteredProps, 'all-props-grid');
            renderClients(filteredClients);
        });
    }

    // Modal de Operaciones / Cierres
    const closeOp = document.getElementById('close-operacion');
    if (closeOp) {
        closeOp.onclick = () => document.getElementById('modal-operacion').style.display = 'none';
    }

    const formOp = document.getElementById('form-operacion');
    if (formOp) {
        formOp.onsubmit = async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            try {
                const params = new URLSearchParams(fd).toString();
                const res = await fetch(`/api/operaciones/add?${params}`);
                const result = await res.json();
                if (result.status === 'ok') {
                    alert('Operación registrada exitosamente');
                    document.getElementById('modal-operacion').style.display = 'none';
                    e.target.reset();
                    fetchData(); // Recargar todo
                } else {
                    alert('Error: ' + result.message);
                }
            } catch(err) { alert('Error de red al registrar operación'); }
        };
    }
}

function applyRoleRestrictions() {
    const adminElements = document.querySelectorAll('.admin-only');
    const deleteBtns = document.querySelectorAll('.delete-btn');
    const adminVisibleOnly = document.querySelectorAll('.admin-visible-only');
    const fabAdmin = document.getElementById('fab-admin');
    
    if (currentRole === 'admin') {
        adminElements.forEach(el => el.style.display = 'inline-block');
        if (fabAdmin) fabAdmin.style.display = 'flex';
        deleteBtns.forEach(el => el.classList.add('admin-visible'));
        adminVisibleOnly.forEach(el => el.style.display = 'flex');
        document.getElementById('modal-register').style.display = 'none';
        console.log("Admin mode active");
    } else {
        adminElements.forEach(el => el.style.display = 'none');
        if (fabAdmin) fabAdmin.style.display = 'none';
        deleteBtns.forEach(el => el.classList.remove('admin-visible'));
        adminVisibleOnly.forEach(el => el.style.display = 'none');
        if (!isRegistered) {
            document.getElementById('modal-register').style.display = 'flex';
        }
    }
}

async function fetchData() {
    try {
        const propRes = await fetch('/api/inmuebles');
        const properties = await propRes.json();
        globalProperties = properties; // Backup para búsqueda
        
        const clientRes = await fetch('/api/clientes');
        const clients = await clientRes.json();
        globalClients = clients; // Backup para búsqueda

        const analitRes = await fetch('/api/analitica');
        const analitics = await analitRes.json();

        renderStats(properties, clients, analitics);
        renderProperties(properties, 'featured-props');
        renderProperties(properties, 'all-props-grid');
        renderClients(clients);
        applyRoleRestrictions();
    } catch (err) {
        console.error('Error:', err);
    }
}

function switchView(viewName) {
    const views = document.querySelectorAll('.content-view');
    views.forEach(v => v.style.display = 'none');
    document.getElementById(`view-${viewName}`).style.display = 'block';
}

function renderStats(props, clients, analitics) {
    document.getElementById('stat-props').textContent = props.length;
    document.getElementById('stat-clients').textContent = clients.length;
    
    if (analitics) {
        const statOps = document.getElementById('stat-ops');
        if (statOps) statOps.textContent = `${analitics.operaciones} Cierres`;
        
        const aCard = document.getElementById('analytics-card');
        if (aCard) {
            aCard.innerHTML = `<h3>Estado del Sistema</h3>
            <p><strong>Total Inmuebles:</strong> ${analitics.inmuebles}</p>
            <p><strong>Total Clientes Registrados:</strong> ${analitics.clientes}</p>
            <p><strong>Operaciones Cerradas:</strong> ${analitics.operaciones}</p>
            <p style="color:var(--primary); margin-top:10px;">Óptimo (Basado en Grafos y BST)</p>`;
        }
    }
}

async function deleteProp(id) {
    if (confirm('¿Eliminar esta propiedad de todas las estructuras (BST/Hash)?')) {
        await fetch(`/api/inmuebles/delete?id=${id}`);
        fetchData();
    }
}

function renderProperties(props, containerId) {
    const grid = document.getElementById(containerId);
    if (!grid) return;
    grid.innerHTML = '';

    const backupPlaceholder = 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80';

    props.forEach((p) => {
        // Validación de imagen: si no hay fotos, o si la primera foto es demasiado corta para ser un objeto válido
        let coverImg = (p.fotos && p.fotos.length > 0 && p.fotos[0].length > 10) ? p.fotos[0] : backupPlaceholder;
        
        const card = document.createElement('div');
        card.className = 'property-card';
        card.onclick = () => showDetails(p);
        card.innerHTML = `
            <button class="delete-btn" onclick="event.stopPropagation(); deleteProp('${p.codigo}')">×</button>
            <div class="prop-img" style="background-image: url('${coverImg}')"></div>
            <div class="prop-info">
                <span class="details">${p.tipo} • ${p.zona}</span>
                <h4>${p.direccion}</h4>
                <p class="price">$${p.precio.toLocaleString()}</p>
                <div class="details">🛌 ${p.hab} Hab | 🚿 ${p.banos || p.ban} Baños</div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function showDetails(p) {
    const backupPlaceholder = 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80';
    // Filtrar fotos válidas
    const validPhotos = (p.fotos && p.fotos.length > 0) ? p.fotos.filter(f => f.length > 10) : [];
    const photos = validPhotos.length > 0 ? validPhotos : [backupPlaceholder];

    document.getElementById('detail-title').textContent = `${p.direccion} - ${p.ciudad}`;
    document.getElementById('detail-price').textContent = `$${p.precio.toLocaleString()}`;
    const mainImg = document.getElementById('main-detail-img');
    mainImg.style.backgroundImage = `url('${photos[0]}')`;
    mainImg.style.backgroundPosition = '50% 50%';
    document.getElementById('img-pos-slider').value = 50;
    
    const tags = document.getElementById('detail-tags');
    tags.innerHTML = `
        <span class="tag">${p.tipo}</span>
        <span class="tag tag-purpose">${p.finalidad}</span>
        <span class="tag tag-status">${p.estado}</span>
    `;

    const meta = document.getElementById('detail-meta');
    meta.innerHTML = `
        <div class="meta-item">🛌 <strong>${p.hab}</strong> Habitación(es)</div>
        <div class="meta-item">🚿 <strong>${p.banos}</strong> Baño(s)</div>
        <div class="meta-item">🏗️ <strong>${p.area} m²</strong> Construidos</div>
        <div class="meta-item">🌳 <strong>${p.areaT} m²</strong> Terreno</div>
        <div class="meta-item">📍 <strong>${p.zona}</strong></div>
    `;

    const contact = document.getElementById('detail-contact');
    if (p.asesor) {
        contact.innerHTML = `<strong>${p.asesor.nombre}</strong><br>📞 ${p.asesor.contacto}`;
    } else {
        contact.innerHTML = `<strong>Departamento de Ventas</strong><br>📞 (601) 123-4567`;
    }

    document.getElementById('schedule-cod').value = p.codigo;
    // Renderizar miniaturas reales
    const thumbs = document.getElementById('thumb-grid');
    thumbs.innerHTML = '';
    photos.forEach((url, index) => {
        const t = document.createElement('div');
        t.className = 'thumb';
        t.style.backgroundImage = `url('${url}')`;
        t.onclick = (e) => {
            e.stopPropagation();
            mainImg.style.backgroundImage = `url('${url}')`;
        };
        
        // Botón de eliminar foto (solo admin)
        if (currentRole === 'admin') {
            const delBtn = document.createElement('button');
            delBtn.className = 'delete-photo-btn';
            delBtn.innerHTML = '×';
            delBtn.onclick = (e) => {
                e.stopPropagation();
                if (confirm('¿Eliminar esta foto permanentemente?')) {
                    deletePhoto(p, index);
                }
            };
            t.appendChild(delBtn);
        }
        
        thumbs.appendChild(t);
    });
    
    // Lógica del Botón Editar
    const btnEdit = document.getElementById('btn-edit-prop');
    if (btnEdit) {
        btnEdit.onclick = () => {
            document.getElementById('modal-details').style.display = 'none';
            document.getElementById('modal-add-prop').style.display = 'flex';
            document.getElementById('modal-add-prop').querySelector('h2').textContent = 'Editar Inmueble';
            
            const f = document.getElementById('form-add-prop');
            f.elements['cod'].value = p.codigo || '';
            f.elements['cod'].readOnly = true; // Para no cambiar el identificador primario
            f.elements['dir'].value = p.direccion || '';
            f.elements['ciu'].value = p.ciudad || 'Bogotá';
            f.elements['tip'].value = p.tipo || '';
            f.elements['pre'].value = p.precio || '';
            f.elements['zon'].value = p.zona || '';
            f.elements['fin'].value = p.finalidad || 'Venta';
            f.elements['are'].value = p.area || '';
            f.elements['areT'].value = p.areaT || p.area || '';
            f.elements['hab'].value = p.hab || '';
            f.elements['ban'].value = p.banos || p.ban || '';
            f.elements['est'].value = p.estado || 'Disponible';
            f.querySelector('button[type="submit"]').textContent = 'Guardar Cambios';
        };
    }

    // Lógica del Botón Cerrar Negocio
    const btnCerrar = document.getElementById('btn-change-status');
    if (btnCerrar) {
        btnCerrar.onclick = () => {
            document.getElementById('modal-details').style.display = 'none';
            document.getElementById('modal-operacion').style.display = 'flex';
            document.getElementById('op-cod').value = p.codigo;
            document.getElementById('op-inmueble-info').value = `${p.codigo} - ${p.direccion}`;
            document.getElementById('op-valor').value = p.precio;
            
            const sel = document.getElementById('op-cliente-select');
            sel.innerHTML = '<option value="" disabled selected>Selecciona el cliente...</option>';
            globalClients.forEach(c => {
                sel.innerHTML += `<option value="${c.identificacion}">${c.nombre} (${c.identificacion})</option>`;
            });
        };
    }

    document.getElementById('modal-details').style.display = 'flex';
}

async function deletePhoto(property, index) {
    property.fotos.splice(index, 1);
    
    // Preparar objeto para el servidor (campos con nombres que espera el backend)
    const data = {
        cod: property.codigo,
        dir: property.direccion,
        tip: property.tipo,
        pre: property.precio,
        zon: property.zona,
        hab: property.hab,
        ban: property.banos || property.ban,
        fts: property.fotos.join('|SEP|'),
        ciu: property.ciudad || "Bogotá",
        fin: property.finalidad || "Venta",
        are: property.area || "100",
        est: property.estado || "Disponible"
    };

    try {
        const res = await fetch('/api/inmuebles/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (result.status === 'ok') {
            showDetails(property); // Refrescar vista
            fetchData(); // Refrescar grilla
        } else {
            alert('Error al borrar foto: ' + result.message);
        }
    } catch (err) { console.error(err); }
}

function renderClients(clients) {
    const container = document.getElementById('clients-list');
    if (!container) return;
    container.innerHTML = '';
    clients.forEach(c => {
        const card = document.createElement('div');
        card.className = 'stat-card';
        card.innerHTML = `<h3>CLIENTE</h3><p>${c.nombre}</p><div class="details">ID: ${c.id} | ${c.tipo}</div>`;
        container.appendChild(card);
    });
}
