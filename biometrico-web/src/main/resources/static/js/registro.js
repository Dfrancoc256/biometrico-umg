/**
 * registro.js — Lógica del formulario de registro biográfico/biométrico
 * Sistema Biométrico UMG Web
 */

document.addEventListener('DOMContentLoaded', function() {
    // Auto-format carnet: mayúsculas
    const campoCarnet = document.getElementById('carnet');
    if (campoCarnet) {
        campoCarnet.addEventListener('input', function() {
            this.value = this.value.toUpperCase();
        });
    }

    // Validación de contraseña en tiempo real
    const campoContrasena = document.getElementById('contrasena');
    if (campoContrasena) {
        campoContrasena.addEventListener('input', function() {
            const longitud = this.value.length;
            const ayuda = this.nextElementSibling;
            if (ayuda && ayuda.classList.contains('campo__ayuda')) {
                if (longitud === 0) {
                    ayuda.textContent = 'Mínimo 6 caracteres';
                    ayuda.style.color = '';
                } else if (longitud < 6) {
                    ayuda.textContent = `Faltan ${6 - longitud} caracteres`;
                    ayuda.style.color = '#c0392b';
                } else {
                    ayuda.textContent = `✓ ${longitud} caracteres`;
                    ayuda.style.color = '#27ae60';
                }
            }
        });
    }
});
