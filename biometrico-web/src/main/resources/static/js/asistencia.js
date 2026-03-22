/**
 * asistencia.js — Lógica de la pantalla de control de asistencia
 * Sistema Biométrico UMG Web
 */

document.addEventListener('DOMContentLoaded', function() {
    actualizarConteo();

    // Escuchar cambios en todos los checkboxes
    document.querySelectorAll('.toggle-asistencia__check').forEach(function(chk) {
        chk.addEventListener('change', function() {
            actualizarEstado(this);
            actualizarConteo();
        });
    });

    actualizarConteo();
});

function actualizarEstado(checkbox) {
    const tarjeta = checkbox.closest('.asistencia-tarjeta');
    if (!tarjeta) return;

    if (checkbox.checked) {
        tarjeta.classList.add('asistencia-tarjeta--presente');
        tarjeta.classList.remove('asistencia-tarjeta--ausente');
    } else {
        tarjeta.classList.add('asistencia-tarjeta--ausente');
        tarjeta.classList.remove('asistencia-tarjeta--presente');
    }
    actualizarConteo();
}

function actualizarConteo() {
    const total = document.querySelectorAll('.toggle-asistencia__check').length;
    const presentes = document.querySelectorAll('.toggle-asistencia__check:checked').length;
    const span = document.getElementById('conteoPresentes');
    if (span) span.textContent = presentes;
}

function marcarTodos(presente) {
    document.querySelectorAll('.toggle-asistencia__check').forEach(function(chk) {
        chk.checked = presente;
        actualizarEstado(chk);
    });
    actualizarConteo();
}
