function initForm() {
    const form = document.getElementById('login-form');

    form.addEventListener('submit', function(event) {
        event.preventDefault();

        const username = document.getElementById('username-input').value.trim();
        const password = document.getElementById('password-input').value.trim();

        if (!username || !password) {
            alert('Fill every input before submitting');
            return;
        }

        const jsonData = JSON.stringify({ username, password });

        httpRequest('http://localhost:8080/user/login', 'POST', jsonData)
            .then(response => {
                console.log("Respuesta del servidor:", response);

                if (response.status !== 200) {
                    alert('Error ' + response.status + '\n' + JSON.stringify(response.body));
                    return;
                }

                const token = response.body["session_token"];
                if (!token) {
                    console.error("No se encontró 'session_token' en la respuesta.");
                    alert("Error: No se encontró el token.");
                    return;
                }

                localStorage.setItem('session_token', token);
                console.log("Token guardado en localStorage, redirigiendo...");
                window.location.href = './dashboard.html';
            })
            .catch(error => {
                console.error('Error en la petición:', error);
                alert('Error en la conexión o en la respuesta del servidor.');
            });
    });
}

document.addEventListener('DOMContentLoaded', initForm);
