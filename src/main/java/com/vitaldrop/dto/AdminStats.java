package com.vitaldrop.dto;

public class AdminStats {

    private long totalDonors;

    private long totalRecipients;

    private long availableDonors;

    private long pendingRequests;

    private long acceptedRequests;

    public long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public long getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(long totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public long getAvailableDonors() {
        return availableDonors;
    }

    public void setAvailableDonors(long availableDonors) {
        this.availableDonors = availableDonors;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getAcceptedRequests() {
        return acceptedRequests;
    }

    public void setAcceptedRequests(long acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }
}