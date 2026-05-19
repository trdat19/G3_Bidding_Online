package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.ItemDAO;
import server.model.item.Item;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock private ItemDAO itemDAO;

    private ItemService itemService;

    @BeforeEach
    public void setup() throws Exception {
        SingletonTestUtil.resetSingleton(ItemService.class);
        itemService = ItemService.getInstance();
        inject("itemDAO", itemDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = ItemService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(itemService, mock);
    }

    // ------------------CREATE/DELETE--------------
    @Test
    @DisplayName("createItem - Tạo sản phẩm thành công với dữ liệu hợp lệ")
    public void createItem_success() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Item");
        data.put("description", "for test");
        data.put("category", ItemCategory.ELECTRONICS);

        when(itemDAO.insertItem(any(Item.class))).thenReturn(true);

        Item result = itemService.createItem(1L, data);
        assertNotNull(result);

        verify(itemDAO, times(1)).insertItem(any(Item.class));
    }

    @Test
    @DisplayName("createItem - Tạo sản phẩm thất bại, thiếu thông tin, ném exception!")
    public void createItem_failure_missingInfo() {
        Map<String, Object> data = Map.of("name", "watch");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            itemService.createItem(1L, data);
        });

        assertEquals("Thiếu thông tin cần thiết để tạo sản phẩm!", exception.getMessage());
    }

    @Test
    @DisplayName("deleteItem - Xoá thành công sản phẩm PENDING")
    public void deleteItem_success() {
        Item item = mock(Item.class);

        when(item.getStatusItem()).thenReturn(ItemStatus.PENDING);
        when(itemDAO.findById(1L)).thenReturn(item);
        when(itemDAO.deleteItem(1L)).thenReturn(true);

        assertTrue(itemService.deleteItem(1L));
    }

    @Test
    @DisplayName("deleteItem - Xoá thất bại sản phẩm không phải PENDING, ném exception!")
    public void deleteItem_failed_itemNotPENDING() {
        Item item = mock(Item.class);

        when(item.getStatusItem()).thenReturn(ItemStatus.ACTIVE);
        when(itemDAO.findById(1L)).thenReturn(item);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            itemService.deleteItem(1L);
        });

        assertEquals("Chỉ có thể xóa sản phẩm khi đang PENDING!", exception.getMessage());
     }

     //-----------------------UPDATE-------------------
    @Test
    @DisplayName("updateItem - Cập nhật sản phẩm thành công, tìm được ID!")
    public void updateItem_success() {
        Item item = mock(Item.class);

        when(itemDAO.findById(1L)).thenReturn(item);
        when(itemDAO.updateItem(any(Item.class))).thenReturn(true);

        assertNotNull(itemService.updateItem(Map.of("id", 1L, "name", "New Name")));
    }

    @Test
    @DisplayName("updateItem - Cập nhật sản phẩm thất bại, không tìm được ID, ném exception!")
    public void updateItem_failed_idNotFound() {
        when(itemDAO.findById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            itemService.updateItem(Map.of("id", 1L, "name", "New Name"));
        });

        assertEquals("Sản phẩm không tồn tại!", exception.getMessage());
    }
}
