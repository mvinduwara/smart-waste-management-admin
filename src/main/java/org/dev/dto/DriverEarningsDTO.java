package org.dev.dto;

import java.util.List;

public class DriverEarningsDTO {

    private double todaysPayout;
    private double totalWasteCollected;
    private List<CollectionHistoryDTO> history;

    public double getTodaysPayout() {
        return todaysPayout;
    }

    public void setTodaysPayout(double todaysPayout) {
        this.todaysPayout = todaysPayout;
    }

    public double getTotalWasteCollected() {
        return totalWasteCollected;
    }

    public void setTotalWasteCollected(double totalWasteCollected) {
        this.totalWasteCollected = totalWasteCollected;
    }

    public List<CollectionHistoryDTO> getHistory() {
        return history;
    }

    public void setHistory(List<CollectionHistoryDTO> history) {
        this.history = history;
    }
}
