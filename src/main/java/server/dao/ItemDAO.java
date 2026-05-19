package server.dao;

import server.database.DBconnection;
import server.model.item.Art;
import server.model.item.Electronics;
import server.model.item.Item;
import server.model.item.Vehicle;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // thêm item mới
    public boolean insertItem(Item item) {
        String sql = "INSERT INTO items(name_item, category, description, id_seller, price_start, status_item, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ? , ?)";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, item.getNameItem());
            ps.setString(2, item.getCategory().name());
            ps.setString(3, item.getDescription());
            ps.setLong(4, item.getSellerId());
            ps.setBigDecimal(5, BigDecimal.ONE);
            ps.setString(6, item.getStatusItem().name());
            ps.setString(7, item.getImageUrl());

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
    public Item findById(long id) {
        String sql = "SELECT * FROM items WHERE id_item = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
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
        try(Connection con = DBconnection.getInstance().getConnection();
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
        try (Connection con = DBconnection.getInstance().getConnection();
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

    // lấy item theo id của seller
    public List<Item> findBySellerId(long sellerId) {
        String sql = "SELECT * FROM items WHERE id_seller = ?";
        List<Item> items = new ArrayList<>();

        try (Connection con = DBconnection.getInstance().getConnection();
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
        String sql = "UPDATE items SET name_item = ?, category = ?, description = ?, " +
                "id_seller = ?, price_start = ?, status_item = ?, image_url = ? WHERE id_item = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, item.getNameItem());
            ps.setString(2, item.getCategory().name());
            ps.setString(3, item.getDescription());
            ps.setLong(4, item.getSellerId());
            ps.setBigDecimal(5, BigDecimal.ONE);
            ps.setString(6, item.getStatusItem().name());
            ps.setString(7,item.getImageUrl());
            ps.setLong(8, item.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updateItem error: " + e.getMessage());
        }
        return false;
    }

    // đổi status item
    public boolean updateStatus(long idItem, ItemStatus status) {
        String sql = "UPDATE items SET status_item = ? WHERE id_item = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
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
    public boolean deleteItem(long idItem) {
        String sql = "DELETE FROM items WHERE id_item = ?";

        try (Connection con = DBconnection.getInstance().getConnection();
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

        switch (category) {
            case ART:
                Art art = new Art(nameItem, description, sellerId, statusItem);
                art.setId(id);
                art.setCreatedAtItem(createdAtItem.toLocalDateTime());
                art.setImageUrl(imageUrl);
                return art;

            case ELECTRONICS:
                Electronics electronics = new Electronics(nameItem, description, sellerId, statusItem);
                electronics.setId(id);
                electronics.setCreatedAtItem(createdAtItem.toLocalDateTime());
                electronics.setImageUrl(imageUrl);
                return electronics;

            case VEHICLE:
                Vehicle vehicle = new Vehicle(nameItem, description, sellerId, statusItem);
                vehicle.setId(id);
                vehicle.setCreatedAtItem(createdAtItem.toLocalDateTime());
                vehicle.setImageUrl(imageUrl);
                return vehicle;

            default:
                throw new SQLException("Invalid item category: " + category);
        }
    }
}