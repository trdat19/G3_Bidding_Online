package server.dao;

import server.database.DBconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InterestedAuctionDAO {

    public boolean markFollowed(Long bidderId, Long auctionId) {
        String sql = """
                INSERT INTO bidder_interested_auctions (bidder_id, auction_id, followed, joined)
                VALUES (?, ?, true, false)
                ON DUPLICATE KEY UPDATE followed = true, updated_at = CURRENT_TIMESTAMP
                """;
        return saveInterest(sql, bidderId, auctionId);
    }

    public boolean markJoined(Long bidderId, Long auctionId) {
        String sql = """
                INSERT INTO bidder_interested_auctions (bidder_id, auction_id, followed, joined)
                VALUES (?, ?, false, true)
                ON DUPLICATE KEY UPDATE joined = true, updated_at = CURRENT_TIMESTAMP
                """;
        return saveInterest(sql, bidderId, auctionId);
    }

    private boolean saveInterest(String sql, Long bidderId, Long auctionId) {
        try (Connection connection = DBconnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bidderId);
            statement.setLong(2, auctionId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("saveInterest error: " + e.getMessage());
            return false;
        }
    }

    public List<Long> findInterestedAuctionIds(Long bidderId) {
        String sql = """
                SELECT auction_id
                FROM bidder_interested_auctions
                WHERE bidder_id = ? AND (followed = true OR joined = true)
                ORDER BY updated_at DESC
                """;
        List<Long> auctionIds = new ArrayList<>();

        try (Connection connection = DBconnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bidderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    auctionIds.add(resultSet.getLong("auction_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("findInterestedAuctionIds error: " + e.getMessage());
        }
        return auctionIds;
    }
}
