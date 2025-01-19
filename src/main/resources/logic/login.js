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
                const status = response.status;
                const body = response.body;
                if (status != 200) {
                    alert('Error ' + status + '\n' + body);
                } else {
                    const token = response.json().get("session-token");
                    localStorage.setItem('session-token', token);
                    window.location.href='./dashboard.html';
                    alert('Login correctly.')
                }
            })

    });
}

document.addEventListener('DOMContentLoaded', initForm);
