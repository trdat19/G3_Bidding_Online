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
    private String imageUrl;
    private byte[] imageBytes;
    private String imageContentType;

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
    //setter
    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setLeader(String leader) {
        this.leader =leader;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime startTime) {
        this.endTime = endTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBidCount(int bidCount) { this.bidCount = bidCount;}

    public void setId(Long id) {this.id = id;}

    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public void setImageBytes(byte[] imageBytes) {this.imageBytes = imageBytes;}
    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    //getter
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

    public Long getId() {return id;}

    public String getImageUrl() {return imageUrl;}
    public byte[] getImageBytes() {
        return imageBytes;
    }
    public String getImageContentType() {
        return imageContentType;
    }
}
