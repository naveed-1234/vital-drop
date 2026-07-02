window.onload = function () {
    // Drop execution immediately if we are sitting inside the login node container
    if (window.location.pathname.includes("login")) {
        return;
    }

    const admin = JSON.parse(localStorage.getItem("admin"));

    if (!admin) {
        window.location.href = "/admin/login.html";
        return;
    }

    // Default Initialization View setup for Dashboard Console Core
    document.getElementById("welcomeText").innerText = `Welcome, ${admin.username}`;
    loadDashboardStats();
};

async function loginAdmin() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const status = document.getElementById("loginStatus");

    try {
        const response = await fetch(
            `/admin/login?username=${username}&password=${password}`,
            { method: "POST" }
        );

        if (response.ok) {
            const admin = await response.json();
            localStorage.setItem("admin", JSON.stringify(admin));
            window.location.href = "/admin/dashboard.html";
        } else {
            status.innerText = await response.text();
            status.style.color = "red";
        }
    } catch (error) {
        console.error(error);
        status.innerText = "Login Failed";
        status.style.color = "red";
    }
}

function logout() {
    localStorage.removeItem("admin");
    window.location.href = "/admin/login.html";
}

async function loadDonors() {
    const response = await fetch("/donors/all");
    const donors = await response.json();
    const donorList = document.getElementById("donorList");

    if (!donorList) return;
    donorList.innerHTML = "";

    donors.forEach(donor => {
        donorList.innerHTML += `
        <tr>
            <td>${donor.donorId}</td>
            <td>${donor.fullName}</td>
            <td>${donor.email}</td>
            <td>${donor.bloodGroup}</td>
            <td>${donor.city}</td>
            <td>${donor.availability ? "Available" : "Not Available"}</td>
            <td>
                <button onclick="deleteDonor(${donor.donorId})">Delete</button>
            </td>
        </tr>
        `;
    });
}

async function deleteDonor(donorId){
    if(!confirm("Delete this donor?")){
        return;
    }
    const response = await fetch(`/donors/${donorId}`, { method: "DELETE" });
    const message = await response.text();
    alert(message);
    loadDonors();
}

async function loadRecipients() {
    const response = await fetch("/recipients/all");
    const recipients = await response.json();
    const recipientList = document.getElementById("recipientList");

    if (!recipientList) return;
    recipientList.innerHTML = "";

    recipients.forEach(recipient => {
        // Alignment tuning mapping back to bloodGroupNeeded parameter array references
        const bloodType = recipient.bloodGroupNeeded || recipient.bloodGroup || "N/A";

        recipientList.innerHTML += `
        <tr>
            <td>${recipient.recipientId}</td>
            <td>${recipient.fullName}</td>
            <td>${recipient.email}</td>
            <td>${bloodType}</td>
            <td>${recipient.city}</td>
            <td>
                <button onclick="deleteRecipient(${recipient.recipientId})">Delete</button>
            </td>
        </tr>
        `;
    });
}

async function deleteRecipient(recipientId){
    if(!confirm("Delete this recipient?")){
        return;
    }
    const response = await fetch(`/recipients/${recipientId}`, { method: "DELETE" });
    alert(await response.text());
    loadRecipients();
}

async function loadAllRequests() {
    const response = await fetch("/requests/details");
    const requests = await response.json();
    const requestList = document.getElementById("requestList");

    if (!requestList) return;
    requestList.innerHTML = "";

    requests.forEach(request => {
        const status = request.status;
        let stepperHtml = '';

        // NEW: Evaluate backend data states to build custom stepper graphics
        // NEW: Bulletproof Unicode Progress Stepper
                if (status === "PENDING") {
                    stepperHtml = `
                        <div class="progress-stepper" title="Status: Pending Match" style="color: #fbbf24;">
                            ●<span style="color:#475569;">──●──●</span> <span style="font-size: 11px; margin-left: 5px; color: #fbbf24;">(Pending)</span>
                        </div>`;
                } else if (status === "ACCEPTED") {
                    stepperHtml = `
                        <div class="progress-stepper" title="Status: Approved by Donor" style="color: #10b981;">
                            ●──●<span style="color:#475569;">──●</span> <span style="font-size: 11px; margin-left: 5px; color: #10b981;">(Accepted)</span>
                        </div>`;
                } else if (status === "COMPLETED") {
                    stepperHtml = `
                        <div class="progress-stepper" title="Status: Donation Complete!" style="color: #6366f1;">
                            ●──●──● <span style="font-size: 11px; margin-left: 5px; color: #6366f1;">(Completed)</span>
                        </div>`;
                } else {
                    // Displays CANCELLED just like rows 2 and 3 in your screen capture
                    stepperHtml = `<span style="color:#ef4444; font-weight:bold;">CANCELLED</span>`;
                }

        const date = new Date(request.requestDate).toLocaleString();

        requestList.innerHTML += `
        <tr>
            <td>${request.requestId}</td>
            <td>${request.donorName}</td>
            <td>${request.bloodGroup}</td>
            <td>${request.city}</td>
            <td>${request.recipientName}</td>
            <td>${date}</td>
            <td>${stepperHtml}</td> </tr>
        `;
    });
}

async function loadDashboardStats() {
    try {
        const response = await fetch("/admin/stats");
        const stats = await response.json();

        document.getElementById("totalDonors").innerText = stats.totalDonors;
        document.getElementById("totalRecipients").innerText = stats.totalRecipients;
        document.getElementById("availableDonors").innerText = stats.availableDonors;
        document.getElementById("pendingRequests").innerText = stats.pendingRequests;
        document.getElementById("acceptedRequests").innerText = stats.acceptedRequests;
    } catch(error){
        console.error(error);
    }
}

/* ==========================================================================
   INTERACTION LAYER GLOBAL CONTROLLER OVERRIDE
   ========================================================================== */
/* Seamless Single-Page Application state logic pipeline */
function showSection(sectionId, element) {
    // 1. Terminate alternate visibility panes
    document.querySelectorAll('.section').forEach(s => s.style.display = 'none');

    // 2. Refresh anchor styles
    document.querySelectorAll('.menu li').forEach(li => li.classList.remove('activeMenu'));

    // 3. Mount current tab pane
    const activeSection = document.getElementById(sectionId);
    if (activeSection) {
        activeSection.style.display = 'block';
    }
    if (element) {
        element.classList.add('activeMenu');
    }

    // 4. Automated Extraction Router Engine
    if (sectionId === 'dashboardSection') {
        loadDashboardStats();
    } else if (sectionId === 'donorsSection') {
        loadDonors();
    } else if (sectionId === 'recipientsSection') {
        loadRecipients();
    } else if (sectionId === 'requestsSection') {
        loadAllRequests();
    }
}

// Example function to call when typing in the Admin Donor search bar
async function handleAdminDonorSearch(searchString) {
    const response = await fetch(`/donors/search/name?name=${encodeURIComponent(searchString)}`);
    const donors = await response.json();

    // Clear and re-populate your admin management grid rows dynamically here
    renderAdminDonorTable(donors);
}

// Dynamic real-time donor lookup engine
async function searchDonorsAdmin(queryName) {
    try {
        // Enforces routing fallback path to fetch all entries if query gets cleared out completely
        const url = queryName.trim() === ""
            ? "/donors/all"
            : `/donors/search/name?name=${encodeURIComponent(queryName)}`;

        const response = await fetch(url);
        const donors = await response.json();

        // Re-use your existing admin table rendering function to clean and rebuild rows instantly
        renderAdminDonorTable(donors);
    } catch (error) {
        console.error("Error executing administrative donor search request thread:", error);
    }
}

// Dynamic real-time recipient lookup engine
async function searchRecipientsAdmin(queryName) {
    try {
        const url = queryName.trim() === ""
            ? "/recipients/all"
            : `/recipients/search/name?name=${encodeURIComponent(queryName)}`;

        const response = await fetch(url);
        const recipients = await response.json();

        // Re-use your existing admin table rendering function to clean and rebuild rows instantly
        renderAdminRecipientTable(recipients);
    } catch (error) {
        console.error("Error executing administrative recipient search request thread:", error);
    }
}

// Function tied to the Donor Search Button
async function executeAdminDonorSearch() {
    const queryName = document.getElementById("adminDonorSearchInput").value.trim();
    try {
        const url = queryName === ""
            ? "/donors/all"
            : `/donors/search/name?name=${encodeURIComponent(queryName)}`;

        const response = await fetch(url);
        const donors = await response.json();

        const donorList = document.getElementById("donorList");
        if (!donorList) return;
        donorList.innerHTML = ""; // Clear table rows

        // Render filtered results
        donors.forEach(donor => {
            donorList.innerHTML += `
            <tr>
                <td>${donor.donorId}</td>
                <td>${donor.fullName}</td>
                <td>${donor.email}</td>
                <td>${donor.bloodGroup}</td>
                <td>${donor.city}</td>
                <td>${donor.availability ? "Available" : "Not Available"}</td>
                <td>
                    <button onclick="deleteDonor(${donor.donorId})">Delete</button>
                </td>
            </tr>
            `;
        });
    } catch (error) {
        console.error("Error executing administrative donor search:", error);
    }
}

// Function tied to the Recipient Search Button
async function executeAdminRecipientSearch() {
    const queryName = document.getElementById("adminRecipientSearchInput").value.trim();
    try {
        const url = queryName === ""
            ? "/recipients/all"
            : `/recipients/search/name?name=${encodeURIComponent(queryName)}`;

        const response = await fetch(url);
        const recipients = await response.json();

        const recipientList = document.getElementById("recipientList");
        if (!recipientList) return;
        recipientList.innerHTML = ""; // Clear table rows

        // Render filtered results
        recipients.forEach(recipient => {
            const bloodType = recipient.bloodGroupNeeded || recipient.bloodGroup || "N/A";
            recipientList.innerHTML += `
            <tr>
                <td>${recipient.recipientId}</td>
                <td>${recipient.fullName}</td>
                <td>${recipient.email}</td>
                <td>${bloodType}</td>
                <td>${recipient.city}</td>
                <td>
                    <button onclick="deleteRecipient(${recipient.recipientId})">Delete</button>
                </td>
            </tr>
            `;
        });
    } catch (error) {
        console.error("Error executing administrative recipient search:", error);
    }
}