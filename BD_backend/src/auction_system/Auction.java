package auction_system.model;

import auction_system.model.item.Item;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Auction {
    private String id;
    private Item item;
    private List<Bid> bids;
    private LocalDateTime startTime, endTime;
    private Bid highestBid;

    public Auction(String id, Item item, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
       // this.item = new Item(), chắc là phải dùng ItemFactory để tạo ra Item,
        // không gán this.item = item vì để đảm bảo tính đóng gói.

        this.bids = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addBid(Bid bid) {
        bids.add(bid);
        // highestBid = .... viết phương thức hay hàm để lấy ra highestBid trong bids mỗi khi add thêm bid mới
    }

    public Bid getHighestBid() {
        return highestBid;
    }
}
