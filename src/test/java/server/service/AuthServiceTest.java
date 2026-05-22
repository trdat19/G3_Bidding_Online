//package server.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import server.dao.UserDAO;
//import server.model.user.Seller;
//import server.model.user.User;
//import shared.enums.UserRole;
//import shared.enums.UserStatus;
//
//import java.lang.reflect.Field;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AuthServiceTest {
//
//    @Mock
//    private UserDAO userDAO;
//
//    private AuthService authService;
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        SingletonTestUtil.resetSingleton(AuthService.class);
//        authService = AuthService.getInstance();
//        inject("userDAO", userDAO);
//    }
//
//    private void inject(String name, Object mock) throws Exception {
//        Field field = AuthService.class.getDeclaredField(name);
//        field.setAccessible(true);
//        field.set(authService, mock);
//    }
//
//    //-------------------LOGIN-------------------
//    @Test
//    @DisplayName("Login thành công với thông tin hợp lệ!")
//    public void login_success() {
//        // 1. Chuẩn bị dữ liệu
//        User mockUser = new Seller();
//        mockUser.setUsername("seller");
//        mockUser.setPassword("123456");
//
//        // thông báo kiểu: tí nữa nếu có ai gọi .findByUsername() thì không trả về đối tượng thật
//        // mà trả về mockUser đã tạo ở trên
//        when(userDAO.findByUsername("seller")).thenReturn(mockUser);
//
//        // 2. Thực thi
//        User result = authService.login("seller", "123456");
//
//        // 3. Assert kết quả
//        assertNotNull(result);
//        assertEquals("seller", result.getUsername());
//        assertEquals("123456", result.getPassword());
//
//        verify(userDAO, times(1)).findByUsername("seller");
//    }
//
//    @Test
//    @DisplayName("Login thất bại khi username không tồn tại!")
//    public void login_failed_usernameNotFound() {
//        when(userDAO.findByUsername("seller")).thenReturn(null);
//
//        User result = authService.login("seller", "654321");
//
//        assertNull(result);
//
//        verify(userDAO, times(1)).findByUsername("seller");
//    }
//
//    @Test
//    @DisplayName("Login thất bại khi password sai!")
//    public void login_failed_wrongPassword() {
//        User mockUser = new Seller();
//        mockUser.setUsername("seller");
//        mockUser.setPassword("wrongPass");
//
//        when(userDAO.findByUsername("seller")).thenReturn(mockUser);
//
//        User result = authService.login("seller", "correctPass");
//
//        assertNull(result);
//
//        verify(userDAO, times(1)).findByUsername("seller");
//    }
//
//    //------------------REGISTER-----------------------
//    @Test
//    @DisplayName("Đăng ký thành công với thông tin hợp lệ!")
//    public void register_success() throws Exception {
//        when(userDAO.existsByUsername("newUser")).thenReturn(false);
//        when(userDAO.existsByEmail("newUser@example.com")).thenReturn(false);
//
//        when(userDAO.insertUser(any(User.class))).thenReturn(true);
//
//        User result = authService.register("newUser",
//                "password",
//                "New User",
//                "newUser@example.com",
//                UserRole.BIDDER);
//
//        assertNotNull(result);
//        assertEquals("newUser", result.getUsername());
//        assertEquals("password", result.getPassword());
//        assertEquals("New User", result.getFullName());
//        assertEquals("newUser@example.com", result.getEmail());
//        assertEquals(UserRole.BIDDER, result.getRole());
//        assertEquals(UserStatus.ACTIVE, result.getStatus());
//
//        verify(userDAO, times(1)).existsByUsername("newUser");
//        verify(userDAO, times(1)).existsByEmail("newUser@example.com");
//        verify(userDAO, times(1)).insertUser(any(User.class));
//    }
//
//    @Test
//    @DisplayName("Đăng ký thất bại khi username đã tồn tại!")
//    public void register_failed_duplicateUsername() {
//        when(userDAO.existsByUsername("existingUser")).thenReturn(true);
//
//        Exception exception = assertThrows(Exception.class, () -> {
//            authService.register("existingUser",
//                                    "password",
//                                    "Existing User",
//                                        "existingUser@example.com",
//                                            UserRole.BIDDER);
//        });
//
//        assertEquals("USERNAME_EXISTS", exception.getMessage());
//
//        verify(userDAO, times(1)).existsByUsername("existingUser");
//    }
//
//    @Test
//    @DisplayName("Đăng ký thất bại khi email đã tồn tại!")
//    public void register_failed_duplicateEmail() {
//        when(userDAO.existsByUsername("existingUser")).thenReturn(false);
//        when(userDAO.existsByEmail("existingEmail")).thenReturn(true);
//
//        Exception exception = assertThrows(Exception.class, () -> {
//            authService.register("existingUser",
//                                    "password",
//                                    "New User",
//                                        "existingEmail",
//                                            UserRole.BIDDER);
//        });
//
//        assertEquals("EMAIL_EXISTS", exception.getMessage());
//
//        verify(userDAO, times(1)).existsByUsername("existingUser");
//        verify(userDAO, times(1)).existsByEmail("existingEmail");
//    }
//}