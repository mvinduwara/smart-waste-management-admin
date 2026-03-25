document.addEventListener("DOMContentLoaded", function() {
    const adminToken = localStorage.getItem('adminAccessToken');
    if (!adminToken) {
        alert("Unauthorized access. Please log in.");
        window.location.href = 'login.html';
        return;
    }
    fetchPendingRequests(adminToken);
});

function fetchPendingRequests(token) {
    fetch('http://localhost:8080/api/admin/requests/PENDING', {
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
        .then(data => populatePendingTable(data))
        .catch(error => console.error('Error fetching data:', error));
}

function populatePendingTable(requests) {
    const tableBody = document.getElementById('pendingTableBody');
    if (!tableBody) return;

    tableBody.innerHTML = '';

    // Handle Empty Data State
    if (!requests || requests.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="12" class="text-center text-muted py-5">
                    <h5>No pending records available</h5>
                </td>
            </tr>`;
        return;
    }

    requests.forEach((req, index) => {
        // Format Date
        let formattedDate = 'N/A';
        if(req.createdAt) {
            let dateObj = new Date(req.createdAt);
            formattedDate = dateObj.toLocaleDateString() + " " + dateObj.toLocaleTimeString();
        }

        // Make Coordinates a clickable map link
        let coordinates = (req.latitude && req.longitude)
            ? `<a href="https://maps.google.com/?q=${req.latitude},${req.longitude}" target="_blank" class="text-primary font-weight-bold">View Map <i class="fa fa-external-link"></i></a>`
            : '<span class="text-muted">N/A</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${index + 1}</strong></td>
            <td class="color-primary"><strong>#${req.id || 'N/A'}</strong></td>
            <td>${req.userName || '<span class="text-muted">N/A</span>'}</td>
            <td>${req.userEmail || '<span class="text-muted">N/A</span>'}</td>
            <td>${req.driverName || '<span class="badge badge-warning light">Unassigned</span>'}</td>
            <td>${req.driverContact || '<span class="text-muted">N/A</span>'}</td>
            <td>${req.wasteType || 'N/A'}</td>
            <td>${req.address || 'N/A'}</td>
            <td>${req.notes || '<span class="text-muted">None</span>'}</td>
            <td><strong>${req.estimatedValue ? '$' + req.estimatedValue : '$0.00'}</strong></td>
            <td>${coordinates}</td>
            <td>${formattedDate}</td>
        `;
        tableBody.appendChild(row);
    });
}