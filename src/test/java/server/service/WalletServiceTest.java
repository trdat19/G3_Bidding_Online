package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.UserDAO;
import shared.exception.InsufficientBalanceException;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private UserDAO userDAO;

    private WalletService walletService;

    @BeforeEach
    public void setUp() throws Exception {
        SingletonTestUtil.resetSingleton(WalletService.class);
        walletService = WalletService.getInstance();
        inject("userDAO", userDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = WalletService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(walletService, mock);
    }

    //---------------GET BALANCE------------------
    @Test
    @DisplayName("getBalance - Lay so du thanh cong")
    public void getBalance_success() {
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("200.00"));

        BigDecimal result = walletService.getBalance(1L);

        assertEquals(new BigDecimal("200.00"), result);
        verify(userDAO, times(1)).getBalance(1L);
    }

    //---------------DEPOSIT------------------
    @Test
    @DisplayName("deposit - Nap tien thanh cong")
    public void deposit_success() {
        when(userDAO.increaseBalance(1L, new BigDecimal("100.00"))).thenReturn(true);
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("300.00"));

        BigDecimal result = walletService.deposit(1L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("300.00"), result);
        verify(userDAO, times(1)).increaseBalance(1L, new BigDecimal("100.00"));
        verify(userDAO, times(1)).getBalance(1L);
    }

    @Test
    @DisplayName("deposit - That bai khi amount null")
    public void deposit_nullAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.deposit(1L, null));

        verify(userDAO, never()).increaseBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("deposit - That bai khi amount bang 0")
    public void deposit_zeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.deposit(1L, BigDecimal.ZERO));

        verify(userDAO, never()).increaseBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("deposit - That bai khi amount am")
    public void deposit_negativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.deposit(1L, new BigDecimal("-50.00")));

        verify(userDAO, never()).increaseBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("deposit - That bai khi DAO khong cap nhat duoc")
    public void deposit_daoFailed() {
        when(userDAO.increaseBalance(1L, new BigDecimal("100.00"))).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                walletService.deposit(1L, new BigDecimal("100.00")));

        verify(userDAO, times(1)).increaseBalance(1L, new BigDecimal("100.00"));
        verify(userDAO, never()).getBalance(1L);
    }

    //---------------CHECK CAN BID------------------
    @Test
    @DisplayName("checkCanBid - Du tien de dat gia")
    public void checkCanBid_success() {
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("500.00"));

        assertDoesNotThrow(() ->
                walletService.checkCanBid(1L, 5L, new BigDecimal("200.00")));

        verify(userDAO, times(1)).getBalance(1L);
    }

    @Test
    @DisplayName("checkCanBid - Khong du tien de dat gia")
    public void checkCanBid_insufficientBalance() {
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("50.00"));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.checkCanBid(1L, 5L, new BigDecimal("200.00")));

        verify(userDAO, times(1)).getBalance(1L);
    }

    //---------------PAY FOR WINNING BID------------------
    @Test
    @DisplayName("payForWinningBid - Tru tien nguoi thang thanh cong")
    public void payForWinningBid_success() {
        when(userDAO.decreaseBalanceIfEnough(1L, new BigDecimal("150.00"))).thenReturn(true);
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("350.00"));

        BigDecimal result = walletService.payForWinningBid(1L, new BigDecimal("150.00"));

        assertEquals(new BigDecimal("350.00"), result);
        verify(userDAO, times(1)).decreaseBalanceIfEnough(1L, new BigDecimal("150.00"));
        verify(userDAO, times(1)).getBalance(1L);
    }

    @Test
    @DisplayName("payForWinningBid - That bai khi khong du tien")
    public void payForWinningBid_insufficientBalance() {
        when(userDAO.decreaseBalanceIfEnough(1L, new BigDecimal("150.00"))).thenReturn(false);
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("50.00"));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.payForWinningBid(1L, new BigDecimal("150.00")));

        verify(userDAO, times(1)).decreaseBalanceIfEnough(1L, new BigDecimal("150.00"));
        verify(userDAO, times(1)).getBalance(1L);
    }

    //---------------ADD SELLER REVENUE------------------
    @Test
    @DisplayName("addSellerRevenue - Cong tien cho seller thanh cong")
    public void addSellerRevenue_success() {
        when(userDAO.increaseBalance(2L, new BigDecimal("150.00"))).thenReturn(true);
        when(userDAO.getBalance(2L)).thenReturn(new BigDecimal("650.00"));

        BigDecimal result = walletService.addSellerRevenue(2L, new BigDecimal("150.00"));

        assertEquals(new BigDecimal("650.00"), result);
        verify(userDAO, times(1)).increaseBalance(2L, new BigDecimal("150.00"));
        verify(userDAO, times(1)).getBalance(2L);
    }

    @Test
    @DisplayName("addSellerRevenue - That bai khi amount khong hop le")
    public void addSellerRevenue_invalidAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.addSellerRevenue(2L, BigDecimal.ZERO));

        verify(userDAO, never()).increaseBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("addSellerRevenue - That bai khi DAO khong cap nhat duoc")
    public void addSellerRevenue_daoFailed() {
        when(userDAO.increaseBalance(2L, new BigDecimal("150.00"))).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                walletService.addSellerRevenue(2L, new BigDecimal("150.00")));

        verify(userDAO, times(1)).increaseBalance(2L, new BigDecimal("150.00"));
        verify(userDAO, never()).getBalance(2L);
    }

    //---------------PAY WINNER TO SELLER------------------
    @Test
    @DisplayName("payWinnerToSeller - Chuyen tien tu bidder sang seller thanh cong")
    public void payWinnerToSeller_success() {
        when(userDAO.transferBalanceIfEnough(1L, 2L, new BigDecimal("150.00"))).thenReturn(true);

        assertDoesNotThrow(() ->
                walletService.payWinnerToSeller(1L, 2L, new BigDecimal("150.00")));

        verify(userDAO, times(1)).transferBalanceIfEnough(1L, 2L, new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("payWinnerToSeller - That bai khi amount null")
    public void payWinnerToSeller_nullAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.payWinnerToSeller(1L, 2L, null));

        verify(userDAO, never()).transferBalanceIfEnough(anyLong(), anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("payWinnerToSeller - That bai khi amount bang 0")
    public void payWinnerToSeller_zeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                walletService.payWinnerToSeller(1L, 2L, BigDecimal.ZERO));

        verify(userDAO, never()).transferBalanceIfEnough(anyLong(), anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("payWinnerToSeller - That bai khi bidder khong du tien")
    public void payWinnerToSeller_insufficientBalance() {
        when(userDAO.transferBalanceIfEnough(1L, 2L, new BigDecimal("150.00"))).thenReturn(false);
        when(userDAO.getBalance(1L)).thenReturn(new BigDecimal("50.00"));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.payWinnerToSeller(1L, 2L, new BigDecimal("150.00")));

        verify(userDAO, times(1)).transferBalanceIfEnough(1L, 2L, new BigDecimal("150.00"));
        verify(userDAO, times(1)).getBalance(1L);
    }
}