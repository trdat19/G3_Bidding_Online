package server.dao;

import server.database.DBconnection;
import server.model.core.Auction;
import shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

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
        String sql = "UPDATE auctions SET max_price = ? WHERE id_auction = ?";

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
            auction.setStartTime(endTs.toLocalDateTime());
        }
        return auction;
    }
}