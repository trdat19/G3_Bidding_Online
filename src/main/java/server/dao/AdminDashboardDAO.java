package server.dao;

import server.database.DBconnection;
import shared.dto.AdminDashboardDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardDAO {

    public AdminDashboardDTO getSummary() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM users) AS total_users,
                    (SELECT COUNT(*) FROM users WHERE status = 'BLOCKED') AS blocked_users,
                    (SELECT COUNT(*) FROM auctions WHERE status_auction = 'RUNNING')
                        AS running_auctions,
                    (SELECT COUNT(*) FROM auctions WHERE status_auction = 'FINISHED')
                        AS finished_auctions,
                    (SELECT COUNT(*) FROM auctions WHERE status_auction = 'WAITING_APPROVAL')
                        AS pending_requests,
                    (SELECT COUNT(*) FROM items) AS total_products,
                    (SELECT COUNT(*) FROM items WHERE status_item = 'ACTIVE') AS active_products
                """;

        try (Connection connection = DBconnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                AdminDashboardDTO summary = new AdminDashboardDTO();
                summary.setTotalUsers(result.getLong("total_users"));
                summary.setBlockedUsers(result.getLong("blocked_users"));
                summary.setRunningAuctions(result.getLong("running_auctions"));
                summary.setFinishedAuctions(result.getLong("finished_auctions"));
                summary.setPendingRequests(result.getLong("pending_requests"));
                summary.setTotalProducts(result.getLong("total_products"));
                summary.setActiveProducts(result.getLong("active_products"));
                return summary;
            }
        } catch (SQLException e) {
            System.err.println("getDashboardSummary error: " + e.getMessage());
        }

        return null;
    }
}
