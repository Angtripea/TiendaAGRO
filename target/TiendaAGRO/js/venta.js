// Esperar a que todo el DOM esté cargado
document.addEventListener('DOMContentLoaded', () => {
    // 1. Autocompletar correos usando <datalist>
    fetch('/TiendaAGRO/obtener_correos')
        .then(res => res.json())
        .then(emails => {
            const datalist = document.getElementById('emails-list');
            emails.forEach(email => {
                const option = document.createElement('option');
                option.value = email;
                datalist.appendChild(option);
            });
        })
        .catch(err => console.error('❌ Error al cargar correos:', err));

    // 2. Evento de compra
    const btnComprar = document.querySelector('.btn-add-carta');

    btnComprar.addEventListener('click', () => {
        if (allProducts.length === 0) {
            alert('🛒 El carrito está vacío.');
            return;
        }

        // Capturar datos del formulario
        const email = document.querySelector('#email').value.trim();
        const password = document.querySelector('#password').value.trim();
        const tarjeta = document.querySelector('#tarjeta').value.trim();

        // Validar campos
        if (!email || !password || !tarjeta) {
            alert('⚠️ Por favor, completa todos los campos de cliente.');
            return;
        }

        // Obtener el total
        const valorTotal = document.querySelector('#valorTotal');
        if (!valorTotal) {
            alert('❌ No se pudo obtener el valor total');
            return;
        }

        const totalTexto = valorTotal.textContent.replace('$', '').replace(/\./g, '').trim();
        const total = parseInt(totalTexto);

        // Enviar al backend
        fetch('/TiendaAGRO/guardar_venta', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email,
                password,
                tarjeta,
                total,
                productos: allProducts
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert('✅ Compra realizada. ID: ' + data.id_compra);

                // Limpiar carrito y formulario
                allProducts = [];
                showHTML();
                document.querySelector('#email').value = '';
                document.querySelector('#password').value = '';
                document.querySelector('#tarjeta').value = '';
            } else {
                alert('❌ Error al guardar venta.');
            }
        })
        .catch(error => {
            alert('❌ Error al comunicarse con el servidor. Intenta nuevamente.');
            console.error('Error de red o servidor:', error);
        });
    });
});
