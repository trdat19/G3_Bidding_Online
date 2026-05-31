package server.dao;

import server.database.DBConnection;
import server.model.item.Art;
import server.model.item.Electronics;
import server.model.item.Item;
import server.model.item.Vehicle;

import shared.dto.common.ItemDTO;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // thêm item mới
    public boolean insertItem(Item item) {
        String sql = """
                INSERT INTO items(name_item, category, description, id_seller,
                                  price_start, status_item, image_url, image_data, image_content_type)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, item.getNameItem());
            ps.setString(2, item.getCategory().name());
            ps.setString(3, item.getDescription());
            ps.setLong(4, item.getSellerId());
            ps.setBigDecimal(5, BigDecimal.ONE);
            ps.setString(6, item.getStatusItem().name());
            ps.setString(7, item.getImageUrl());
            ps.setBytes(8, item.getImageBytes());
            ps.setString(9, item.getImageContentType());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("insertItem error: " + e.getMessage());
        }
        return false;
    }

    // tìm item theo id
    public Item findById(Long id) {
        String sql = "SELECT * FROM items WHERE id_item = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("findById error: " + e.getMessage());
        }
        return null;
    }

    // tìm item theo tên
    public Item findByName(String nameItem) {
        String sql = "SELECT * FROM items WHERE name_item = ?";
        try(Connection con = DBConnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1,nameItem);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("FindByName_item error: " + e.getMessage());
        }
        return null;
    }

    // lấy tất cả item
    public List<Item> getAllItems() {
        String sql = "SELECT * FROM items";

        List<Item> items = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("getAllItems error: " + e.getMessage());
        }

        return items;
    }
    //Lay tong so item
    public long countAllItems() {
        String sql = "SELECT COUNT(*) FROM items";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            System.err.println("countAllItems error: " + e.getMessage());
        }

        return 0;
    }

    public List<ItemDTO> getAdminItemSummaries() {
        String sql = """
                SELECT
                    i.id_item,
                    i.name_item,
                    i.category,
                    i.description,
                    i.id_seller,
                    i.status_item,
                    i.created_atItem AS created_at_item,
                    u.full_name AS seller_name,
                    latest_auction.start_price,
                    latest_auction.max_price
                FROM items i
                LEFT JOIN users u ON u.id = i.id_seller
                LEFT JOIN (
                    SELECT a.*
                    FROM auctions a
                    INNER JOIN (
                        SELECT id_item, MAX(id_auction) AS latest_auction_id
                        FROM auctions
                        GROUP BY id_item
                    ) latest_ids ON latest_ids.latest_auction_id = a.id_auction
                ) latest_auction ON latest_auction.id_item = i.id_item
                ORDER BY i.id_item DESC
                """;

        List<ItemDTO> items = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToAdminItemDTO(rs));
            }

        } catch (SQLException e) {
            System.err.println("getAdminItemSummaries error: " + e.getMessage());
        }

        return items;
    }

    public List<ItemDTO> getSellerItemSummaries(Long sellerId) {
        String sql = """
            SELECT
                i.id_item,
                i.name_item,
                i.category,
                i.description,
                i.id_seller,
                i.status_item,
                i.created_atItem AS created_at_item,
                i.image_url,
                i.image_data,
                i.image_content_type,
                latest_auction.start_price,
                latest_auction.max_price
            FROM items i
            LEFT JOIN (
                SELECT a.*
                FROM auctions a
                INNER JOIN (
                    SELECT id_item, MAX(id_auction) AS latest_auction_id
                    FROM auctions
                    GROUP BY id_item
                ) latest_ids ON latest_ids.latest_auction_id = a.id_auction
            ) latest_auction ON latest_auction.id_item = i.id_item
            WHERE i.id_seller = ?
            ORDER BY i.id_item DESC
            """;

        List<ItemDTO> items = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, sellerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToSellerItemDTO(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("getSellerItemSummaries error: " + e.getMessage());
        }

        return items;
    }

    // lấy item theo id của seller
    public List<Item> findBySellerId(Long sellerId) {
        String sql = "SELECT * FROM items WHERE id_seller = ?";
        List<Item> items = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("findBySellerId error: " + e.getMessage());
        }
        return items;
    }

    // cập nhật toàn bộ item
    public boolean updateItem(Item item) {
        String sql = """
                    UPDATE items SET name_item = ?, category = ?, description = ?, id_seller = ?,
                                     price_start = ?, status_item = ?,  image_url = ?, image_data = ?,
                                     image_content_type = ?
                    WHERE id_item = ?
                """;

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, item.getNameItem());
            ps.setString(2, item.getCategory().name());
            ps.setString(3, item.getDescription());
            ps.setLong(4, item.getSellerId());
            ps.setBigDecimal(5, BigDecimal.ONE);
            ps.setString(6, item.getStatusItem().name());
            ps.setString(7, item.getImageUrl());
            ps.setBytes(8, item.getImageBytes());
            ps.setString(9, item.getImageContentType());
            ps.setLong(10, item.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateItem error: " + e.getMessage());
        }
        return false;
    }

    // đổi status item
    public boolean updateStatus(Long idItem, ItemStatus status) {
        String sql = "UPDATE items SET status_item = ? WHERE id_item = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, status.name());
            ps.setLong(2, idItem);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateStatus error: " + e.getMessage());
        }
        return false;
    }

    // xóa item
    public boolean deleteItem(Long idItem) {
        String sql = "DELETE FROM items WHERE id_item = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idItem);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("deleteItem error: " + e.getMessage());
        }
        return false;
    }

    // map 1 dòng DB -> object item
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        long id = rs.getLong("id_item");
        String nameItem = rs.getString("name_item");
        ItemCategory category = ItemCategory.valueOf(rs.getString("category"));
        String description = rs.getString("description");
        long sellerId = rs.getLong("id_seller");
        BigDecimal priceStart = rs.getBigDecimal("price_start");
        ItemStatus statusItem = ItemStatus.valueOf(rs.getString("status_item"));
        Timestamp createdAtItem = rs.getTimestamp("created_atItem");
        String imageUrl = rs.getString("image_url");
        byte[] imageBytes = rs.getBytes("image_data");
        String imageContentType = rs.getString("image_content_type");
        switch (category) {
            case ART: {
                Art art = new Art(nameItem, description, sellerId, statusItem);
                art.setId(id);
                if (createdAtItem != null) {
                    art.setCreatedAtItem(createdAtItem.toLocalDateTime());
                }
                art.setImageUrl(imageUrl);
                art.setImageBytes(imageBytes);
                art.setImageContentType(imageContentType);
                return art;
            }
            case ELECTRONICS: {
                Electronics electronics = new Electronics(nameItem, description, sellerId, statusItem);
                electronics.setId(id);
                if (createdAtItem != null) {
                    electronics.setCreatedAtItem(createdAtItem.toLocalDateTime());
                }
                electronics.setImageUrl(imageUrl);
                electronics.setImageBytes(imageBytes);
                electronics.setImageContentType(imageContentType);
                return electronics;
            }
            case VEHICLE: {
                Vehicle vehicle = new Vehicle(nameItem, description, sellerId, statusItem);
                vehicle.setId(id);
                if (createdAtItem != null) {
                    vehicle.setCreatedAtItem(createdAtItem.toLocalDateTime());
                }
                vehicle.setImageUrl(imageUrl);
                vehicle.setImageBytes(imageBytes);
                vehicle.setImageContentType(imageContentType);
                return vehicle;
            }
            default: {
                throw new SQLException("Invalid item category: " + category);
            }
        }
    }

    private ItemDTO mapResultSetToAdminItemDTO(ResultSet rs) throws SQLException {
        ItemDTO dto = new ItemDTO();

        dto.setId(rs.getLong("id_item"));
        dto.setName(rs.getString("name_item"));
        dto.setDescription(rs.getString("description"));
        dto.setCategory(ItemCategory.valueOf(rs.getString("category")));
        dto.setStatus(ItemStatus.valueOf(rs.getString("status_item")));
        dto.setSellerId(rs.getLong("id_seller"));

        String sellerName = rs.getString("seller_name");
        dto.setSellerName(sellerName != null ? sellerName : "Khong xac dinh");

        Timestamp createdAt = rs.getTimestamp("created_at_item");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime());
        }

        if (dto.getStatus() != ItemStatus.PENDING) {
            BigDecimal startPrice = rs.getBigDecimal("start_price");
            BigDecimal currentPrice = rs.getBigDecimal("max_price");

            dto.setPriceStart(startPrice);
            dto.setCurrentPrice(currentPrice != null ? currentPrice : startPrice);
        }

        return dto;
    }

    private ItemDTO mapResultSetToSellerItemDTO(ResultSet rs) throws SQLException {
        ItemDTO dto = new ItemDTO();

        dto.setId(rs.getLong("id_item"));
        dto.setName(rs.getString("name_item"));
        dto.setDescription(rs.getString("description"));
        dto.setCategory(ItemCategory.valueOf(rs.getString("category")));
        dto.setStatus(ItemStatus.valueOf(rs.getString("status_item")));
        dto.setSellerId(rs.getLong("id_seller"));

        Timestamp createdAt = rs.getTimestamp("created_at_item");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime());
        }

        BigDecimal startPrice = rs.getBigDecimal("start_price");
        BigDecimal currentPrice = rs.getBigDecimal("max_price");
        dto.setPriceStart(startPrice);
        dto.setCurrentPrice(currentPrice != null ? currentPrice : startPrice);

        dto.setImageUrl(rs.getString("image_url"));
        dto.setImageBytes(rs.getBytes("image_data"));
        dto.setImageContentType(rs.getString("image_content_type"));

        return dto;
    }
}
