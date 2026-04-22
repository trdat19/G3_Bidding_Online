package server.dao;

import server.config.DBconnection;
import server.model.core.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AuctionDao {

    // thêm Auction vào bảng auctions
    public boolean insertAuction (Auction auction) {
        String sql =" INSERT INTO auctions(id_item, id_seller, start_price, max_price, min_increment, " +
                "buy_now_price, start_time, end_time, status_auction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, auction.getItemId());
            ps.setLong(2, auction.getSellerId());
            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getMaxPrice());
            ps.setDouble(5, auction.getMinIncrement());
            ps.setDouble(6, auction.getBuyNowPrice());
            ps.setTimestamp(7, auction.getStartTime());
            ps.setTimestamp(8, auction.getEndTime());
            ps.setString(9, (auction.getStatus() != null) ? auction.getStatus().name() : "OPEN");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}