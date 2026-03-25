function handleAdminLogin(event) {
    // Prevent the form from refreshing the page
    if(event) {
        event.preventDefault();
    }

    // 1. Get the values from the input fields
    const usernameInput = document.getElementById('adminUsername').value;
    const passwordInput = document.getElementById('adminPassword').value;

    // Optional: Basic validation to ensure fields aren't empty
    if(!usernameInput || !passwordInput) {
        alert("Please enter both username and password.");
        return;
    }

    // 2. Make the POST request to your backend
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
                return response.json(); // Parse the JSON if login is successful
            } else {
                throw new Error('Invalid admin credentials'); // Throw error if 401 Unauthorized
            }
        })
        .then(data => {
            // 3. Save the JWT tokens to localStorage so the browser remembers the admin is logged in
            localStorage.setItem('adminAccessToken', data.accessToken);
            localStorage.setItem('adminRequestToken', data.requestToken);

            alert("Login Successful!");

            // 4. Redirect to the main dashboard (update 'index.html' if your dashboard has a different name)
            window.location.href = 'index.html';
        })
        .catch(error => {
            // Handle failed logins (e.g., wrong password)
            alert('Login Failed: ' + error.message);
        });
}