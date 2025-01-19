function initForm() {
    const form = document.getElementById('register-form');

    form.addEventListener('submit', function(event) {
        event.preventDefault();

        const username = document.getElementById('username-input').value.trim();
        const password = document.getElementById('password-input').value.trim();
        const dateOfBirth = document.getElementById('date-input').value;

        if (!username || !password || !dateOfBirth) {
            alert('Fill every input before submitting');
            return;
        }

        const jsonData = JSON.stringify({ username, password, dateOfBirth });
        httpRequest('http://localhost:8080/user', 'POST', jsonData)
            .then(response => {
                const status = response.status;
                const body = response.body;
                if (status != 200) {
                    alert('Error ' + status + '\n' + body);
                } else {
                    window.location.href='./login.html';
                    alert('Account successfully created.')
                }
            })

    });
}

document.addEventListener('DOMContentLoaded', initForm);
