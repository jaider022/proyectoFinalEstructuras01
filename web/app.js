let currentRole = 'guest'; // 'admin', 'asesor' o 'guest'
let isRegistered = false;
let currentClientId = null; 
let currentClientFavorites = []; // IDs de inmuebles favoritos
let globalProperties = []; // Para la barra de búsqueda
let globalClients = [];
let lightboxPhotos = [];
let lightboxCurrentIndex = 0;

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
    // ==========================================
    // LÓGICA DE LANDING PAGE
    // ==========================================
    const landingView = document.getElementById('landing-view');
    const appContainer = document.getElementById('app-container');

    // Los botones de landing ahora funcionan por atributo onclick en HTML para mayor seguridad.
    // Pero necesitamos una función para que el invitado entre a la app:
    window.enterAsGuest = () => {
        landingView.style.display = 'none';
        appContainer.style.display = 'flex';
        currentRole = 'guest';
        applyRoleRestrictions();
        switchView('dashboard');
    };

    // Botones del Header Interno
    const btnReg = document.getElementById('btn-header-register');
    if(btnReg) btnReg.onclick = () => {
        document.getElementById('modal-register').style.display = 'flex';
    };
    
    const btnLog = document.getElementById('btn-header-login');
    if(btnLog) btnLog.onclick = () => {
        document.getElementById('modal-login').style.display = 'flex';
    };
    
    const btnLogout = document.getElementById('btn-header-logout');
    if (btnLogout) {
        btnLogout.onclick = () => {
            currentRole = 'guest';
            currentClientId = null;
            isRegistered = false;
            fetchData();
            switchView('dashboard');
            document.getElementById('landing-view').style.display = 'flex';
            document.getElementById('app-container').style.display = 'none';
            applyRoleRestrictions();
        };
    }
    
    // Cerrar modal de registro manual
    const closeReg = document.getElementById('close-register');
    if(closeReg) closeReg.onclick = () => {
        document.getElementById('modal-register').style.display = 'none';
    };

    // Volver a la Landing Page desde el Logo interior
    const innerLogo = document.querySelector('.sidebar .logo');
    if(innerLogo) {
        innerLogo.style.cursor = 'pointer';
        innerLogo.onclick = () => {
            currentRole = 'guest';
            applyRoleRestrictions();
            document.getElementById('landing-view').style.display = 'flex';
            document.getElementById('app-container').style.display = 'none';
        };
    }

    // MODAL LOGIN UNIFICADO
    const formUnifiedLogin = document.getElementById('form-unified-login');
    if (formUnifiedLogin) {
        formUnifiedLogin.onsubmit = async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const id = fd.get('id');
            const pwd = fd.get('pwd');

            try {
                const res = await fetch(`/api/login/unified?id=${encodeURIComponent(id)}&pwd=${encodeURIComponent(pwd)}`);
                const data = await res.json();

                if (data.status === 'ok') {
                    currentRole = data.role;
                    currentClientId = (data.role === 'cliente' || data.role === 'asesor') ? data.id : null;
                    isRegistered = (data.role === 'cliente');
                    
                    document.getElementById('modal-login').style.display = 'none';
                    
                    await fetchData();
                    
                    if (currentRole === 'admin') {
                        switchView('dashboard');
                    } else if (currentRole === 'asesor') {
                        switchView('asesor-dashboard');
                    } else if (currentRole === 'cliente') {
                        switchView('dashboard'); // Ir al catálogo principal
                    }
                    
                    // Actualizar UI
                    document.getElementById('landing-view').style.display = 'none';
                    document.getElementById('app-container').style.display = 'flex';
                    applyRoleRestrictions();
                    
                    alert(`Bienvenido, acceso como ${currentRole.toUpperCase()}`);
                } else {
                    alert(data.message || "Error al iniciar sesión");
                }
            } catch (err) {
                console.error("Login error:", err);
                alert("Error de conexión con el servidor");
            }
        };
    }

    // Cerrar modal de login
    const closeLogin = document.getElementById('close-login');
    if(closeLogin) {
        closeLogin.onclick = () => {
            document.getElementById('modal-login').style.display = 'none';
        };
    }


    const formRegister = document.getElementById('form-register');
    formRegister.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(formRegister);
        const params = new URLSearchParams(fd).toString();
        currentClientId = fd.get('id');
        try {
            console.log("Enviando registro para:", currentClientId);
            const res = await fetch(`/api/clientes/register?${params}&tip=Invitado&pre=0&zon=Norte&itm=Generico`);
            if (!res.ok) throw new Error("Error en el servidor al registrar");
            
            isRegistered = true;
            currentRole = 'cliente'; // Promocionar a cliente automáticamente
            document.getElementById('modal-register').style.display = 'none';
            await fetchData();
            switchView('dashboard');
        } catch (err) {
            console.error("Error en registro:", err);
            alert("No se pudo completar el registro: " + err.message);
        }
    });

    // Botones UNDO
    document.getElementById('btn-undo-snap').onclick = async () => {
        const res = await fetch('/api/undo/snapshot');
        const data = await res.json();
        alert(data.message);
        fetchData();
    };

    document.getElementById('btn-undo-admin').onclick = async () => {
        const res = await fetch('/api/undo/admin');
        const data = await res.json();
        alert(data.message);
        fetchData();
    };

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
        
        // Resetear campo personalizado
        const ct = document.getElementById('custom-tip');
        if(ct) {
            ct.style.display = 'none';
            ct.required = false;
        }

        document.getElementById('prop-is-update').value = 'false';
        document.querySelector('#form-add-prop [name="cod"]').readOnly = false;
        document.getElementById('modal-add-prop').querySelector('h2').textContent = 'Agregar Nuevo Inmueble';
        document.querySelector('#form-add-prop button[type="submit"]').textContent = 'Registrar Propiedad';
    });

    document.getElementById('close-add-prop').addEventListener('click', () => {
        document.getElementById('modal-add-prop').style.display = 'none';
    });

    const propTipSelect = document.getElementById('prop-tip-select');
    const customTipInput = document.getElementById('custom-tip');
    if (propTipSelect && customTipInput) {
        propTipSelect.addEventListener('change', (e) => {
            if (e.target.value === 'Otro') {
                customTipInput.style.display = 'block';
                customTipInput.required = true;
            } else {
                customTipInput.style.display = 'none';
                customTipInput.required = false;
            }
        });
    }

    document.getElementById('form-add-prop').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const data = Object.fromEntries(fd.entries());
        
        // Si el tipo es 'Otro', usar el valor del campo personalizado
        if (data.tip === 'Otro') {
            const customTip = document.getElementById('custom-tip').value;
            if (customTip) data.tip = customTip;
        }
        
        // Procesar archivos locales (nuevas fotos)
        const fileInput = document.getElementById('photo-file-input');
        const photoPromises = Array.from(fileInput.files).map(file => fileToBase64(file));
        const newPhotosArray = await Promise.all(photoPromises);

        if (newPhotosArray.length > 0) {
            // Si se seleccionaron fotos nuevas, usarlas
            data.fts = newPhotosArray.join('|SEP|');
        } else {
            // *** BUG FIX: si NO hay fotos nuevas, preservar las fotos existentes ***
            const existingPhotos = document.getElementById('existing-photos');
            data.fts = (existingPhotos && existingPhotos.value) ? existingPhotos.value : '';
        }

        // Eliminar el campo auxiliar antes de enviar al servidor
        delete data['existing-photos'];

        try {
            const res = await fetch('/api/inmuebles/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const result = await res.json();
            if (result.status === 'ok') {
                document.getElementById('modal-add-prop').style.display = 'none';
                // Limpiar el campo de fotos existentes para la próxima vez
                document.getElementById('existing-photos').value = '';
                document.getElementById('existing-photos-preview').style.display = 'none';
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

    // Cerrar lightbox
    const closeLightboxBtn = document.getElementById('close-lightbox');
    if (closeLightboxBtn) {
        closeLightboxBtn.onclick = () => {
            document.getElementById('modal-lightbox').style.display = 'none';
        };
    }
    
    // Flechas lightbox
    const prevLightboxBtn = document.getElementById('lightbox-prev');
    if (prevLightboxBtn) {
        prevLightboxBtn.onclick = () => {
            if (lightboxPhotos.length > 0) {
                lightboxCurrentIndex = (lightboxCurrentIndex - 1 + lightboxPhotos.length) % lightboxPhotos.length;
                document.getElementById('lightbox-img').src = lightboxPhotos[lightboxCurrentIndex];
            }
        };
    }
    const nextLightboxBtn = document.getElementById('lightbox-next');
    if (nextLightboxBtn) {
        nextLightboxBtn.onclick = () => {
            if (lightboxPhotos.length > 0) {
                lightboxCurrentIndex = (lightboxCurrentIndex + 1) % lightboxPhotos.length;
                document.getElementById('lightbox-img').src = lightboxPhotos[lightboxCurrentIndex];
            }
        };
    }

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
        
        // Agregar ID del cliente actual
        const clienteId = currentClientId || document.querySelector('#form-register input[name="id"]').value || "C-DEMO";
        params.append('cli', clienteId);

        try {
            console.log("Agendando cita para cliente:", clienteId, "Inmueble:", fd.get('cod'));
            const res = await fetch(`/api/visitas/agendar?${params.toString()}`);
            const result = await res.json();
            if (result.status === 'ok') {
                const propCode = fd.get('cod');
                const property = globalProperties.find(p => p.codigo === propCode);
                const asesorNombre = (property && property.asesor && property.asesor.nombre !== 'null') ? property.asesor.nombre : 'nuestro equipo de ventas';
                
                alert(`¡Cita agendada con éxito!\n\nSerás atendido por el asesor: ${asesorNombre}. ¡Te esperamos!`);
                document.getElementById('modal-schedule').style.display = 'none';
                e.target.reset();
                document.getElementById('slots-container').innerHTML = '<p style="font-size: 0.8rem; color: var(--text-muted);">Selecciona una fecha para ver horarios.</p>';
                
                // Actualizar panel de cliente si está activo
                if (currentRole === 'cliente') {
                    fetchClienteData();
                }
            } else {
                alert('No se pudo agendar: ' + (result.message || 'Error desconocido'));
            }
        } catch (err) { 
            console.error("Error al agendar:", err);
            alert('Error al conectar con el servidor para agendar cita'); 
        }
    };

    // Asesores (Modal)
    const btnAddAsesor = document.getElementById('btn-add-asesor');
    if (btnAddAsesor) {
        btnAddAsesor.onclick = () => {
            document.getElementById('modal-add-asesor').style.display = 'flex';
        };
    }
    
    const closeAddAsesor = document.getElementById('close-add-asesor');
    if (closeAddAsesor) {
        closeAddAsesor.onclick = () => {
            document.getElementById('modal-add-asesor').style.display = 'none';
        };
    }

    const formAddAsesor = document.getElementById('form-add-asesor');
    if (formAddAsesor) {
        formAddAsesor.onsubmit = async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const data = Object.fromEntries(fd.entries());
            try {
                const res = await fetch('/api/asesores/add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                const result = await res.json();
                if (result.status === 'ok') {
                    document.getElementById('modal-add-asesor').style.display = 'none';
                    e.target.reset();
                    fetchAsesores(); // Recargar asesores
                } else {
                    alert('Error: ' + result.message);
                }
            } catch (err) { alert('Error al guardar asesor: ' + err.message); }
        };
    }

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
                (p.disponibilidad && p.disponibilidad.toLowerCase().includes(query))
            );

            // Filtrar clientes
            const filteredClients = globalClients.filter(c => 
                (c.id && c.id.toLowerCase().includes(query)) ||
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

    // Modal de Filtros Avanzados
    const btnFilters = document.getElementById('btn-advanced-filters');
    if (btnFilters) {
        btnFilters.onclick = () => {
            document.getElementById('modal-filtros').style.display = 'flex';
        };
    }

    const closeFilters = document.getElementById('close-filtros');
    if (closeFilters) {
        closeFilters.onclick = () => {
            document.getElementById('modal-filtros').style.display = 'none';
        };
    }

    const formFilters = document.getElementById('form-filtros');
    if (formFilters) {
        formFilters.onsubmit = async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const params = new URLSearchParams(fd).toString();
            try {
                const res = await fetch(`/api/inmuebles/filtrar?${params}`);
                const filtrados = await res.json();
                
                // Mostrar en ambas vistas (Dashboard e Inmuebles)
                renderProperties(filtrados, 'featured-props');
                renderProperties(filtrados, 'all-props-grid');
                
                // Ocultar modal
                document.getElementById('modal-filtros').style.display = 'none';
                
                // Si estamos en el dashboard, forzamos la vista de inmuebles para ver mejor
                // O podemos dejar que el usuario siga en la vista actual. Lo dejaremos en la actual.
            } catch (err) {
                alert('Error al aplicar filtros: ' + err.message);
            }
        };
    }

    // Botones de Filtro Rápido (Venta / Arriendo)
    const btnVenta = document.getElementById('qf-venta');
    if (btnVenta) {
        btnVenta.onclick = async () => {
            try {
                const res = await fetch(`/api/inmuebles/filtrar?fin=Venta`);
                const filtrados = await res.json();
                renderProperties(filtrados, 'featured-props');
                renderProperties(filtrados, 'all-props-grid');
                switchView('dashboard'); // Volver al inicio
                document.getElementById('featured-props').scrollIntoView({ behavior: 'smooth' });
                // Resetear form para que refleje el estado
                document.getElementById('filter-fin').value = 'Venta';
            } catch (err) { alert('Error: ' + err.message); }
        };
    }

    const btnArriendo = document.getElementById('qf-arriendo');
    if (btnArriendo) {
        btnArriendo.onclick = async () => {
            try {
                const res = await fetch(`/api/inmuebles/filtrar?fin=Arriendo`);
                const filtrados = await res.json();
                renderProperties(filtrados, 'featured-props');
                renderProperties(filtrados, 'all-props-grid');
                switchView('dashboard'); // Volver al inicio
                document.getElementById('featured-props').scrollIntoView({ behavior: 'smooth' });
                // Resetear form para que refleje el estado
                document.getElementById('filter-fin').value = 'Arriendo';
            } catch (err) { alert('Error: ' + err.message); }
        };
    }
}

function applyRoleRestrictions() {
    const adminElements = document.querySelectorAll('.admin-only');
    const asesorElements = document.querySelectorAll('.asesor-only');
    const guestOnlyElements = document.querySelectorAll('.guest-only');
    const loggedInElements = document.querySelectorAll('.logged-in-only');
    const deleteBtns = document.querySelectorAll('.delete-btn');
    const adminVisibleOnly = document.querySelectorAll('.admin-visible-only');
    const commercialElements = document.querySelectorAll('.commercial-only');
    
    if (currentRole === 'admin') {
        adminElements.forEach(el => {
            // Si es un LI, usamos block, si es un DIV de stats o FAB, usamos flex
            if (el.tagName === 'LI') el.style.display = 'block';
            else if (el.classList.contains('fab') || el.classList.contains('sidebar-stats')) el.style.display = 'flex';
            else el.style.display = 'inline-block';
        });
        asesorElements.forEach(el => el.style.display = 'none');
        commercialElements.forEach(el => el.style.display = 'inline-block');
        guestOnlyElements.forEach(el => el.style.display = 'none');
        loggedInElements.forEach(el => el.style.display = 'block');
        
        deleteBtns.forEach(el => el.style.display = 'flex');
        adminVisibleOnly.forEach(el => el.style.display = 'flex');
        document.getElementById('modal-register').style.display = 'none';
        
        // Ocultar landing si estaba visible
        const lv = document.getElementById('landing-view');
        const ac = document.getElementById('app-container');
        if(lv) lv.style.display = 'none';
        if(ac) ac.style.display = 'flex';

        console.log("Admin mode active");
    } else if (currentRole === 'asesor') {
        adminElements.forEach(el => el.style.display = 'none');
        asesorElements.forEach(el => el.style.display = '');
        commercialElements.forEach(el => el.style.display = 'inline-block');
        guestOnlyElements.forEach(el => el.style.display = 'none');
        loggedInElements.forEach(el => el.style.display = '');
        
        if (fabAdmin) fabAdmin.style.display = 'none';
        deleteBtns.forEach(el => el.classList.remove('admin-visible'));
        adminVisibleOnly.forEach(el => el.style.display = 'none');
        document.getElementById('modal-register').style.display = 'none';
        
        // Ocultar landing si estaba visible
        const lv = document.getElementById('landing-view');
        const ac = document.getElementById('app-container');
        if(lv) lv.style.display = 'none';
        if(ac) ac.style.display = 'flex';
        
        // Redirigir a panel asesor si estaban en admin view
        const activeNav = document.querySelector('nav li.active');
        if(activeNav && ['clientes', 'asesores', 'analetica', 'auditoria'].includes(activeNav.getAttribute('data-view'))) {
            switchView('asesor-dashboard');
            document.querySelectorAll('nav li').forEach(i => i.classList.remove('active'));
            document.querySelector('[data-view="asesor-dashboard"]').classList.add('active');
        }
    } else if (currentRole === 'cliente') {
        adminElements.forEach(el => el.style.display = 'none');
        asesorElements.forEach(el => el.style.display = 'none');
        guestOnlyElements.forEach(el => el.style.display = 'none');
        loggedInElements.forEach(el => el.style.display = 'block');
        document.querySelectorAll('.cliente-only').forEach(el => el.style.display = 'block');
        
        const lv = document.getElementById('landing-view');
        const ac = document.getElementById('app-container');
        if(lv) lv.style.display = 'none';
        if(ac) ac.style.display = 'flex';
        
        document.getElementById('role-label').textContent = 'Cliente';
        document.getElementById('role-label').style.display = 'block';
    } else {
        adminElements.forEach(el => el.style.display = 'none');
        document.querySelectorAll('.cliente-only').forEach(el => el.style.display = 'none');
        commercialElements.forEach(el => el.style.display = 'none');
        
        if (fabAdmin) fabAdmin.style.display = 'none';
        deleteBtns.forEach(el => el.classList.remove('admin-visible'));
        adminVisibleOnly.forEach(el => el.style.display = 'none');
        
        // Si el guest está en vista admin/asesor, mandar al dashboard
        const activeNav = document.querySelector('nav li.active');
        if(activeNav && activeNav.getAttribute('data-view') !== 'dashboard' && activeNav.getAttribute('data-view') !== 'inmuebles') {
            switchView('dashboard');
            document.querySelectorAll('nav li').forEach(i => i.classList.remove('active'));
            document.querySelector('[data-view="dashboard"]').classList.add('active');
        }
    }
}

async function fetchAuditoria() {
    if (currentRole !== 'admin') return;
    
    // Alertas de sistema
    const resAlerts = await fetch('/api/alertas');
    const alerts = await resAlerts.json();
    const alertList = document.getElementById('system-alerts-list');
    if (alertList) {
        alertList.innerHTML = alerts.map(a => `
            <div class="alert-item">
                <span>⚠️</span>
                <div>${a}</div>
            </div>
        `).join('');
    }

    // Auditoría de anomalías
    const resAud = await fetch('/api/auditoria');
    const anomalies = await resAud.json();
    const anomList = document.getElementById('anomalies-list');
    if (anomList) {
        anomList.innerHTML = anomalies.map(a => `
            <div class="anomaly-card ${a.nivel}">
                <span class="type">${a.tipo}</span>
                <div>${a.descripcion}</div>
                <div class="meta">Entidad: ${a.idEntidad} | ${a.timestamp}</div>
            </div>
        `).reverse().join(''); // Más recientes arriba
    }
}

async function fetchRecommendations() {
    if (!currentClientId) return;
    try {
        const res = await fetch(`/api/recomendaciones?cli=${currentClientId}`);
        if (!res.ok) throw new Error("API error");
        const recs = await res.json();
        
        const container = document.getElementById('recommendations-container');
        if (recs && recs.length > 0) {
            container.style.display = 'block';
            renderProperties(recs, 'rec-props');
        } else {
            if (container) container.style.display = 'none';
        }
    } catch (e) { console.error("Error fetching recommendations:", e); }
}

async function fetchData() {
    try {
        const propRes = await fetch('/api/inmuebles');
        const properties = propRes.ok ? await propRes.json() : [];
        globalProperties = properties;
        
        const clientRes = await fetch('/api/clientes');
        const clients = clientRes.ok ? await clientRes.json() : [];
        globalClients = clients;

        const analitRes = await fetch('/api/analitica');
        const analitics = analitRes.ok ? await analitRes.json() : null;

        const reportRes = await fetch('/api/reportes');
        const reports = reportRes.ok ? await reportRes.json() : null;

        renderStats(properties, clients, analitics);
        if (reports) renderReports(reports);
        
        if (isRegistered || currentRole === 'cliente') {
            await fetchRecommendations();
            if (currentClientId) await fetchClienteData();
        }

        renderProperties(properties, 'featured-props');
        renderProperties(properties, 'all-props-grid');
        renderClients(clients);
        
        if (currentRole === 'admin') {
            fetchAuditoria();
            fetchAsesores();
        }
        
        applyRoleRestrictions();
    } catch (err) {
        console.error('Error in fetchData:', err);
    }
}

async function fetchClienteData() {
    if (!currentClientId) return;
    try {
        const resFavs = await fetch(`/api/clientes/favoritos?id=${currentClientId}`);
        if (!resFavs.ok) throw new Error("Error loading favorites");
        const favs = await resFavs.json();
        currentClientFavorites = favs.map(p => p.codigo);
        console.log("Updated favorites count:", currentClientFavorites.length);
        
        if (document.getElementById('view-cliente-dashboard').style.display !== 'none') {
            await renderClienteDashboard();
        }
    } catch (e) { console.error("Error fetching client data:", e); }
}


function switchView(viewId) {
    document.querySelectorAll('.content-view').forEach(v => v.style.display = 'none');
    const view = document.getElementById(`view-${viewId}`);
    if (view) view.style.display = 'block';
    
    if (viewId === 'analetica') {
        loadAnalitica();
    }
    if (viewId === 'simulacion') {
        setupSimulacion();
    }
    if (viewId === 'cliente-dashboard') {
        renderClienteDashboard();
    }
}

async function setupSimulacion() {
    const container = document.getElementById('sim-result-container');
    if(container) container.innerHTML = '<p style="color:var(--text-muted)">Selecciona una zona para proyectar la demanda...</p>';
    
    const btn = document.getElementById('btn-run-sim');
    if(btn) {
        btn.onclick = async () => {
            const zona = document.getElementById('sim-zona').value;
            const meses = document.getElementById('sim-meses').value;
            try {
                const res = await fetch(`/api/simulacion/demanda?zon=${encodeURIComponent(zona)}&mes=${meses}`);
                const data = await res.json();
                container.innerHTML = `
                    <div style="background: var(--soft-green); padding: 20px; border-radius: 12px; border-left: 5px solid #22c55e;">
                        <h4 style="margin:0; color:#166534;">Proyección para: ${data.zona}</h4>
                        <p style="font-size: 1.1rem; margin: 10px 0;">Crecimiento estimado de demanda: <strong>${(data.crecimiento * 100).toFixed(1)}%</strong></p>
                        <p style="font-size: 0.85rem; color: #166534;">* Basado en el motor de tendencias históricas y volumen de visitas actual.</p>
                    </div>
                `;
            } catch(e) { alert("Error en simulación"); }
        };
    }
}

function renderStats(props, clients, analitics) {
    // 1. Actualizar Estadísticas en la Barra Lateral
    if (document.getElementById('stat-props')) document.getElementById('stat-props').textContent = props ? props.length : 0;
    if (document.getElementById('stat-clients')) document.getElementById('stat-clients').textContent = clients ? clients.length : 0;
    
    if (analitics) {
        const statOps = document.getElementById('stat-ops');
        if (statOps) statOps.textContent = analitics.operaciones || 0;
    }

    // 2. Generar Estadísticas por Tipo de Inmueble en el Dashboard
    const typeGrid = document.getElementById('type-stats-grid');
    if (typeGrid && props) {
        typeGrid.innerHTML = '';
        
        // Contar por tipo
        const counts = {};
        props.forEach(p => {
            const t = p.tipo || 'Otro';
            counts[t] = (counts[t] || 0) + 1;
        });

        // Crear tarjetas
        Object.keys(counts).forEach(tipo => {
            const card = document.createElement('div');
            card.className = 'stat-card clickable';
            card.innerHTML = `
                <h3>${tipo}s Disponibles</h3>
                <p>${counts[tipo]}</p>
            `;
            // Filtrar al hacer clic
            card.onclick = () => {
                const searchInput = document.getElementById('main-search-input');
                if (searchInput) {
                    searchInput.value = tipo;
                    // Disparar evento input para activar la búsqueda global
                    const event = new Event('input', { bubbles: true });
                    searchInput.dispatchEvent(event);
                    // Mover scroll hacia los inmuebles
                    document.getElementById('featured-props').scrollIntoView({ behavior: 'smooth' });
                }
            };
            typeGrid.appendChild(card);
        });
    }
}

function renderReports(reports) {
    const aCard = document.getElementById('analytics-card');
    if (aCard && reports) {
        let zonesHtml = reports.zonas.map(z => `<li><strong>${z.zona}:</strong> ${z.visitas} visitas</li>`).join('');
        aCard.innerHTML += `
            <div style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px;">
                <h4>Actividad por Zonas (Reporte 6.10)</h4>
                <ul style="list-style: none; padding: 0; font-size: 0.9rem;">
                    ${zonesHtml || '<li>No hay actividad registrada en zonas aún.</li>'}
                </ul>
            </div>
        `;
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
    if (!grid || !props || !Array.isArray(props)) {
        if (grid) grid.innerHTML = '<p style="color:var(--text-muted)">No hay inmuebles disponibles en esta sección.</p>';
        return;
    }
    grid.innerHTML = '';

    const backupPlaceholder = 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80';

    props.forEach((p) => {
        // Validación de imagen: si no hay fotos, o si la primera foto es demasiado corta para ser un objeto válido
        let coverImg = (p.fotos && p.fotos.length > 0 && p.fotos[0].length > 10) ? p.fotos[0] : backupPlaceholder;
        
        const isFav = currentClientFavorites.includes(p.codigo);
        const card = document.createElement('div');
        card.className = 'property-card';
        card.onclick = () => showDetails(p);
        card.innerHTML = `
            <button class="delete-btn" onclick="event.stopPropagation(); deleteProp('${p.codigo}')">×</button>
            ${(isRegistered || currentRole === 'cliente') ? `<button class="fav-btn ${isFav ? 'active' : ''}" onclick="event.stopPropagation(); toggleFavorite('${p.codigo}')">${isFav ? '❤️' : '🤍'}</button>` : ''}
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

async function toggleFavorite(codigo) {
    if (!currentClientId) {
        alert("Regístrate para guardar favoritos");
        document.getElementById('modal-register').style.display = 'flex';
        return;
    }

    const isFav = currentClientFavorites.includes(codigo);
    const endpoint = isFav ? '/api/clientes/favoritos/remove' : '/api/clientes/favoritos/add';
    
    try {
        console.log("Toggling favorite for client:", currentClientId, "Inmueble:", codigo);
        const res = await fetch(`${endpoint}?cli=${currentClientId}&cod=${codigo}`);
        if (res.ok) {
            // Actualizar estado local inmediatamente para feedback visual rápido
            if (isFav) {
                currentClientFavorites = currentClientFavorites.filter(c => c !== codigo);
            } else {
                currentClientFavorites.push(codigo);
            }
            
            // Refrescar datos globales
            await fetchData(); 
            
            // Si estamos en el dashboard del cliente, forzar actualización inmediata
            if (document.getElementById('view-cliente-dashboard').style.display !== 'none') {
                await renderClienteDashboard();
            }
            console.log("Toggled favorite successfully");
        } else {
            const errorText = await res.text();
            console.error("Error en respuesta de favoritos:", res.status, errorText);
            alert("No se pudo actualizar favoritos en el servidor.");
        }
    } catch (e) { 
        console.error("Error toggling favorite", e); 
        alert("Error de conexión al actualizar favoritos.");
    }
}

async function renderClienteDashboard() {
    if (!currentClientId) {
        console.warn("Attempted to render client dashboard without currentClientId");
        return;
    }
    
    try {
        console.log("Rendering client dashboard for:", currentClientId);
        const [resFavs, resVisits] = await Promise.all([
            fetch(`/api/clientes/favoritos?id=${currentClientId}`),
            fetch(`/api/clientes/visitas?id=${currentClientId}`)
        ]);
        
        const favs = await resFavs.json();
        const visits = await resVisits.json();
        console.log("Dashboard data loaded. Favs:", favs.length, "Visits:", visits.length);

        // Renderizar favoritos
        renderProperties(favs, 'cliente-favs-grid');

        // Renderizar visitas
        const visitList = document.getElementById('cliente-visits-list');
        if (visitList) {
            visitList.innerHTML = '';
            if (!visits || visits.length === 0) {
                visitList.innerHTML = '<p style="color:var(--text-muted)">No tienes visitas agendadas.</p>';
            } else {
                visits.forEach(v => {
                    const card = document.createElement('div');
                    card.className = 'visit-card-compact';
                    card.innerHTML = `
                        <div class="info">
                            <h4>${v.inmueble ? v.inmueble.direccion : 'Inmueble desconocido'}</h4>
                            <p>📅 ${v.fecha} | ⏰ ${v.hora}</p>
                            <p style="font-size:0.75rem;">Asesor: ${v.asesor ? v.asesor.nombre : 'Por asignar'}</p>
                        </div>
                        <div class="status" style="background:${v.estado === 'Pendiente' ? '#fef3c7' : '#dcfce7'}; color:${v.estado === 'Pendiente' ? '#92400e' : '#166534'};">
                            ${v.estado}
                        </div>
                    `;
                    visitList.appendChild(card);
                });
            }
        }

        // Actualizar badge
        const welcomeBadge = document.getElementById('cliente-welcome-badge');
        if (welcomeBadge) {
            const cliente = globalClients.find(c => c.id === currentClientId);
            if (cliente) {
                welcomeBadge.textContent = `Hola, ${cliente.nombre}`;
            }
        }

    } catch (e) { console.error("Error rendering client dashboard:", e); }
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
    mainImg.style.cursor = 'zoom-in'; // Indica que se puede ampliar
    
    // Configuración del lightbox para la imagen principal
    mainImg.onclick = () => {
        lightboxPhotos = photos;
        const bg = mainImg.style.backgroundImage;
        const match = bg.match(/url\(['"]?(.*?)['"]?\)/);
        if (match && match[1]) {
            const currentUrl = match[1];
            lightboxCurrentIndex = lightboxPhotos.indexOf(currentUrl);
            if(lightboxCurrentIndex === -1) lightboxCurrentIndex = 0;
            document.getElementById('lightbox-img').src = lightboxPhotos[lightboxCurrentIndex];
            
            // Ocultar flechas si solo hay 1 foto
            document.getElementById('lightbox-prev').style.display = lightboxPhotos.length > 1 ? 'block' : 'none';
            document.getElementById('lightbox-next').style.display = lightboxPhotos.length > 1 ? 'block' : 'none';
            
            document.getElementById('modal-lightbox').style.display = 'flex';
        }
    };

    document.getElementById('img-pos-slider').value = 50;
    
    const tags = document.getElementById('detail-tags');
    tags.innerHTML = `
        <span class="tag">${p.tipo}</span>
        <span class="tag tag-purpose">${p.finalidad}</span>
        <span class="tag tag-status">${p.disponibilidad}</span>
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
            document.getElementById('prop-is-update').value = 'true';
            f.elements['cod'].value = p.codigo || '';
            f.elements['cod'].readOnly = true; // Para no cambiar el identificador primario
            f.elements['dir'].value = p.direccion || '';
            f.elements['ciu'].value = p.ciudad || 'Bogotá';
            
            // Lógica inteligente para tipos personalizados
            const defaultTypes = ["Casa", "Apartamento", "Oficina", "Local", "Lote"];
            const ct = document.getElementById('custom-tip');
            if (defaultTypes.includes(p.tipo)) {
                f.elements['tip'].value = p.tipo || '';
                if(ct) { ct.style.display = 'none'; ct.required = false; }
            } else {
                f.elements['tip'].value = "Otro";
                if(ct) {
                    ct.style.display = 'block';
                    ct.required = true;
                    ct.value = p.tipo || '';
                }
            }
            
            f.elements['pre'].value = p.precio || '';
            f.elements['zon'].value = p.zona || '';
            f.elements['fin'].value = p.finalidad || 'Venta';
            f.elements['are'].value = p.area || '';
            f.elements['areT'].value = p.areaT || p.area || '';
            f.elements['hab'].value = p.hab || '';
            f.elements['ban'].value = p.banos || p.ban || '';
            f.elements['est'].value = p.disponibilidad || 'Disponible';
            f.querySelector('button[type="submit"]').textContent = 'Guardar Cambios';

            // *** PRESERVAR FOTOS EXISTENTES ***
            const existingField = document.getElementById('existing-photos');
            const previewDiv = document.getElementById('existing-photos-preview');
            const currentPhotos = (p.fotos && p.fotos.filter(f => f && f.length > 5)) || [];
            
            if (existingField) {
                existingField.value = currentPhotos.join('|SEP|');
            }
            
            // Mostrar miniaturas de fotos actuales como referencia
            if (previewDiv) {
                if (currentPhotos.length > 0) {
                    previewDiv.style.display = 'flex';
                    previewDiv.innerHTML = '<p style="width:100%;font-size:0.8rem;color:var(--text-muted);margin:0 0 5px 0;">Fotos actuales (' + currentPhotos.length + '):</p>' +
                        currentPhotos.slice(0, 5).map(src => 
                            `<div style="width:60px;height:60px;border-radius:6px;background:url('${src}') center/cover;border:2px solid #e2e8f0;"></div>`
                        ).join('');
                } else {
                    previewDiv.style.display = 'none';
                    previewDiv.innerHTML = '';
                }
            }

            // Limpiar el input de archivos para que no haya confusiones
            document.getElementById('photo-file-input').value = '';
        };
    }

    // Lógica del Botón Cerrar Negocio
    const btnCerrar = document.getElementById('btn-change-status');
    if (btnCerrar) {
        btnCerrar.onclick = () => {
            openOperacionModal(p.codigo, p.direccion, p.precio);
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
        est: property.disponibilidad || "Disponible"
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
    
    if (clients.length === 0) {
        container.innerHTML = '<p>No hay clientes registrados.</p>';
        return;
    }

    let html = `
    <table style="width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
        <thead>
            <tr style="background: var(--primary-color); color: white; text-align: left;">
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">ID</th>
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">Nombre</th>
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">Contacto</th>
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">Presupuesto</th>
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">Zona Interés</th>
                <th style="padding: 12px; border-bottom: 1px solid #ddd;">Estado</th>
            </tr>
        </thead>
        <tbody>
    `;

    clients.forEach(c => {
        let contactoStr = (c.correo && c.correo !== 'null') ? c.correo : 'N/A';
        if (c.telefono && c.telefono !== 'null') contactoStr += `<br><small>${c.telefono}</small>`;

        html += `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 12px;">${c.id}</td>
                <td style="padding: 12px; font-weight: bold;">${c.nombre}<br><span style="font-size: 0.8rem; font-weight: normal; color: #666;">${c.tipo}</span></td>
                <td style="padding: 12px;">${contactoStr}</td>
                <td style="padding: 12px;">$${c.presupuesto.toLocaleString()}</td>
                <td style="padding: 12px;">${c.zona && c.zona !== 'null' ? c.zona : 'Cualquiera'}</td>
                <td style="padding: 12px;">
                    <span style="background: var(--bg-secondary); padding: 4px 8px; border-radius: 4px; font-size: 0.85rem;">
                        ${c.estadoBusqueda}
                    </span>
                </td>
            </tr>
        `;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
    container.className = ""; // Quitamos stats-grid para que la tabla ocupe todo
}

async function fetchAsesores() {
    try {
        const res = await fetch('/api/asesores');
        const asesores = await res.json();
        renderAsesores(asesores);
    } catch (err) {
        console.error('Error cargando asesores:', err);
    }
}

function renderAsesores(asesores) {
    const container = document.getElementById('asesores-list');
    if (!container) return;
    
    if (asesores.length === 0) {
        container.innerHTML = '<p>No hay asesores registrados.</p>';
        return;
    }

    container.innerHTML = '';
    asesores.forEach(a => {
        const card = document.createElement('div');
        card.className = 'asesor-card';
        
        let inmueblesTags = '';
        if (a.inmuebles && a.inmuebles.length > 0) {
            inmueblesTags = a.inmuebles.map(codigo => `<span class="tag">${codigo}</span>`).join('');
        } else {
            inmueblesTags = '<span style="color:#999; font-style:italic;">Sin inmuebles asignados</span>';
        }

        card.innerHTML = `
            <div class="asesor-header">
                <div class="asesor-avatar">${a.nombre.charAt(0)}</div>
                <div class="asesor-info">
                    <h3>${a.nombre}</h3>
                    <p>${a.id} • ${a.zona}</p>
                    <p style="margin-top:4px;">📞 ${a.contacto}</p>
                </div>
            </div>
            <div class="asesor-stats-row">
                <div>
                    <strong>${a.inmueblesCount}</strong>
                    Inmuebles
                </div>
                <div>
                    <strong>${a.visitasCount || 0}</strong>
                    Visitas Pend.
                </div>
                <div>
                    <strong>${a.cierres}</strong>
                    Cierres
                </div>
            </div>
            <div class="asesor-inmuebles-list">
                <strong>Inmuebles a Cargo:</strong><br>
                <div style="margin-top:5px;">${inmueblesTags}</div>
            </div>
        `;
        container.appendChild(card);
    });
}

function renderAsesorDashboard(asesor) {
    // Info personal
    const infoContainer = document.getElementById('asesor-personal-info');
    infoContainer.innerHTML = `
        <div style="display:flex; justify-content:space-between;">
            <div>
                <h3>${asesor.nombre}</h3>
                <p style="color:var(--text-muted)">ID: ${asesor.id} | Zona: ${asesor.zona}</p>
                <p>Contacto: ${asesor.contacto}</p>
            </div>
            <div style="text-align:right">
                <div style="font-size: 2rem; color: var(--accent-color); font-weight: bold;">${asesor.cierres}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted); text-transform: uppercase;">Cierres Exitosos</div>
            </div>
        </div>
    `;

    // Inmuebles asignados
    const propsContainer = document.getElementById('asesor-my-props');
    propsContainer.innerHTML = '';
    if (asesor.inmuebles && asesor.inmuebles.length > 0) {
        asesor.inmuebles.forEach(cod => {
            const propData = globalProperties.find(p => p.codigo === cod);
            if(propData) {
                const div = document.createElement('div');
                div.style.padding = '10px';
                div.style.border = '1px solid #e2e8f0';
                div.style.borderRadius = '8px';
                div.style.background = '#f8fafc';
                div.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <div style="font-weight:bold; color:var(--text-main);">${propData.tipo} en ${propData.zona}</div>
                            <div style="font-size:0.85rem; color:var(--text-muted);">${propData.codigo} - ${propData.direccion}</div>
                            <div style="margin-top:5px; font-size:0.9rem; font-weight:600;">$${propData.precio.toLocaleString()} (${propData.finalidad})</div>
                        </div>
                        <button class="secondary-btn" style="padding: 5px 10px; font-size: 0.8rem;" onclick="event.stopPropagation(); openOperacionModal('${propData.codigo}', '${propData.direccion}', ${propData.precio})">
                            Cerrar
                        </button>
                    </div>
                `;
                propsContainer.appendChild(div);
            }
        });
    } else {
        propsContainer.innerHTML = '<p style="color:var(--text-muted)">No tienes inmuebles asignados.</p>';
    }

    // Visitas agendadas (Por ahora mostraremos solo el contador o una nota si no tenemos la info completa)
    const visitsContainer = document.getElementById('asesor-my-visits');
    visitsContainer.innerHTML = `
        <div style="padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; background: #f0fdf4; color:#166534; text-align:center;">
            <strong>${asesor.visitasCount || 0}</strong> visitas pendientes en cola.
        </div>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-top:10px;">
            * El sistema maneja las visitas mediante una cola de prioridad basada en el orden de llegada.
        </p>
    `;
}

async function loadAnalitica() {
    try {
        const [resRankings, resReportes] = await Promise.all([
            fetch('/api/rankings'),
            fetch('/api/reportes')
        ]);
        
        const rankings = await resRankings.json();
        const reportes = await resReportes.json();

        // Actualizar KPIs superiores
        document.getElementById('ana-total-cierres').textContent = reportes.cierres || 0;
        
        if (rankings.zonas && rankings.zonas.length > 0) {
            document.getElementById('ana-top-zona').textContent = rankings.zonas[0];
        } else {
            document.getElementById('ana-top-zona').textContent = "Sin datos";
        }

        // Tabla de Asesores
        const asesoresList = document.getElementById('ana-asesores-list');
        asesoresList.innerHTML = '';
        if (rankings.asesores && rankings.asesores.length > 0) {
            document.getElementById('ana-top-asesor').textContent = rankings.asesores[0].nombre;
            
            rankings.asesores.forEach((a, index) => {
                const tr = document.createElement('tr');
                tr.style.borderBottom = "1px solid #eee";
                tr.innerHTML = `
                    <td style="padding: 10px;">${index === 0 ? '👑 ' : ''}<strong>${a.nombre}</strong><br><span style="font-size:0.8rem; color:#666;">${a.id}</span></td>
                    <td style="padding: 10px;">${a.cierres} cierres</td>
                    <td style="padding: 10px;">
                        <div style="background: #e0e0e0; border-radius: 4px; height: 10px; width: 100%; overflow: hidden;">
                            <div style="background: var(--accent-color); height: 100%; width: ${Math.min(a.cierres * 10, 100)}%;"></div>
                        </div>
                    </td>
                `;
                asesoresList.appendChild(tr);
            });
        } else {
            document.getElementById('ana-top-asesor').textContent = "Sin datos";
            asesoresList.innerHTML = '<tr><td colspan="3">No hay datos de asesores</td></tr>';
        }

        // Clientes VIP
        const vipsList = document.getElementById('ana-vips-list');
        vipsList.innerHTML = '';
        if (rankings.vips && rankings.vips.length > 0) {
            rankings.vips.forEach(v => {
                vipsList.innerHTML += `
                    <div style="background: var(--bg-secondary); padding: 15px; border-radius: 6px; border-left: 4px solid var(--accent-color);">
                        <strong style="display: block; font-size: 1.1rem; margin-bottom: 5px;">${v.nombre}</strong>
                        <span style="font-size: 0.9rem; color: #555;">Busca: ${v.tipo} en ${v.zona}</span>
                        <div style="margin-top: 5px; font-weight: bold; color: var(--primary-color);">
                            Presupuesto: $${v.presupuesto.toLocaleString()}
                        </div>
                    </div>
                `;
            });
        } else {
            vipsList.innerHTML = '<p style="color: #666;">No hay clientes destacados por ahora.</p>';
        }

    } catch (err) {
        console.error("Error cargando analítica:", err);
    }
}

// Función global para abrir el modal de cerrar negocio
window.openOperacionModal = (codigo, direccion, precio) => {
    document.getElementById('modal-details').style.display = 'none';
    document.getElementById('modal-operacion').style.display = 'flex';
    document.getElementById('op-cod').value = codigo;
    document.getElementById('op-inmueble-info').value = `${codigo} - ${direccion}`;
    document.getElementById('op-valor').value = precio;
    
    const sel = document.getElementById('op-cliente-select');
    sel.innerHTML = '<option value="" disabled selected>Selecciona el cliente...</option>';
    globalClients.forEach(c => {
        sel.innerHTML += `<option value="${c.id}">${c.nombre} (${c.id})</option>`;
    });
};
