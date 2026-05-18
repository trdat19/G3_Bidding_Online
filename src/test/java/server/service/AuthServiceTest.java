//package server.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import server.dao.UserDAO;
//import server.model.user.Bidder;
//import server.model.user.User;
//import server.service.AuthService;
//import shared.enums.UserRole;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class AuthServiceTest {
//
//    private UserDAO userDAO;
//    private AuthService authService;
//
//    @BeforeEach
//    void setUp() {
//        userDAO = mock(UserDAO.class);
//        authService = new AuthService(userDAO);
//    }
//
//    // ================= LOGIN =================
//
//    @Test
//    void login_Success() {
//        User user = new Bidder();
//        user.setUsername("dat");
//        user.setPassword("123");
//
//        when(userDAO.findByUsername("dat")).thenReturn(user);
//
//        User result = authService.login("dat", "123");
//
//        assertNotNull(result);
//        assertEquals("dat", result.getUsername());
//    }
//
//    @Test
//    void login_WrongPassword() {
//        User user = new Bidder();
//        user.setUsername("dat");
//        user.setPassword("123");
//
//        when(userDAO.findByUsername("dat")).thenReturn(user);
//
//        User result = authService.login("dat", "wrong");
//
//        assertNull(result);
//    }
//
//    @Test
//    void login_UserNotFound() {
//        when(userDAO.findByUsername("dat")).thenReturn(null);
//
//        User result = authService.login("dat", "123");
//
//        assertNull(result);
//    }
//
//    // ================= REGISTER =================
//
//    @Test
//    void register_Success() throws Exception {
//        when(userDAO.existsByUsername("dat")).thenReturn(false);
//        when(userDAO.existsByEmail("dat@gmail.com")).thenReturn(false);
//        when(userDAO.insertUser(any())).thenReturn(true);
//
//        User result = authService.register(
//                "dat", "123", "Dat", "dat@gmail.com", UserRole.BIDDER
//        );
//
//        assertNotNull(result);
//        assertEquals("dat", result.getUsername());
//    }
//
//    @Test
//    void register_UsernameExists() {
//        when(userDAO.existsByUsername("dat")).thenReturn(true);
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            authService.register("dat", "123", "Dat", "dat@gmail.com", UserRole.BIDDER);
//        });
//
//        assertEquals("USERNAME_EXISTS", ex.getMessage());
//    }
//
//    @Test
//    void register_EmailExists() {
//        when(userDAO.existsByUsername("dat")).thenReturn(false);
//        when(userDAO.existsByEmail("dat@gmail.com")).thenReturn(true);
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            authService.register("dat", "123", "Dat", "dat@gmail.com", UserRole.BIDDER);
//        });
//
//        assertEquals("EMAIL_EXISTS", ex.getMessage());
//    }
//
//    @Test
//    void register_DatabaseFail() {
//        when(userDAO.existsByUsername("dat")).thenReturn(false);
//        when(userDAO.existsByEmail("dat@gmail.com")).thenReturn(false);
//        when(userDAO.insertUser(any())).thenReturn(false);
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            authService.register("dat", "123", "Dat", "dat@gmail.com", UserRole.BIDDER);
//        });
//
//        assertEquals("DATABASE_ERROR", ex.getMessage());
//    }
//}