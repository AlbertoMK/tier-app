function searchUser() {
    const user = document.querySelector("#username-input").value;
    httpRequest('http://localhost:8080/user?username='+user, 'GET')
        .then(response => {
            const status = response.status;
            const body = response.body;
            if (status != 200) {
                alert('Error ' + status + '\n' + body);
            } else {
                document.getElementById("user-result").style.display = "flex";
                document.getElementById("username").innerText = user;
            }
        })
}

function addUser() {
    const requested = document.querySelector("#username").innerText;
    const session_token = localStorage.getItem('session_token');
    const jsonData = JSON.stringify({ session_token, requested });
    httpRequest('http://localhost:8080/user/friend', 'POST', jsonData)
        .then(response => {
            const status = response.status;
            const body = response.body;
            if (status != 200) {
                alert('Error ' + status + '\n' + body);
            } else {
                alert('Friend request sent!')
                window.location.href = './dashboard.html';
            }
        })
}