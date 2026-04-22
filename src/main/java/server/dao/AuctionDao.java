package server.dao;

import server.config.DBconnection;
import server.model.core.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class AuctionDao {

    // thêm Auction vào bảng auctions
    public boolean insertAuction (Auction auction) {
        String sql =" INSERT INTO auctions(id_item, id_seller, start_price, max_price, min_increment, " +
        "buy_now_price, start_time, end_time, status_auction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1,);
        }
    }
}