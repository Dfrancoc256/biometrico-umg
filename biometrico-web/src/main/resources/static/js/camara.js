/**
 * camara.js — Manejo de la cámara web para captura de fotos
 * Sistema Biométrico UMG Web
 */

let streamActivo = null;

function activarCamara() {
    const seccionCamara = document.getElementById('seccionCamara');
    const video = document.getElementById('videoStream');
    seccionCamara.style.display = 'block';

    navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480 } })
        .then(stream => {
            streamActivo = stream;
            video.srcObject = stream;
        })
        .catch(err => {
            alert('No se pudo acceder a la cámara: ' + err.message);
            seccionCamara.style.display = 'none';
        });
}

function capturarFoto() {
    const video = document.getElementById('videoStream');
    const canvas = document.getElementById('canvasCaptura');
    const preview = document.getElementById('imgPreview');
    const placeholder = document.getElementById('fotoPlaceholder');
    const estadoBiometrico = document.getElementById('estadoBiometrico');

    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;

    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    const dataUrl = canvas.toDataURL('image/jpeg', 0.92);

    // Mostrar vista previa
    preview.src = dataUrl;
    preview.style.display = 'block';
    if (placeholder) placeholder.style.display = 'none';

    // Guardar base64 para envío
    document.getElementById('encodingFacialBase64').value = dataUrl;

    // Actualizar estado biométrico
    if (estadoBiometrico) {
        estadoBiometrico.innerHTML = `
            <span class="estado-punto estado-punto--verde"></span>
            Foto capturada desde cámara
        `;
    }

    detenerCamara();
}

function detenerCamara() {
    if (streamActivo) {
        streamActivo.getTracks().forEach(t => t.stop());
        streamActivo = null;
    }
    const seccionCamara = document.getElementById('seccionCamara');
    if (seccionCamara) seccionCamara.style.display = 'none';
}

function previsualizarFoto(input) {
    if (!input.files || !input.files[0]) return;
    const lector = new FileReader();
    const preview = document.getElementById('imgPreview');
    const placeholder = document.getElementById('fotoPlaceholder');
    const estadoBiometrico = document.getElementById('estadoBiometrico');

    lector.onload = function(e) {
        if (preview) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        if (placeholder) placeholder.style.display = 'none';
        if (estadoBiometrico) {
            estadoBiometrico.innerHTML = `
                <span class="estado-punto estado-punto--verde"></span>
                Foto cargada desde archivo
            `;
        }
    };
    lector.readAsDataURL(input.files[0]);
}
