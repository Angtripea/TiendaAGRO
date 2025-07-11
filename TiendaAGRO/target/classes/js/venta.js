document.addEventListener('DOMContentLoaded', () => {
    const btnComprar = document.querySelector('.btn-add-carta');

    btnComprar.addEventListener('click', () => {
        if (allProducts.length === 0) {
            alert('🛒 El carrito está vacío.');
            return;
        }

        const email = document.querySelector('#email').value.trim();
        const password = document.querySelector('#password').value.trim();
        const tarjeta = document.querySelector('#tarjeta').value.trim();

        if (!email || !password || !tarjeta) {
            alert('⚠️ Por favor, completa todos los campos de cliente.');
            return;
        }

        // ✅ CALCULAR TOTAL CORRECTAMENTE
        const totalTexto = valorTotal.textContent.replace('$', '').replace('.', '').trim();
        const total = parseInt(totalTexto);

        // ✅ Enviar al backend
        fetch('guardar_venta', {
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
            alert('❌ Error al comunicarse con el servidor.');
            console.error(error);
        });
    });
});