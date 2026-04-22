package server.dao;

public class AuctionDao {

//    // thêm Auction vào bảng auctions
//    public boolean insertAuction (Auction auction) {
//        String sql =" INSERT INTO auctions(id_item, id_seller, start_price, max_price, min_increment, " +
//        "buy_now_price, start_time, end_time, status_auction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        try (Connection con = DBconnection.getInstance().getConnection();
//             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setLong(1, auction.getId());
//            ps.setLong(2, auction.getSellerId());
//            ps.setDouble(3, auction.getStartPrice());
//            ps.setDouble(4, auction.getMax_price());
//            ps.setDouble(5, auction.getMin_increment());
//            ps.setDouble(6, auction.getBuy_now_price());
//            ps.setTimestamp(7, auction.getStartTime());
//            ps.setTimestamp(8, auction.getEndTime());
//            ps.setString(9, auction.getStatus().name());
//
//            ps.executeUpdate();
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
}