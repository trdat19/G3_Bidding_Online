package client.model;

import java.time.LocalDate;

public class Item {
    private String title;
    private String category;
    private String description;
    private double startPrice;
    private double currentPrice;
    private String leader;
    private LocalDate startTime;
    private LocalDate endTime;
    private String status;
    private int bidCount;

    public Item(String title, String category, String description,
                double startPrice, double currentPrice,
                String leader, LocalDate startTime, LocalDate endTime,
                String status, int bidCount) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.leader = leader;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.bidCount = bidCount;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getLeader() {
        return leader;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public int getBidCount() {
        return bidCount;
    }
}
