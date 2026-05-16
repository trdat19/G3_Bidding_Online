package client.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Item {
    private String title;
    private String category;
    private String description;
    private double startPrice;
    private double currentPrice;
    private String leader;
    private String seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int bidCount;
    private Long id;

    public Item(String title, String category, String description,
                double startPrice, double currentPrice,
                String leader, LocalDateTime startTime, LocalDateTime endTime,
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }
}
