/**
 * restringidos.js — Búsqueda y gestión de personas restringidas
 * Sistema Biométrico UMG Web
 */

let temporizadorBusqueda = null;

function buscarParaRestringir(termino) {
    clearTimeout(temporizadorBusqueda);
    const resultados = document.getElementById('resultadosBusqueda');

    if (termino.length < 2) {
        resultados.innerHTML = '';
        return;
    }

    temporizadorBusqueda = setTimeout(function() {
        fetch('/api/personas/buscar?q=' + encodeURIComponent(termino))
            .then(r => r.json())
            .then(personas => {
                if (personas.length === 0) {
                    resultados.innerHTML = '<p style="color:#7f8c8d;font-size:0.85rem;padding:0.5rem">Sin resultados</p>';
                    return;
                }
                resultados.innerHTML = personas.map(p => `
                    <div style="background:#f5f5f5;border-radius:8px;padding:0.75rem;margin-top:0.5rem;display:flex;align-items:center;justify-content:space-between">
                        <div>
                            <strong>${p.nombreCompleto}</strong>
                            <span style="color:#7f8c8d;font-size:0.8rem;margin-left:0.5rem">${p.carnet}</span>
                            <span style="color:#7f8c8d;font-size:0.8rem;margin-left:0.5rem">${p.tipoPersona}</span>
                        </div>
                        <button class="btn btn--peligro btn--pequeno"
                                onclick="mostrarFormRestriccion(${p.id}, '${p.nombreCompleto}')">
                            🚫 Restringir
                        </button>
                    </div>
                `).join('');
            })
            .catch(() => {
                resultados.innerHTML = '<p style="color:#c0392b;font-size:0.85rem">Error al buscar</p>';
            });
    }, 350);
}

function mostrarFormRestriccion(id, nombre) {
    const motivo = prompt(`Ingrese el motivo de restricción para:\n${nombre}`);
    if (motivo && motivo.trim()) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/admin/restringidos/restringir/' + id;

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || '';

        form.innerHTML = `
            <input type="hidden" name="_csrf" value="${csrfToken}"/>
            <input type="hidden" name="motivo" value="${motivo.trim()}"/>
        `;
        document.body.appendChild(form);
        form.submit();
    }
}
