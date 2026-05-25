package server.dao;

import server.database.DBconnection;
import server.model.core.Auction;
import server.model.core.AutoBidRule;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutoBidRuleDAO {

    //-----------------INSERT/UPDATE------------------------
    public boolean insertRule(AutoBidRule rule) {
        String sql = """
                INSERT INTO auto_bid_rules(auction_id, bidder_id, max_amount, step_amount, is_active)
                VALUES (?, ?, ?, ?, ?)""";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, rule.getAuctionId());
            ps.setLong(2, rule.getBidderId());
            ps.setBigDecimal(3, rule.getMaxAmount());
            ps.setBigDecimal(4, rule.getStepAmount());
            ps.setBoolean(5, rule.getIsActive());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        rule.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo Auto Bid Rule: " + e.getMessage());
        }
        return false;
    }

    //nếu đã có rule thì chỉ update, nếu chưa thì thêm
    public boolean saveOrUpdateRule(AutoBidRule rule) {
        AutoBidRule existingRule = findByAuctionIdAndBidderId(rule.getAuctionId(), rule.getBidderId());

        if (existingRule == null) {
            return insertRule(rule);
        }

        return updateMaxAmountAndSetActive(existingRule.getId(), rule.getMaxAmount(), rule.getIsActive());
    }

    //--------------------------------FIND--------------------
    /** Tìm theo id*/
    public AutoBidRule findById(Long id) {
        String sql ="SELECT * FROM auto_bid_rules WHERE id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAutoBidRule(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findById lỗi: " + e.getMessage());
        }
        return null;
    }

    /** Tìm theo bidderId trong 1 auction*/
    public AutoBidRule findByAuctionIdAndBidderId(Long auctionId, Long bidderId) {
        String sql = "SELECT * FROM auto_bid_rules WHERE auction_id = ? AND bidder_id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, auctionId);
            ps.setLong(2, bidderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAutoBidRule(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findByAuctionIdAndBidderId lỗi: " + e.getMessage());
        }
        return null;
    }

    //---------------------GET------------------------
    /** Lấy toàn bộ active rule trong 1 auction */
    public List<AutoBidRule> getActiveRulesByAuctionId(Long auctionId) {
        String sql = """
                SELECT * FROM auto_bid_rules
                WHERE auction_id = ? AND is_active = TRUE
                ORDER BY max_amount DESC, created_at ASC
                """;

        List<AutoBidRule> rules = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rules.add(mapResultSetToAutoBidRule(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getActiveRulesByAuctionId lỗi: " + e.getMessage());
        }
        return rules;
    }

    /** Lấy các rule active có maxAmount lớn hơn giá hiện tại */
    public List<AutoBidRule> getEligibleRules(Long auctionId, Long bidderId, BigDecimal currentPrice) {
        String sql = """
                SELECT * FROM auto_bid_rules
                WHERE auction_id = ?
                AND bidder_id <> ?
                AND is_active = TRUE
                AND max_amount > ?
                ORDER BY max_amount DESC, created_at ASC
                """;

        List<AutoBidRule> rules = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, auctionId);
            ps.setLong(2, bidderId);
            ps.setBigDecimal(3, currentPrice);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rules.add(mapResultSetToAutoBidRule(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getEligibleRules lỗi: " + e.getMessage());
        }
        return rules;
    }

    //---------------------UPDATE------------------
    /** update mức autobid */
    public boolean updateMaxAmountAndSetActive(Long id, BigDecimal maxAmount, boolean active) {
        String sql = """
                UPDATE auto_bid_rules
                SET max_amount = ?, is_active = ?
                WHERE id = ?
                """;

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, maxAmount);
            ps.setBoolean(2, active);
            ps.setLong(3, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateMaxAmountAndActive error: " + e.getMessage());
        }

        return false;
    }

    //---------------CHANGE_STATUS-----------------
    /** Chỉ update trạng thái active/inactive của rule, không thay đổi maxAmount */
    public boolean updateStatus(Long id, boolean active) {
        String sql = """
                UPDATE auto_bid_rules
                SET is_active = ?
                WHERE id = ?
                """;

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setLong(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateStatus error: " + e.getMessage());
        }

        return false;
    }

    /** Tắt/Bật auto bid của 1 bidder trong 1 auction */
    public boolean switchActiveStateRule(Long auctionId, Long bidderId, boolean state) {
        String sql = """
                UPDATE auto_bid_rules
                SET is_active = ?
                WHERE auction_id = ? AND bidder_id = ?
                """;

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, state);
            ps.setLong(2, auctionId);;
            ps.setLong(3, bidderId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("switchActiveStateRule error: " + e.getMessage());
        }

        return false;
    }
    //------------------DELETE----------------
    /** Xóa rule nếu cần */
    public boolean deleteRuleById(Long id) {
        String sql = "DELETE FROM auto_bid_rules WHERE id = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("deleteRuleById error: " + e.getMessage());
        }
        return false;
    }

    //----------------HELER-----------------
    private AutoBidRule mapResultSetToAutoBidRule(ResultSet rs) throws SQLException {
        AutoBidRule rule = new AutoBidRule();

        rule.setId(rs.getLong("id"));
        rule.setAuctionId(rs.getLong("auction_id"));
        rule.setBidderId(rs.getLong("bidder_id"));
        rule.setMaxAmount(rs.getBigDecimal("max_amount"));
        rule.setStepAmount(rs.getBigDecimal("step_amount"));
        rule.setIsActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rule.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            rule.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return rule;
    }
}
