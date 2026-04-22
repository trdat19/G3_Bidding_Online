package server.dao;

import server.database.DBconnection;
import server.model.core.Auction;
import shared.enums.AuctionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDao {

    // thêm auction mới vào bảng auctions
    public boolean insertAuction(Auction auction) {
        String sql = "INSERT INTO auctions(id_item, id_seller, start_price, max_price, min_increment, " +
                "buy_now_price, start_time, end_time, status_auction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, auction.getItemId());
            ps.setLong(2, auction.getSellerId());
            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getMax_price());
            ps.setDouble(5, auction.getMin_increment() > 0 ? auction.getMin_increment() : 1.0);
            ps.setObject(6,
                    auction.getBuy_now_price() > 0 ? auction.getBuy_now_price() : null,
                    Types.DECIMAL);
            ps.setTimestamp(7, auction.getStartTime());
            ps.setTimestamp(8, auction.getEndTime());
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
    public List<Auction> findAll() {
        String sql = "SELECT * FROM auctions";
        List<Auction> auctions = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                auctions.add(mapResultSetToAuction(rs));
            }
        } catch (SQLException e) {
            System.err.println("findAll error: " + e.getMessage());
        }
        return auctions;
    }

    // lấy tất cả auction của một seller
    public List<Auction> findBySellerId(long sellerId) {
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
            System.err.println("findBySellerId error: " + e.getMessage());
        }
        return auctions;
    }

    // lấy tất cả auction của một item
    // dùng để xem lịch sử đấu giá của món hàng đó
    public List<Auction> findByItemId(long itemId) {
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
            System.err.println("findByItemId error: " + e.getMessage());
        }
        return auctions;
    }

    // lấy auction theo trạng thái
    public List<Auction> findByStatus(AuctionStatus status) {
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
            System.err.println("findByStatus error: " + e.getMessage());
        }
        return auctions;
    }

    // tìm auction đang OPEN của một item
    // dùng khi cần kiểm tra item này hiện có đang được đấu giá hay không
    public Auction findOpenByItemId(long itemId) {
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
            System.err.println("findOpenByItemId error: " + e.getMessage());
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
            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getMax_price());
            ps.setDouble(5, auction.getMin_increment() > 0 ? auction.getMin_increment() : 1.0);
            ps.setObject(6,
                    auction.getBuy_now_price() > 0 ? auction.getBuy_now_price() : null,
                    Types.DECIMAL);
            ps.setTimestamp(7, auction.getStartTime());
            ps.setTimestamp(8, auction.getEndTime());
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
    public boolean updateMaxPrice(long idAuction, double newMaxPrice) {
        String sql = "UPDATE auctions SET max_price = ? WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newMaxPrice);
            ps.setLong(2, idAuction);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateMaxPrice error: " + e.getMessage());
        }
        return false;
    }

    // mở auction
    public boolean openAuction(long idAuction) {
        return updateStatus(idAuction, AuctionStatus.OPEN);
    }

    // đóng auction
    public boolean closeAuction(long idAuction) {
        return updateStatus(idAuction, AuctionStatus.CLOSED);
    }

    // hủy auction
    public boolean cancelAuction(long idAuction) {
        return updateStatus(idAuction, AuctionStatus.CANCELLED);
    }

    // kiểm tra auction có tồn tại không
    public boolean existsById(long idAuction) {
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

    // xóa auction theo id
    public boolean deleteAuction(long idAuction) {
        String sql = "DELETE FROM auctions WHERE id_auction = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAuction);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteAuction error: " + e.getMessage());
        }
        return false;
    }

    // chuyển dữ liêu DB thành Object
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_auction");
        Long itemId = rs.getLong("id_item");
        Long sellerId = rs.getLong("id_seller");
        double startPrice = rs.getDouble("start_price");
        Timestamp startTime = rs.getTimestamp("start_time");
        Timestamp endTime = rs.getTimestamp("end_time");
        Auction auction = new Auction(id, itemId, sellerId, startPrice, startTime, endTime);
        auction.setStatus(AuctionStatus.valueOf(rs.getString("status_auction")));
        auction.setMax_price(rs.getDouble("max_price"));
        auction.setMin_increment(rs.getDouble("min_increment"));

        double buyNowPrice = rs.getDouble("buy_now_price");
        if (!rs.wasNull()) {
            auction.setBuy_now_price(buyNowPrice);
        }
        return auction;
    }
}