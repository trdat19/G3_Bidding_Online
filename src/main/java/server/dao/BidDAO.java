package server.dao;

import server.database.DBConnection;
import server.model.core.Bid;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    // thêm 1 lần bid mới vào bảng bids
    public boolean insertBid(Bid bid) {
        String sql = """
                    INSERT INTO bids (auction_id, bidder_id, bid_amount, is_auto_bid) VALUES (?, ?, ?, ?)
                    """;

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bid.getAuctionId());
            ps.setLong(2, bid.getBidderId());
            ps.setBigDecimal(3, bid.getAmount());
            ps.setBoolean(4, bid.getIsAutoBid());

            int rowsAffected = ps.executeUpdate();

            // nếu insert thành công thì lấy id tự tăng từ DB gán lại cho object bid
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bid.setId(rs.getLong(1));

                        // lấy lại bid_time từ DB
                        Bid inserted = findById(bid.getId());
                        if (inserted != null) {
                            bid.setTimestamp(inserted.getTimestamp());
                            bid.setIsAutoBid(inserted.getIsAutoBid());
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lượt đặt giá: " + e.getMessage());
        }
        return false;
    }

    // tìm bid theo id
    public Bid findById(Long id) {
        String sql = "SELECT * FROM bids WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBid(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lượt đặt giá theo ID: " + e.getMessage());
        }
        return null;
    }

    // lấy toàn bộ các bid trong bảng
    public List<Bid> getAllBids() {
        String sql = "SELECT * FROM bids";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả các lượt đặt giá: " + e.getMessage());
        }
        return bids;
    }

    // lấy toàn bộ bid của một auction
    public List<Bid> getAllBidsInAuctionId(Long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC, bid_time ASC";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bids.add(mapResultSetToBid(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả các lượt đặt giá trong phiên: " + e.getMessage());
        }
        return bids;
    }

    // lấy toàn bộ bid của một bidder
    public List<Bid> getAllBidsByBidderId(Long bidderId) {
        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY bid_time DESC";
        List<Bid> bids = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
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
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC, bid_time ASC, id ASC LIMIT 1";

        try (Connection con = DBConnection.getInstance().getConnection();
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

        try (Connection con = DBConnection.getInstance().getConnection();
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
    public Long countBidByAuctionId(Long auctionId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE auction_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("countByAuctionId error: " + e.getMessage());
        }
        return 0L;
    }

    // kiểm tra bid có tồn tại theo id hay không
    public boolean existsBidById(long id) {
        String sql = "SELECT 1 FROM bids WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
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

        try (Connection con = DBConnection.getInstance().getConnection();
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

        bid.setIsAutoBid(rs.getBoolean("is_auto_bid"));
        return bid;
    }


    public BigDecimal getReservedAmountByBidderIdExcludingAuction(
            Long bidderId, Long excludedAuctionId) {
        String sql = """
                SELECT COALESCE(SUM(b.bid_amount), 0) AS reserved_amount
                FROM bids b
                JOIN auctions a ON a.id_auction = b.auction_id
                WHERE b.bidder_id = ?
                  AND b.auction_id <> ?
                  AND a.status_auction IN ('OPEN', 'RUNNING')
                  AND b.id = (
                      SELECT b2.id
                      FROM bids b2
                      WHERE b2.auction_id = b.auction_id
                      ORDER BY b2.bid_amount DESC, b2.bid_time ASC, b2.id ASC
                      LIMIT 1
                  )
                """;

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, bidderId);
            ps.setLong(2, excludedAuctionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("reserved_amount");
                }
            }
        } catch (SQLException e) {
            System.err.println("getReservedAmount error: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }
}