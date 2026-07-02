window.onload = async function () { // Added async keyword here to handle data fetching safely

    // Run only on dashboard page
    if (!window.location.pathname.includes("dashboard")) {
        return;
    }

    const donor = JSON.parse(localStorage.getItem("donor"));

    // Secure authentication check
    if (!donor) {
        alert("Please login first");
        window.location.href = "/donor/login.html";
        return;
    }

    document.getElementById("headerUserName").innerText = donor.fullName;

    document.getElementById("welcomeText").innerText =
        `Welcome, ${donor.fullName}`;

    document.getElementById("availabilityStatus").innerText =
        donor.availability
            ? "Available "
            : "Not Available ";

    document.getElementById("profileName").innerText =
        "Name: " + donor.fullName;

    document.getElementById("profileEmail").innerText =
        "Email: " + donor.email;

    document.getElementById("profileBlood").innerText =
        "Blood Group: " + donor.bloodGroup;

    document.getElementById("profileCity").innerText =
        "City: " + donor.city;

    // ==========================================================================
    // DYNAMIC COUNTERS LOGIC ENGINE
    // ==========================================================================
    try {
        // Fetch all detailed requests assigned to this donor node
        const response = await fetch(`/requests/donor/${donor.donorId}`);
        const requests = await response.json();

        // 1. Calculate PENDING requests
        const pendingCount = requests.filter(req => req.status === "PENDING").length;
        document.getElementById("pendingCount").innerText = pendingCount;

        // 2. Calculate COMPLETED requests
        const completedCount = requests.filter(req => req.status === "COMPLETED").length;
        document.getElementById("completedCount").innerText = completedCount;

    } catch (error) {
        console.error("Error populating analytical dashboard grid counters:", error);
        document.getElementById("pendingCount").innerText = "0";
        document.getElementById("completedCount").innerText = "0";
    }

    loadRequests();
    loadDonationHistory();
    showSection("dashboardSection");
};


async function sendOtp() {
    const email = document.getElementById("email").value;

    if (email === "") {
        alert("Please enter email");
        return;
    }

    // Find the send OTP button dynamically across your registration/forgot-password pages
    const otpBtn = document.querySelector("button[onclick='sendOtp()']");
    if (!otpBtn) return;

    // 1. Lock the button and swap text with a high-end spinner markup
    otpBtn.disabled = true;
    const originalText = otpBtn.innerHTML;
    otpBtn.innerHTML = `<span class="spinner"></span> Sending...`;

    try {
        const response = await fetch(
            `/otp/send?email=${email}`,
            { method: "POST" }
        );

        const message = await response.text();
        alert(message);

        // 2. Begin the strict 30-second security countdown
        let countdown = 30;
        const timerInterval = setInterval(() => {
            countdown--;
            if (countdown > 0) {
                otpBtn.innerHTML = `Resend in ${countdown}s`;
            } else {
                clearInterval(timerInterval);
                otpBtn.disabled = false;
                otpBtn.innerHTML = originalText; // Restores original text (e.g., "Send OTP")
            }
        }, 1000);

    } catch (error) {
        console.error(error);
        alert("Failed to send OTP");

        // Safety Fallback: Instantly unlock button if the network completely fails
        otpBtn.disabled = false;
        otpBtn.innerHTML = originalText;
    }
}

async function verifyOtp() {
    const email = document.getElementById("email").value;
    const otp = document.getElementById("otp").value;
    const status = document.getElementById("verificationStatus");
    const verifyBtn = document.getElementById("verifyBtn"); // Targeting the button

    try {
        const response = await fetch(
            `/otp/verify?email=${email}&otp=${otp}`,
            { method: "POST" }
        );
        const message = await response.text();

        if(message.includes("Verified")) {
            status.innerHTML = "Email Verified Successfully";
            status.style.color = "green";

            // NEW: Disable the button so it cannot be clicked again
            if(verifyBtn) {
                verifyBtn.disabled = true;
                verifyBtn.style.opacity = "0.5"; // Optional: make it look "disabled"
                verifyBtn.style.cursor = "not-allowed";
            }

            if(document.getElementById("registerBtn")){
                document.getElementById("registerBtn").disabled = false;
            }
            if(document.getElementById("resetBtn")){
                document.getElementById("resetBtn").disabled = false;
            }
        } else {
            status.innerHTML = "Invalid OTP";
            status.style.color = "red";
        }
    } catch(error) {
        status.innerHTML = "Verification Failed";
        status.style.color = "red";
    }
}

async function loginDonor() {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const status = document.getElementById("loginStatus");

    try {
        const response = await fetch(
            `/donors/login?email=${email}&password=${password}`,
            { method: "POST" }
        );

        if(response.ok) {
            const donor = await response.json();
            localStorage.setItem("donor", JSON.stringify(donor));
            window.location.href = "/donor/dashboard.html";
        } else {
            const error = await response.text();
            status.innerHTML = error;
            status.style.color = "red";
        }
    } catch(error) {
        console.error(error);
        status.innerHTML = "Login Failed";
        status.style.color = "red";
    }
}

function logout() {
    localStorage.removeItem("donor");
    window.location.href = "/donor/login.html";
}

async function toggleAvailability() {
    const donor = JSON.parse(localStorage.getItem("donor"));
    const response = await fetch(
        `/donors/availability/${donor.donorId}`,
        { method: "PUT" }
    );

    if(response.ok) {
        const updatedDonor = await response.json();
        localStorage.setItem("donor", JSON.stringify(updatedDonor));
        alert("Availability updated!");
        window.location.reload();
    }
}

async function loadRequests() {
    const donor = JSON.parse(localStorage.getItem("donor"));
    const response = await fetch(`/requests/donor/${donor.donorId}`);
    const requests = await response.json();
    const requestList = document.getElementById("requestList");

    if (requests.length === 0) {
        requestList.innerHTML = `
            <tr>
                <td colspan="4">No requests yet.</td>
            </tr>
        `;
        return;
    }

    requestList.innerHTML = "";
    requests.forEach(request => {
        const status = request.status;
        let stepperHtml = '';
        let actionButtons = "Completed";

        // NEW: Inline visual timeline parsing framework
        if (status === "PENDING") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Pending Match" style="color: #fbbf24; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●<span style="color:#475569;">──●──●</span> <span style="font-size: 11px; margin-left: 5px; color: #fbbf24;">(Pending)</span>
                </div>`;
            actionButtons = `
                <button onclick="acceptRequest(${request.requestId})">Accept</button>
                <button onclick="rejectRequest(${request.requestId})">Reject</button>
            `;
        } else if (status === "ACCEPTED") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Active Delivery" style="color: #10b981; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●──●<span style="color:#475569;">──●</span> <span style="font-size: 11px; margin-left: 5px; color: #10b981;">(Accepted)</span>
                </div>`;
            actionButtons = `<span style="color: #10b981; font-weight: 600;">In Progress</span>`;
        } else if (status === "COMPLETED") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Donation Complete!" style="color: #6366f1; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●──●──● <span style="font-size: 11px; margin-left: 5px; color: #6366f1;">(Completed)</span>
                </div>`;
        } else {
            stepperHtml = `<span style="color:#ef4444; font-weight:bold;">CANCELLED</span>`;
            actionButtons = `<span style="color:#475569;">None</span>`;
        }

        requestList.innerHTML += `
            <tr>
                <td>${request.requestId}</td>
                <td><strong>${request.recipientName}</strong></td>
                <td>${stepperHtml}</td> <td>${actionButtons}</td>
            </tr>
        `;
    });
}

async function acceptRequest(requestId) {
    const response = await fetch(
        `/requests/accept/${requestId}`,
        { method: "PUT" }
    );

    if (response.ok) {
        let donor = JSON.parse(localStorage.getItem("donor"));
        donor.availability = false;
        localStorage.setItem("donor", JSON.stringify(donor));
        alert("Request Accepted");
        location.reload();
    }
}

async function rejectRequest(requestId) {
    const response = await fetch(
        `/requests/reject/${requestId}`,
        { method: "PUT" }
    );

    if (response.ok) {
        alert("Request Rejected");
        loadRequests();
    }
}

function toggleProfile() {
    const dropdown = document.getElementById("profileDropdown");
    if(dropdown.style.display === "block") {
        dropdown.style.display = "none";
    } else {
        dropdown.style.display = "block";
    }
}

async function resetPassword() {
    const email = document.getElementById("email").value;
    const password = document.getElementById("newPassword").value;

    if (password.length < 6) {
        alert("Password must be at least 6 characters.");
        return;
    }

    try {
        const response = await fetch(
            `/donors/reset-password?email=${email}&password=${password}`,
            { method: "PUT" }
        );
        const message = await response.text();
        alert(message);
        window.location.href = "/donor/login.html";
    } catch(error){
        console.error(error);
        alert("Password Reset Failed");
    }
}

async function loadDonationHistory() {
    const donor = JSON.parse(localStorage.getItem("donor"));
    const response = await fetch(`/requests/history/donor/${donor.donorId}`);
    const history = await response.json();
    const historyList = document.getElementById("historyList");

    if (history.length === 0) {
        historyList.innerHTML = `
            <tr>
                <td colspan="5">No completed donations yet.</td>
            </tr>
        `;
        return;
    }

    historyList.innerHTML = "";
    history.forEach(request => {
        const date = new Date(request.requestDate).toLocaleString();
        historyList.innerHTML += `
            <tr>
                <td>${request.requestId}</td>
                <td>${request.recipientName}</td>
                <td>${date}</td>
                <td>${request.status}</td>
                <td>
                    <button onclick="downloadCertificate(${request.requestId})">Download PDF</button>
                </td>
            </tr>
        `;
    });
}

function downloadCertificate(requestId) {
    window.open(`/certificate/download/${requestId}`, "_blank");
}

function showSection(sectionId, menu){
    document.querySelectorAll(".section").forEach(section => {
        section.style.display="none";
    });

    document.getElementById(sectionId).style.display="block";

    document.querySelectorAll(".menu li").forEach(item=>{
        item.classList.remove("activeMenu");
    });

    if(menu){
        menu.classList.add("activeMenu");
    }

    if(sectionId==="requestsSection"){
        loadRequests();
    }

    if(sectionId==="historySection"){
        loadDonationHistory();
    }

    if (sectionId === "certificateSection") {
        loadCertificates();
    }
}

async function loadCertificates() {
    const donor = JSON.parse(localStorage.getItem("donor"));
    const response = await fetch(`/requests/history/donor/${donor.donorId}`);
    const history = await response.json();
    const certificateList = document.getElementById("certificateList");

    if (history.length === 0) {
        certificateList.innerHTML = `
            <tr>
                <td colspan="5">No certificates available yet. Complete a donation to unlock!</td>
            </tr>
        `;
        return;
    }

    certificateList.innerHTML = "";
    history.forEach(request => {
        const date = new Date(request.requestDate).toLocaleDateString();

        // Formats a clean certificate serial code locally (e.g., VD-2026-000042)
        const serialNo = `VD-${new Date(request.requestDate).getFullYear()}-${String(request.requestId).padStart(6, '0')}`;

        certificateList.innerHTML += `
            <tr>
                <td><strong>${serialNo}</strong></td>
                <td>${request.recipientName}</td>
                <td>${date}</td>
                <td><span style="color: var(--color-emerald); font-weight: 600;">✓ Verified</span></td>
                <td>
                    <button onclick="downloadCertificate(${request.requestId})">
                        Download PDF
                    </button>
                </td>
            </tr>
        `;
    });
}