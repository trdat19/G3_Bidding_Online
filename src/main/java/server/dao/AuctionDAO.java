package server.dao;

import server.database.DBconnection;
import server.model.core.Auction;
import shared.dto.common.AuctionDTO;
import shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    // thêm auction mới vào bảng auctions
    public boolean insertAuction(Auction auction) {
        String sql = "INSERT INTO auctions(id_item, id_seller, start_price, max_price, min_increment, " +
                "buy_now_price, start_time, end_time, status_auction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, auction.getItemId());
            ps.setLong(2, auction.getSellerId());
            ps.setBigDecimal(3, auction.getStartPrice());
            ps.setBigDecimal(4, auction.getMaxPrice());
            ps.setBigDecimal(5, auction.getMinIncrement());
            ps.setBigDecimal(6, auction.getBuyNowPrice());
            ps.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));
            ps.setString(9, auction.getStatus().name());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        auction.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("insertAuction error: " + e.getMessage());
        }
        return false;
    }

    // tìm auction theo id_auction
    public Auction findById(long id) {
        String sql = "SELECT * FROM auctions WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findById error: " + e.getMessage());
        }
        return null;
    }

    // lấy toàn bộ auction trong bảng
    public List<Auction> getAllAuctions() {
        String sql = "SELECT * FROM auctions";
        List<Auction> auctions = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                auctions.add(mapResultSetToAuction(rs));
            }
        } catch (SQLException e) {
            System.err.println("getAllAuctions error: " + e.getMessage());
        }
        return auctions;
    }

    public List<AuctionDTO> getAdminAuctionSummaries() {
        String sql = """
                SELECT
                    a.id_auction,
                    a.id_item,
                    i.name_item AS item_name,
                    i.description AS item_description,
                    i.category AS item_category,
                    i.image_url AS item_image_url,
                    a.id_seller,
                    seller.full_name AS seller_name,
                    a.start_price,
                    a.max_price,
                    a.min_increment,
                    a.buy_now_price,
                    a.status_auction,
                    a.start_time,
                    a.end_time,
                    highest_bid.bidder_id AS leader_id,
                    leader.full_name AS leader_name,
                    COALESCE(bid_counts.bid_count, 0) AS bid_count
                FROM auctions a
                JOIN items i ON i.id_item = a.id_item
                LEFT JOIN users seller ON seller.id = a.id_seller
                LEFT JOIN (
                    SELECT ranked.auction_id, ranked.bidder_id
                    FROM (
                        SELECT
                            b.auction_id,
                            b.bidder_id,
                            ROW_NUMBER() OVER (
                                PARTITION BY b.auction_id
                                ORDER BY b.bid_amount DESC, b.bid_time ASC, b.id ASC
                            ) AS rn
                        FROM bids b
                    ) ranked
                    WHERE ranked.rn = 1
                ) highest_bid ON highest_bid.auction_id = a.id_auction
                LEFT JOIN users leader ON leader.id = highest_bid.bidder_id
                LEFT JOIN (
                    SELECT auction_id, COUNT(*) AS bid_count
                    FROM bids
                    GROUP BY auction_id
                ) bid_counts ON bid_counts.auction_id = a.id_auction
                ORDER BY a.id_auction DESC
                """;

        List<AuctionDTO> auctions = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                auctions.add(mapResultSetToAdminAuctionDTO(rs));
            }

        } catch (SQLException e) {
            System.err.println("getAdminAuctionSummaries error: " + e.getMessage());
        }

        return auctions;
    }

    // lấy tất cả auction của một seller
    public List<Auction> getAllAuctionsBySellerId(long sellerId) {
        String sql = "SELECT * FROM auctions WHERE id_seller = ?";
        List<Auction> auctions = new ArrayList<>();
        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    auctions.add(mapResultSetToAuction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAllAuctionsBySellerId error: " + e.getMessage());
        }
        return auctions;
    }

    // lấy tất cả auction của một item
    // dùng để xem lịch sử đấu giá của món hàng đó
    public List<Auction> getAllAuctionsByItemId(long itemId) {
        String sql = "SELECT * FROM auctions WHERE id_item = ? ORDER BY id_auction DESC";
        List<Auction> auctions = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    auctions.add(mapResultSetToAuction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAllAuctionsByItemId error: " + e.getMessage());
        }
        return auctions;
    }

    // lấy auction theo trạng thái
    public List<Auction> getAllAuctionsByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM auctions WHERE status_auction = ?";
        List<Auction> auctions = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    auctions.add(mapResultSetToAuction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAllAuctionsByStatus error: " + e.getMessage());
        }
        return auctions;
    }

    // tìm auction đang OPEN của một item
    // dùng khi cần kiểm tra item này hiện có đang được đấu giá hay không
    public Auction findOpeningAuctionsByItemId(long itemId) {
        String sql = "SELECT * FROM auctions WHERE id_item = ? AND status_auction = 'OPEN' LIMIT 1";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findOpeningAuctionsByItemId error: " + e.getMessage());
        }
        return null;
    }

    // cập nhật toàn bộ thông tin của auction
    // DB vẫn sẽ tự kiểm tra lại các ràng buộc check
    public boolean updateAuction(Auction auction) {
        String sql = "UPDATE auctions SET id_item = ?, id_seller = ?, start_price = ?, max_price = ?, " +
                "min_increment = ?, buy_now_price = ?, start_time = ?, end_time = ?, status_auction = ? " +
                "WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, auction.getItemId());
            ps.setLong(2, auction.getSellerId());
            ps.setBigDecimal(3, auction.getStartPrice());
            ps.setBigDecimal(4, auction.getMaxPrice());
            ps.setBigDecimal(5, auction.getMinIncrement());
            ps.setBigDecimal(6, auction.getBuyNowPrice());
            ps.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));
            ps.setString(9, auction.getStatus().name());
            ps.setLong(10, auction.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateAuction error: " + e.getMessage());
        }
        return false;
    }

    // cập nhật riêng trạng thái của auction
    public boolean updateStatus(long idAuction, AuctionStatus status) {
        String sql = "UPDATE auctions SET status_auction = ? WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, idAuction);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateStatus error: " + e.getMessage());
        }
        return false;
    }

    // cập nhật riêng giá cao nhất hiện tại
    // dùng khi có người bid thành công
    public boolean updateMaxPrice(long idAuction, BigDecimal newMaxPrice) {
        String sql = """
            UPDATE auctions
            SET max_price = ?
            WHERE id_auction = ?
              AND (max_price IS NULL OR max_price < ?)
            """;

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, newMaxPrice);
            ps.setLong(2, idAuction);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateMaxPrice error: " + e.getMessage());
        }
        return false;
    }

    // kiểm tra auction có tồn tại không
    public boolean existsAuctionById(long idAuction) {
        String sql = "SELECT 1 FROM auctions WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAuction);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsById error: " + e.getMessage());
        }
        return false;
    }

    // kiểm tra item này có auction OPEN chưa
    public boolean existsOpenAuctionByItemId(long itemId) {
        String sql = "SELECT 1 FROM auctions WHERE id_item = ? AND status_auction = 'OPEN'";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsOpenAuctionByItemId error: " + e.getMessage());
        }
        return false;
    }
    //Kiem tra item nay co auction chưa
    public boolean existsAuctionByItemId(long itemId) {
        String sql = "SELECT 1 FROM auctions WHERE id_item = ? LIMIT 1";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsAuctionByItemId error: " + e.getMessage());
        }
        return false;
    }

    // xóa auction theo id
    public boolean deleteAuction(long idAuction) {
        String sql = "DELETE FROM auctions WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAuction);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteAuction error: " + e.getMessage());
            return false;
        }
    }


    // chuyển dữ liêu DB thành Object
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        Auction auction = new Auction();

        auction.setId(rs.getLong("id_auction"));
        auction.setItemId(rs.getLong("id_item"));
        auction.setSellerId(rs.getLong("id_seller"));
        auction.setStartPrice(rs.getBigDecimal("start_price"));
        auction.setMaxPrice(rs.getBigDecimal("max_price"));
        auction.setMinIncrement(rs.getBigDecimal("min_increment"));
        auction.setBuyNowPrice(rs.getBigDecimal("buy_now_price"));
        auction.setStatus(AuctionStatus.valueOf(rs.getString("status_auction")));
        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            auction.setStartTime(startTs.toLocalDateTime());
        }
        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) {
            auction.setEndTime(endTs.toLocalDateTime());
        }

        return auction;
    }

    private AuctionDTO mapResultSetToAdminAuctionDTO(ResultSet rs) throws SQLException {
        AuctionDTO dto = new AuctionDTO();

        dto.setId(rs.getLong("id_auction"));
        dto.setItemId(rs.getLong("id_item"));
        dto.setItemName(rs.getString("item_name"));
        dto.setItemDescription(rs.getString("item_description"));
        dto.setItemCategory(rs.getString("item_category"));
        dto.setItemImageUrl(rs.getString("item_image_url"));
        dto.setSellerId(rs.getLong("id_seller"));

        String sellerName = rs.getString("seller_name");
        dto.setSellerName(sellerName != null ? sellerName : "Khong xac dinh");

        dto.setStartPrice(rs.getBigDecimal("start_price"));
        BigDecimal maxPrice = rs.getBigDecimal("max_price");
        dto.setCurrentPrice(maxPrice != null ? maxPrice : dto.getStartPrice());
        dto.setMinIncrement(rs.getBigDecimal("min_increment"));
        dto.setBuyNowPrice(rs.getBigDecimal("buy_now_price"));
        dto.setStatus(AuctionStatus.valueOf(rs.getString("status_auction")));

        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            dto.setStartTime(startTime.toLocalDateTime());
        }

        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            dto.setEndTime(endTime.toLocalDateTime());
        }

        long leaderId = rs.getLong("leader_id");
        if (!rs.wasNull()) {
            dto.setLeaderId(leaderId);

            String leaderName = rs.getString("leader_name");
            dto.setLeaderName(leaderName != null ? leaderName : "ID: " + leaderId);
        }

        dto.setBidCount(rs.getInt("bid_count"));

        return dto;
    }

    public boolean deleteAuctionsByItemId(long itemId) {
        String sql = "DELETE FROM auctions WHERE id_item = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("deleteAuctionsByItemId error: " + e.getMessage());
            return false;
        }
    }

    public boolean existsActiveAuctionByItemId(long itemId) {
        String sql = """
            SELECT 1
            FROM auctions
            WHERE id_item = ?
              AND status_auction IN ('WAITING_APPROVAL', 'OPEN', 'RUNNING')
            LIMIT 1
            """;

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("existsActiveAuctionByItemId error: " + e.getMessage());
            return false;
        }
    }
    public long countAuctionsByStatus(AuctionStatus status) {
        String sql = "SELECT COUNT(*) FROM auctions WHERE status_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("countAuctionsByStatus error: " + e.getMessage());
        }

        return 0;
    }
}
