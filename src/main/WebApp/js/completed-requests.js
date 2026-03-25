document.addEventListener("DOMContentLoaded", function() {
    const adminToken = localStorage.getItem('adminAccessToken');
    if (!adminToken) {
        alert("Unauthorized access. Please log in.");
        window.location.href = 'login.html';
        return;
    }
    fetchCompletedRequests(adminToken);
});

function fetchCompletedRequests(token) {
    // Fetching COMPLETED requests
    fetch('http://localhost:8080/api/admin/requests/COMPLETED', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    })
        .then(response => {
            if (response.status === 401) {
                localStorage.removeItem('adminAccessToken');
                window.location.href = 'login.html';
                throw new Error('Unauthorized');
            }
            return response.json();
        })
        .then(data => populateCompletedTable(data))
        .catch(error => console.error('Error fetching data:', error));
}

function populateCompletedTable(requests) {
    const tableBody = document.getElementById('completedTableBody');
    tableBody.innerHTML = '';

    if (!requests || requests.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">No completed requests available</td>
            </tr>`;
        return;
    }

    requests.forEach(req => {
        let dateObj = req.createdAt ? new Date(req.createdAt) : new Date();
        let formattedDate = dateObj.toLocaleDateString();

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>#${req.id}</strong></td>
            <td>User ID: ${req.userId || 'N/A'}</td>
            <td>${req.wasteType || 'N/A'}</td>
            <td>${req.weight || 'N/A'} kg</td>
            <td>${formattedDate}</td>
            <td><span class="badge badge-success">${req.status}</span></td>
        `;
        tableBody.appendChild(row);
    });
}