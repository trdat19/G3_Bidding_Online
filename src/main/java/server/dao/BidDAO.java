package server.dao;

import server.Database.DBconnection;
import server.model.core.Bid;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    // thêm 1 lần bid mới vào bảng bids
    public boolean insertBid(Bid bid) {
        String sql = "INSERT INTO bids(auction_id, bidder_id, bid_amount) VALUES (?, ?, ?)";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bid.getAuctionId());
            ps.setLong(2, bid.getBidderId());
            ps.setBigDecimal(3, bid.getAmount());

            int rowsAffected = ps.executeUpdate();

            // nếu insert thành công thì lấy id tự tăng từ DB gán lại cho object bid
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bid.setId(rs.getLong(1));

                        // lấy lại bid_time từ DB
                        Bid inserted = findById(bid.getId());
                        bid.setTimestamp(inserted.getTimestamp());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("insertBid error: " + e.getMessage());
        }
        return false;
    }

    // tìm bid theo id
    public Bid findById(long id) {
        String sql = "SELECT * FROM bids WHERE id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBid(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findById error: " + e.getMessage());
        }
        return null;
    }

    // lấy toàn bộ các bid trong bảng
    public List<Bid> getAllBids() {
        String sql = "SELECT * FROM bids";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }

        } catch (SQLException e) {
            System.err.println("getAllBids error: " + e.getMessage());
        }
        return bids;
    }

    // lấy toàn bộ bid của một auction
    public List<Bid> getAllBidsInAuctionId(long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC, bid_time ASC";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bids.add(mapResultSetToBid(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAllBidsInAuctionId error: " + e.getMessage());
        }
        return bids;
    }

    // lấy toàn bộ bid của một bidder
    public List<Bid> getAllBidsByBidderId(long bidderId) {
        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY bid_time DESC";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, bidderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bids.add(mapResultSetToBid(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAllBidsByBidderId error: " + e.getMessage());
        }
        return bids;
    }

    // lấy bid cao nhất của một auction
    // dùng khi cần xác định người đang dẫn đầu phiên đấu giá
    public Bid getHighestBidByAuctionId(long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC, bid_time ASC LIMIT 1";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBid(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getHighestBidByAuctionId error: " + e.getMessage());
        }
        return null;
    }

    // lấy bid mới nhất của một auction
    // dùng khi muốn xem lần đặt giá cuối cùng theo thời gian
    public Bid getLatestBidByAuctionId(long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_time DESC, id DESC LIMIT 1";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBid(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getLatestBidByAuctionId error: " + e.getMessage());
        }
        return null;
    }

    // đếm số lần bid của một auction
    public int countBidByAuctionId(long auctionId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE auction_id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("countByAuctionId error: " + e.getMessage());
        }
        return 0;
    }

    // kiểm tra bid có tồn tại theo id hay không
    public boolean existsBidById(long id) {
        String sql = "SELECT 1 FROM bids WHERE id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsBidById error: " + e.getMessage());
        }
        return false;
    }

    // xóa bid theo id
    public boolean deleteBidById(long id) {
        String sql = "DELETE FROM bids WHERE id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("deleteBidById error: " + e.getMessage());
        }
        return false;
    }

    // chuyển dữ liệu DB thành Object
    private Bid mapResultSetToBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();

        bid.setId(rs.getLong("id"));
        bid.setAuctionId(rs.getLong("auction_id"));
        bid.setBidderId(rs.getLong("bidder_id"));
        bid.setAmount(rs.getBigDecimal("bid_amount"));

        Timestamp ts = rs.getTimestamp("bid_time");
        if (ts != null) {
            bid.setTimestamp(ts.toLocalDateTime());
        }
        return bid;
    }
}