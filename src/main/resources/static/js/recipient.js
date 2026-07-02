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

async function registerRecipient() {
    const recipient = {
        fullName: document.getElementById("fullName").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        phone: document.getElementById("phone").value,
        bloodGroupNeeded: document.getElementById("bloodGroupNeeded").value,
        city: document.getElementById("city").value
    };

    try {
        const response = await fetch(
            "/recipients/register",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(recipient)
            }
        );

        if(response.ok) {
            alert("Registration Successful");
            window.location.href = "/recipient/login.html";
        } else {
            const error = await response.text();
            alert(error);
        }
    } catch(error) {
        console.error(error);
        alert("Registration Failed");
    }
}

async function loginRecipient() {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const status = document.getElementById("loginStatus");

    try {
        const response = await fetch(
            `/recipients/login?email=${email}&password=${password}`,
            { method: "POST" }
        );

        if(response.ok) {
            const recipient = await response.json();
            localStorage.setItem("recipient", JSON.stringify(recipient));
            alert("Login Successful");
            window.location.href = "/recipient/dashboard.html";
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

window.onload = function () {
    if (!window.location.pathname.includes("dashboard")) {
        return;
    }

    const recipient = JSON.parse(localStorage.getItem("recipient"));
    if (!recipient) {
        alert("Please login first");
        window.location.href = "/recipient/login.html";
        return;
    }

    document.getElementById("welcomeText").innerText = `Welcome, ${recipient.fullName}`;

    // Initial data load for the landing view
    loadRecipientHistory();
};

function logoutRecipient() {
    localStorage.removeItem("recipient");
    window.location.href = "/recipient/login.html";
}

async function loadDonors() {
    const bloodGroup = document.getElementById("bloodGroup").value;
    if (!bloodGroup) {
        alert("Please select a blood group");
        return;
    }

    const recipient = JSON.parse(localStorage.getItem("recipient"));

    try {
        // Fetch active requests first to track who has already been pinged
        const requestsResponse = await fetch(`/requests/recipient/${recipient.recipientId}`);
        const existingRequests = await requestsResponse.json();

        // Fetch matched available donors
        const donorsResponse = await fetch(`/donors/search?bloodGroup=${encodeURIComponent(bloodGroup)}`);
        const donors = await donorsResponse.json();

        const donorList = document.getElementById("donorList");

        if (donors.length === 0) {
            donorList.innerHTML = `
                <tr>
                    <td colspan="5">No available donors found.</td>
                </tr>
            `;
            return;
        }

        donorList.innerHTML = "";
        donors.forEach(donor => {
            // Find if there's an ongoing interaction with this specific donor
            const ongoingRequest = existingRequests.find(req =>
                req.donorId === donor.donorId &&
                (req.status === "PENDING" || req.status === "ACCEPTED")
            );

            let actionButtonMarkup = "";

            if (ongoingRequest) {
                // If a request exists and isn't finalized, lock the operation
                const labelText = ongoingRequest.status === "PENDING" ? "Request Pending" : "Request Active";
                actionButtonMarkup = `<button disabled style="background:#1e293b; color:#475569; border:1px solid rgba(255,255,255,0.05); cursor:not-allowed;">${labelText}</button>`;
            } else {
                // Otherwise, leave it wide open for interaction
                actionButtonMarkup = `<button onclick="sendRequest(this, ${donor.donorId})">Request Blood</button>`;
            }

            donorList.innerHTML += `
                <tr>
                    <td>${donor.fullName}</td>
                    <td>${donor.bloodGroup}</td>
                    <td>${donor.city}</td>
                    <td>${donor.availability ? "Available" : "Not Available"}</td>
                    <td>${actionButtonMarkup}</td>
                </tr>
            `;
        });
    } catch (error) {
        console.error("Error loading donor directory:", error);
    }
}

async function sendRequest(buttonElement, donorId) {
    const recipient = JSON.parse(localStorage.getItem("recipient"));

    // 1. Instantly disable the clicked button and show an inline spinner state
    buttonElement.disabled = true;
    const originalText = buttonElement.innerHTML;
    buttonElement.innerHTML = `<span class="spinner"></span> Processing...`;

    try {
        const response = await fetch(
            "/requests/send",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    donorId: donorId,
                    recipientId: recipient.recipientId
                })
            }
        );

        const message = await response.text();
        alert(message);

        // 2. Change state to pending permanently instead of turning back on
        buttonElement.innerHTML = "Request Pending";
        buttonElement.style.background = "#1e293b";
        buttonElement.style.color = "#475569";
        buttonElement.style.border = "1px solid rgba(255,255,255,0.05)";
        buttonElement.style.cursor = "not-allowed";

    } catch(error) {
        console.error(error);
        alert("Failed to send request");

        // Network Failure Fallback: Re-unlock the trigger if dispatch fails completely
        buttonElement.disabled = false;
        buttonElement.innerHTML = originalText;
    }
}


async function loadMyRequests() {
    const recipient = JSON.parse(localStorage.getItem("recipient"));
    const response = await fetch(`/requests/recipient/${recipient.recipientId}`);
    const requests = await response.json();
    const requestList = document.getElementById("myRequestList");

    if (requests.length === 0) {
        requestList.innerHTML = `
            <tr>
                <td colspan="5">No requests found.</td>
            </tr>
        `;
        return;
    }

    requestList.innerHTML = "";
    requests.forEach(request => {
        const status = request.status;
        let stepperHtml = '';
        const date = new Date(request.requestDate).toLocaleString();
        let action = "";

        // NEW: Inline visual timeline tracking framework
        if (status === "PENDING") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Awaiting Donor" style="color: #fbbf24; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●<span style="color:#475569;">──●──●</span> <span style="font-size: 11px; margin-left: 5px; color: #fbbf24;">(Pending)</span>
                </div>`;
            action = "Waiting for Donor";
        } else if (status === "ACCEPTED") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Donor En Route" style="color: #10b981; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●──●<span style="color:#475569;">──●</span> <span style="font-size: 11px; margin-left: 5px; color: #10b981;">(Active)</span>
                </div>`;
            action = `
                <button onclick="confirmDonation(${request.requestId})">
                    Confirm Blood Received
                </button>
            `;
        } else if (status === "COMPLETED") {
            stepperHtml = `
                <div class="progress-stepper" title="Status: Transaction Success" style="color: #6366f1; display: inline-flex; align-items: center; gap: 4px; font-weight: bold;">
                    ●──●──● <span style="font-size: 11px; margin-left: 5px; color: #6366f1;">(Completed)</span>
                </div>`;
            action = `<span style="color: #6366f1; font-weight: 600;">Completed</span>`;
        } else {
            stepperHtml = `<span style="color:#ef4444; font-weight:bold;">CANCELLED</span>`;
            action = "Rejected";
        }

        requestList.innerHTML += `
            <tr>
                <td>${request.requestId}</td>
                <td><strong>${request.donorName}</strong></td>
                <td>${stepperHtml}</td> <td>${date}</td>
                <td>${action}</td>
            </tr>
        `;
    });
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
            `/recipients/reset-password?email=${email}&password=${password}`,
            { method: "PUT" }
        );
        const message = await response.text();
        alert(message);
        window.location.href = "/recipient/login.html";
    } catch (error) {
        console.error(error);
        alert("Password Reset Failed");
    }
}

async function confirmDonation(requestId) {
    const response = await fetch(
        `/requests/complete/${requestId}`,
        { method: "PUT" }
    );

    if (response.ok) {
        alert("Donation confirmed successfully!");
        loadMyRequests();
    } else {
        const error = await response.text();
        alert(error);
    }
}

async function loadRecipientHistory() {
    const recipient = JSON.parse(localStorage.getItem("recipient"));
    const response = await fetch(
        `/requests/history/recipient/${recipient.recipientId}`
    );
    const history = await response.json();
    const historyList = document.getElementById("recipientHistoryList");

    if (history.length === 0) {
        historyList.innerHTML = `
            <tr>
                <td colspan="4">No completed donations.</td>
            </tr>
        `;
        return;
    }

    historyList.innerHTML = "";
   // Inside loadRecipientHistory() in recipient.js
   history.forEach(request => {
       const date = new Date(request.requestDate).toLocaleString();
       historyList.innerHTML += `
           <tr>
               <td>${request.requestId}</td>
               <td><strong>${request.donorName}</strong>
               </td> <td>${date}</td>
               <td>${request.status}</td>
           </tr>
       `;
   });
}

/* --- ENTERPRISE SIDEBAR NAV SWITCHER --- */
/* This handles tab displays AND triggers backend fetches automatically */
function showSection(sectionId, element) {
    // 1. Hide all application content view panes
    document.querySelectorAll('.section').forEach(s => s.style.display = 'none');

    // 2. Clear out highlighted visual tags across menu anchors
    document.querySelectorAll('.menu li').forEach(li => li.classList.remove('activeMenu'));

    // 3. Render target pane visible and activate menu lighting state
    document.getElementById(sectionId).style.display = 'block';
    element.classList.add('activeMenu');

    // 4. Dynamic Data Extraction: Run data queries depending on active panel tab
    if (sectionId === 'myRequestsSection') {
        loadMyRequests();
    } else if (sectionId === 'historySection') {
        loadRecipientHistory();
    }
}