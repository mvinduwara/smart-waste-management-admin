package org.dev.dto;

import java.util.List;

public class SellerHistoryDTO {
    private int totalRequests;
    private double totalEarned;
    private List<PickupRequestDTO> history;

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public List<PickupRequestDTO> getHistory() {
        return history;
    }

    public void setHistory(List<PickupRequestDTO> history) {
        this.history = history;
    }
}
