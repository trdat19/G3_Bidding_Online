package shared.dto;

import shared.enums.AuctionStatus;

import java.io.Serializable;

public class AuctionDTO implements Serializable {
    private Long id;
    private Long itemId;
    private String itemName;
    private long sellerId;
    private double currentPrice;
    private AuctionStatus status;
    private long endTime; // Sử dụng timestamp để dễ tính countdown trên Client
    private String leaderName; // Tên người đang dẫn đầu để hiển thị

    public AuctionDTO() {}

    // Constructor, Getters và Setters
}