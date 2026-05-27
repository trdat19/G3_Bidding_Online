package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.UserDAO;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        SingletonTestUtil.resetSingleton(UserService.class);
        userService = UserService.getInstance();
        inject("userDAO", userDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = UserService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(userService, mock);
    }

    //---------------QUERY------------------
    @Test
    @DisplayName("findUser - Tìm thấy người dùng hợp lệ!")
    public void findUser_found() {
        User mockUser = new Bidder();
        mockUser.setId(1L);
        mockUser.setRole(UserRole.BIDDER);

        when(userDAO.findById(1L)).thenReturn(mockUser);

        User result = userService.findUser(1L);

        assertEquals(1L, result.getId());
        assertEquals(UserRole.BIDDER, result.getRole());

        verify(userDAO, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findUser - Không tìm thấy, ném Exception!")
    public void findUser_notFound() {
        when(userDAO.findById(99L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findUser(99L);
        });

        assertEquals("Không tìm thấy người dùng #99", exception.getMessage());

        verify(userDAO, times(1)).findById(99L);
    }

    @Test
    @DisplayName("countAllUsers - Trả về đúng số lượng!")
    public void countAllUsers_success() {
        when(userDAO.getAllUsers()).thenReturn(List.of(new Admin(), new Seller(), new Bidder()));

        long result = userService.countAllUsers();

        assertEquals(3, result);

        verify(userDAO, times(1)).getAllUsers();
    }

    //-------------STATUS--------------------
    @Test
    @DisplayName("changeStatus - Thay đổi trạng thái thành công!")
    public void changeStatus_success() {
        User mockUser = new Admin();

        when(userDAO.findById(1L)).thenReturn(mockUser);
        when(userDAO.updateStatus(1L, UserStatus.BLOCKED)).thenReturn(true);

        assertTrue(userService.changeStatus(1L, UserStatus.BLOCKED));

        verify(userDAO, times(1)).findById(1L);
        verify(userDAO, times(1)).updateStatus(1L, UserStatus.BLOCKED);
    }
}