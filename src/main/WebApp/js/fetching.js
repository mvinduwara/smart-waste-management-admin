function handleAdminLogin(event) {
    if (event) {
        event.preventDefault();
    }

    const usernameInput = document.getElementById('adminUsername').value;
    const passwordInput = document.getElementById('adminPassword').value;

    if (!usernameInput || !passwordInput) {
        alert("Please enter both username and password.");
        return;
    }

    fetch('http://localhost:8080/api/admin/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: usernameInput,
            password: passwordInput
        })
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Invalid admin credentials');
            }
        })
        .then(data => {
            localStorage.setItem('adminAccessToken', data.accessToken);
            localStorage.setItem('adminRequestToken', data.requestToken);

            alert("Login Successful!");
            window.location.href = 'index.html';
        })
        .catch(error => {
            alert('Login Failed: ' + error.message);
        });
}

function handleLogout(event) {
    event.preventDefault();
    localStorage.removeItem('token');
    localStorage.removeItem('adminId');
    localStorage.removeItem('adminUsername');

    window.location.href = 'login.html';
}
